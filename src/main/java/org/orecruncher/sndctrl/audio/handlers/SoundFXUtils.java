/*
 * Dynamic Surroundings:
 * Sound Physics
 * Copyright (C) 2020  OreCruncher
 * Copyright SonicEther
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 *
 * Summary of changes:
 *
 * - Rework to incorporate into Sound Control's framework.
 * - Precalculate ray cast vectors
 * - Rework for parallel stream operation
 * - Added effect of rain on sound dampening
 * - Listener head in various fluids support
 * - Precache frequently used world information
 */

package org.orecruncher.sndctrl.audio.handlers;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.math.BlockRayTrace;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.math.RayTraceIterator;
import org.orecruncher.mobeffects.library.Constants;
import org.orecruncher.sndctrl.audio.SoundUtils;
import org.orecruncher.sndctrl.config.Config;
import org.orecruncher.sndctrl.audio.handlers.effects.LowPassData;
import org.orecruncher.sndctrl.audio.handlers.effects.SourcePropertyFloat;
import org.orecruncher.sndctrl.library.AudioEffectLibrary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public final class SoundFXUtils {

    /**
     * Maximum number of segments to check when ray tracing for occlusion.
     */
    private static final int OCCLUSION_SEGMENTS = 5;
    /**
     * Number of rays to project when doing reverb calculations.
     */
    private static final int REVERB_RAYS = 32;
    /**
     * Number of bounces a sound wave will make when projecting.
     */
    private static final int REVERB_RAY_BOUNCES = 4;
    /**
     * Maximum distance to trace a reverb ray segment before stopping.
     */
    private static final float MAX_REVERB_DISTANCE = 256;
    /**
     * Reciprocal of the total number of rays cast.
     */
    private static final float RECIP_TOTAL_RAYS = 1F / (REVERB_RAYS * REVERB_RAY_BOUNCES);
    /**
     * Reciprocal of the total primary rays.
     */
    private static final float RECIP_PRIMARY_RAYS = 1F / REVERB_RAYS;
    /**
     * Sound reflection energy coefficient
     */
    private static final float ENERGY_COEFF = 0.75F * 0.25F * RECIP_TOTAL_RAYS;
    /**
     * Sound reflection energy constant
     */
    private static final float ENERGY_CONST = 0.25F * 0.25F * RECIP_TOTAL_RAYS;
    /**
     * Normals for the direction of each of the rays to be cast.
     */
    private static final Vector3d[] REVERB_RAY_NORMALS = new Vector3d[REVERB_RAYS];
    /**
     * Precalculated vectors to determine end targets relative to an origin.
     */
    private static final Vector3d[] REVERB_RAY_PROJECTED = new Vector3d[REVERB_RAYS];
    /**
     * Precaluclated direction surface normals as Vec3d instead of Vec3i
     */
    private static final Vector3d[] SURFACE_DIRECTION_NORMALS = new Vector3d[Direction.values().length];

    static {

        // Would have been cool to have a direction vec as a 3d as well as 3i.
        for (final Direction d : Direction.values()) {
            Vector3i v = d.getDirectionVec();
            SURFACE_DIRECTION_NORMALS[d.ordinal()] = new Vector3d(v.getX(), v.getY(), v.getZ());
        }

        // Pre-calculate the known vectors that will be projected off a sound source when casting about to establish
        // reverb effects.
        for (int i = 0; i < REVERB_RAYS; i++) {
            final double longitude = MathStuff.ANGLE * i;
            final double latitude = Math.asin(((double) i / REVERB_RAYS) * 2.0D - 1.0D);

            REVERB_RAY_NORMALS[i] = new Vector3d(
                    Math.cos(latitude) * Math.cos(longitude),
                    Math.cos(latitude) * Math.sin(longitude),
                    Math.sin(latitude)
            ).normalize();

            REVERB_RAY_PROJECTED[i] = REVERB_RAY_NORMALS[i].scale(MAX_REVERB_DISTANCE);
        }

    }

    private final SourceContext source;

    public SoundFXUtils(@Nonnull final SourceContext source) {
        this.source = source;
    }

    public void calculate(@Nonnull final WorldContext ctx) {

        assert ctx.player != null;
        assert ctx.world != null;
        assert this.source.getSound() != null;

        if (ctx.isNotValid()
                || !this.source.isEnabled()
                || !SoundUtils.inRange(ctx.playerEyePosition, this.source.getSound())
                || this.source.getPosition().equals(Vector3d.ZERO)) {
            this.clearSettings();
            return;
        }

        if (this.source.getCategory() == Constants.FOOTSTEPS) {
            int x = 0;
        }

        // Need to offset sound toward player if it is in a solid block
        final Vector3d soundPos = offsetPositionIfSolid(ctx.world, this.source.getPosition(), ctx.playerEyePosition);

        final float absorptionCoeff = Effects.GLOBAL_BLOCK_ABSORPTION * 3.0F;
        final float airAbsorptionFactor = calculateWeatherAbsorption(ctx, soundPos, ctx.playerEyePosition);
        final float occlusionAccumulation = calculateOcclusion(ctx, soundPos, ctx.playerEyePosition);
        final float sendCoeff = -occlusionAccumulation * absorptionCoeff;

        float directCutoff = (float) MathStuff.exp(sendCoeff);

        // Handle any dampening effects from the player - like head in water
        directCutoff *= 1F - ctx.auralDampening;

        // Calculate reverb parameters for this sound
        float sendGain0 = 0F;
        float sendGain1 = 0F;
        float sendGain2 = 0F;
        float sendGain3 = 0F;

        float sendCutoff0;
        float sendCutoff1;
        float sendCutoff2;
        float sendCutoff3;

        // Shoot rays around sound
        final float[] bounceRatio = new float[REVERB_RAY_BOUNCES];

        float sharedAirspace = 0F;

        final BlockRayTrace traceContext = new BlockRayTrace(ctx.world, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.SOURCE_ONLY);

        for (int i = 0; i < REVERB_RAYS; i++) {

            Vector3d origin = soundPos;
            Vector3d target = origin.add(REVERB_RAY_PROJECTED[i]);

            BlockRayTraceResult rayHit = traceContext.trace(origin, target);

            if (isMiss(rayHit))
                continue;

            // Additional bounces
            BlockPos lastHitBlock = rayHit.getPos();
            Vector3d lastHitPos = rayHit.getHitVec();
            Vector3d lastHitNormal = surfaceNormal(rayHit.getFace());
            Vector3d lastRayDir = REVERB_RAY_NORMALS[i];

            double totalRayDistance = origin.distanceTo(rayHit.getHitVec());

            // Secondary ray bounces
            for (int j = 0; j < REVERB_RAY_BOUNCES; j++) {

                final float blockReflectivity = AudioEffectLibrary.getReflectivity(ctx.world.getBlockState(lastHitBlock));
                final float energyTowardsPlayer = blockReflectivity * ENERGY_COEFF + ENERGY_CONST;

                final Vector3d newRayDir = MathStuff.reflection(lastRayDir, lastHitNormal);
                origin = MathStuff.addScaled(lastHitPos, newRayDir, 0.01F);
                target = MathStuff.addScaled(origin, newRayDir, MAX_REVERB_DISTANCE);

                rayHit = traceContext.trace(origin, target);

                if (isMiss(rayHit)) {
                    totalRayDistance += lastHitPos.distanceTo(ctx.playerEyePosition);
                } else {

                    bounceRatio[j] += blockReflectivity;
                    totalRayDistance += lastHitPos.distanceTo(rayHit.getHitVec());

                    lastHitPos = rayHit.getHitVec();
                    lastHitNormal = surfaceNormal(rayHit.getFace());
                    lastRayDir = newRayDir;
                    lastHitBlock = rayHit.getPos();

                    // Cast a ray back at the player.  If it is a miss there is a path back from the reflection
                    // point to the player meaning they share the same airspace.
                    final Vector3d finalRayStart = MathStuff.addScaled(lastHitPos, lastHitNormal, 0.01F);
                    final BlockRayTraceResult finalRayHit = traceContext.trace(finalRayStart, ctx.playerEyePosition);
                    if (isMiss(finalRayHit)) {
                        sharedAirspace += 1.0F;
                    }
                }

                assert totalRayDistance >= 0;
                final float reflectionDelay = (float) totalRayDistance * 0.12F * blockReflectivity;

                final float cross0 = 1.0F - MathStuff.clamp1(Math.abs(reflectionDelay - 0.0F));
                final float cross1 = 1.0F - MathStuff.clamp1(Math.abs(reflectionDelay - 1.0F));
                final float cross2 = 1.0F - MathStuff.clamp1(Math.abs(reflectionDelay - 2.0F));
                final float cross3 = MathStuff.clamp1(reflectionDelay - 2.0F);

                sendGain0 += cross0 * energyTowardsPlayer * 6.4F;
                sendGain1 += cross1 * energyTowardsPlayer * 12.8F;
                sendGain2 += cross2 * energyTowardsPlayer * 12.8F;
                sendGain3 += cross3 * energyTowardsPlayer * 12.8F;

                // Nowhere to bounce off of, stop bouncing!
                if (isMiss(rayHit)) {
                    break;
                }
            }
        }

        bounceRatio[0] = bounceRatio[0] / REVERB_RAYS;
        bounceRatio[1] = bounceRatio[1] / REVERB_RAYS;
        bounceRatio[2] = bounceRatio[2] / REVERB_RAYS;
        bounceRatio[3] = bounceRatio[3] / REVERB_RAYS;

        sharedAirspace *= RECIP_TOTAL_RAYS * 64F;

        final float sharedAirspaceWeight0 = MathStuff.clamp1(sharedAirspace / 20.0F);
        final float sharedAirspaceWeight1 = MathStuff.clamp1(sharedAirspace / 15.0F);
        final float sharedAirspaceWeight2 = MathStuff.clamp1(sharedAirspace / 10.0F);
        final float sharedAirspaceWeight3 = MathStuff.clamp1(sharedAirspace / 10.0F);

        final float exp1 = (float) MathStuff.exp(sendCoeff);
        final float exp2 = (float) MathStuff.exp(sendCoeff * 1.5F);
        sendCutoff0 = exp1 * (1.0F - sharedAirspaceWeight0) + sharedAirspaceWeight0;
        sendCutoff1 = exp1 * (1.0F - sharedAirspaceWeight1) + sharedAirspaceWeight1;
        sendCutoff2 = exp2 * (1.0F - sharedAirspaceWeight2) + sharedAirspaceWeight2;
        sendCutoff3 = exp2 * (1.0F - sharedAirspaceWeight3) + sharedAirspaceWeight3;

        final float averageSharedAirspace = (sharedAirspaceWeight0 + sharedAirspaceWeight1 + sharedAirspaceWeight2
                + sharedAirspaceWeight3) * 0.25F;
        directCutoff = Math.max((float) Math.sqrt(averageSharedAirspace) * 0.2F, directCutoff);

        float directGain = (float) MathStuff.pow(directCutoff, 0.1);

        sendGain1 *= bounceRatio[1];
        sendGain2 *= (float) MathStuff.pow(bounceRatio[2], 3.0);
        sendGain3 *= (float) MathStuff.pow(bounceRatio[3], 4.0);

        sendGain0 = MathStuff.clamp1(sendGain0);
        sendGain1 = MathStuff.clamp1(sendGain1);
        sendGain2 = MathStuff.clamp1(sendGain2 * 1.05F - 0.05F);
        sendGain3 = MathStuff.clamp1(sendGain3 * 1.05F - 0.05F);

        sendGain0 *= (float) MathStuff.pow(sendCutoff0, 0.1);
        sendGain1 *= (float) MathStuff.pow(sendCutoff1, 0.1);
        sendGain2 *= (float) MathStuff.pow(sendCutoff2, 0.1);
        sendGain3 *= (float) MathStuff.pow(sendCutoff3, 0.1);

        if (ctx.player.isInWater()) {
            sendCutoff0 *= 0.4F;
            sendCutoff1 *= 0.4F;
            sendCutoff2 *= 0.4F;
            sendCutoff3 *= 0.4F;
        }

        final LowPassData lp0 = this.source.getLowPass0();
        final LowPassData lp1 = this.source.getLowPass1();
        final LowPassData lp2 = this.source.getLowPass2();
        final LowPassData lp3 = this.source.getLowPass3();
        final LowPassData direct = this.source.getDirect();
        final SourcePropertyFloat prop = this.source.getAirAbsorb();

        synchronized (this.source.sync()) {
            lp0.gain = sendGain0;
            lp0.gainHF = sendCutoff0;
            lp0.setProcess(true);

            lp1.gain = sendGain1;
            lp1.gainHF = sendCutoff1;
            lp1.setProcess(true);

            lp2.gain = sendGain2;
            lp2.gainHF = sendCutoff2;
            lp2.setProcess(true);

            lp3.gain = sendGain3;
            lp3.gainHF = sendCutoff3;
            lp3.setProcess(true);

            direct.gain = directGain;
            direct.gainHF = directCutoff;
            direct.setProcess(true);

            prop.setValue(airAbsorptionFactor);
            prop.setProcess(true);
        }
    }

    private void clearSettings() {
        synchronized (this.source.sync()) {
            source.getLowPass0().setProcess(false);
            source.getLowPass1().setProcess(false);
            source.getLowPass2().setProcess(false);
            source.getLowPass3().setProcess(false);
            source.getDirect().setProcess(false);
            source.getAirAbsorb().setProcess(false);
        }
    }

    private float calculateOcclusion(@Nonnull final WorldContext ctx, @Nonnull final Vector3d origin, @Nonnull final Vector3d target) {

        assert ctx.world != null;
        assert ctx.player != null;

        // If occlusion is not enabled, short cut
        if (!Config.CLIENT.sound.enableOcclusionCalcs.get())
            return 0F;

        // See if the category is eligible for occlusion
        if (!this.source.getCategory().doOcclusion())
            return 0F;

        float factor = 0F;

        if (Config.CLIENT.sound.enableOcclusionCalcs.get()) {
            Vector3d lastHit = origin;
            BlockState lastState = ctx.world.getBlockState(new BlockPos(lastHit.getX(), lastHit.getY(), lastHit.getZ()));
            final BlockRayTrace traceContext = new BlockRayTrace(ctx.world, origin, target, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.SOURCE_ONLY);
            final Iterator<BlockRayTraceResult> itr = new RayTraceIterator(traceContext);
            for (int i = 0; i < OCCLUSION_SEGMENTS; i++) {
                if (itr.hasNext()) {
                    final BlockRayTraceResult result = itr.next();
                    final float occlusion = AudioEffectLibrary.getOcclusion(lastState);
                    final double distance = lastHit.distanceTo(result.getHitVec());
                    // Occlusion is scaled by the distance travelled through the block.
                    factor += occlusion * distance;
                    lastHit = result.getHitVec();
                    lastState = ctx.world.getBlockState(result.getPos());
                } else {
                    break;
                }
            }
        }

        return factor;
    }

    private static float calculateWeatherAbsorption(@Nonnull final WorldContext ctx, @Nonnull final Vector3d pt1, @Nonnull final Vector3d pt2) {
        assert ctx.world != null;

        if (!ctx.isPrecipitating)
            return 1F;

        final BlockPos low = new BlockPos(pt1);
        final BlockPos mid = new BlockPos(MathStuff.addScaled(pt1, pt2, 0.5F));
        final BlockPos high = new BlockPos(pt2);

        // Determine the precipitation type at each point
        final Biome.RainType rt1 = WorldUtils.getCurrentPrecipitationAt(ctx.world, low);
        final Biome.RainType rt2 = WorldUtils.getCurrentPrecipitationAt(ctx.world, mid);
        final Biome.RainType rt3 = WorldUtils.getCurrentPrecipitationAt(ctx.world, high);

        // Calculate the impact of weather on dampening
        float factor = calcFactor(rt1, 0.25F);
        factor += calcFactor(rt2, 0.5F);
        factor += calcFactor(rt3, 0.25F);
        factor *= ctx.precipitationStrength;

        return factor;
    }

    @Nonnull
    private static Vector3d surfaceNormal(@Nonnull final Direction d) {
        return SURFACE_DIRECTION_NORMALS[d.ordinal()];
    }

    private static Vector3d offsetPositionIfSolid(@Nonnull final IWorldReader world, @Nonnull final Vector3d origin, @Nonnull final Vector3d target) {
        if (!WorldUtils.isAirBlock(world, new BlockPos(origin))) {
            return MathStuff.addScaled(origin, MathStuff.normalize(origin, target), 0.876F);
        }
        return origin;
    }

    private static float calcFactor(@Nonnull final Biome.RainType type, final float base) {
        return type == Biome.RainType.NONE ? base : base * (type == Biome.RainType.SNOW ? Effects.SNOW_AIR_ABSORPTION_FACTOR : Effects.RAIN_AIR_ABSORPTION_FACTOR);
    }

    private static boolean isMiss(@Nullable final BlockRayTraceResult result) {
        return result == null || result.getType() == RayTraceResult.Type.MISS;
    }

}

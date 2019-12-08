/*
 * Dynamic Surroundings: Sound Control
 * Sound Physics
 * Copyright (C) 2019  OreCruncher
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
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.math.BlockRayTrace;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.math.RayTraceIterator;
import org.orecruncher.sndctrl.audio.handlers.effects.LowPassData;
import org.orecruncher.sndctrl.audio.handlers.effects.SourcePropertyFloat;
import org.orecruncher.sndctrl.xface.IBlockStateEffects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

@OnlyIn(Dist.CLIENT)
public final class SoundFXUtils {

    /**
     * Maximum number of segements to check when ray tracing for occlusion.
     */
    private static final int OCCLUSION_RAYS = 10;
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
    private static final Vec3d[] REVERB_RAY_NORMALS = new Vec3d[REVERB_RAYS];
    /**
     * Precalculated vectors to determine end targets relative to an origin.
     */
    private static final Vec3d[] REVERB_RAY_PROJECTED = new Vec3d[REVERB_RAYS];
    /**
     * Precaluclated direction surface normals as Vec3d instead of Vec3i
     */
    private static final Vec3d[] SURFACE_DIRECTION_NORMALS = new Vec3d[Direction.values().length];

    static {

        // Would have been cool to have a direction vec as a 3d as well as 3i.
        for (final Direction d : Direction.values())
            SURFACE_DIRECTION_NORMALS[d.ordinal()] = new Vec3d(d.getDirectionVec());

        // Pre-calculate the known vectors that will be projected off a sound source when casting about to establish
        // reverb effects.
        for (int i = 0; i < REVERB_RAYS; i++) {
            final double longitude = MathStuff.ANGLE * i;
            final double latitude = Math.asin(((double) i / REVERB_RAYS) * 2.0D - 1.0D);

            REVERB_RAY_NORMALS[i] = new Vec3d(
                    Math.cos(latitude) * Math.cos(longitude),
                    Math.cos(latitude) * Math.sin(longitude),
                    Math.sin(latitude)
            ).normalize();

            REVERB_RAY_PROJECTED[i] = REVERB_RAY_NORMALS[i].scale(MAX_REVERB_DISTANCE);
        }

    }

    public static void calculate(@Nonnull final WorldContext ctx, @Nonnull final SourceContext source) {

        assert ctx.world != null;
        assert ctx.player != null;

        if (ctx.isNotValid()
                || source.isDisabled()
                || !inRange(source.getPosition(), ctx.playerEyePosition, source.getAttenuationDistance())
                || source.getPosition().equals(Vec3d.ZERO)) {
            clearSettings(source);
            return;
        }

        // Need to offset sound toward player if it is in a solid block
        final Vec3d soundPos = offsetPositionIfSolid(ctx.world, source.getPosition(), ctx.playerEyePosition);

        final float absorptionCoeff = Effects.globalBlockAbsorption * 3.0F;
        final float airAbsorptionFactor = calculateWeatherAbsorption(ctx, soundPos, ctx.playerEyePosition);
        final float occlusionAccumulation = calculateOcclusion(ctx, soundPos, ctx.playerEyePosition);

        float directCutoff = (float) MathStuff.exp(-occlusionAccumulation * absorptionCoeff);

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

            Vec3d origin = soundPos;
            Vec3d target = origin.add(REVERB_RAY_PROJECTED[i]);

            final BlockRayTraceResult rayHit = traceContext.trace(origin, target);

            if (isMiss(rayHit))
                continue;

            // Additional bounces
            BlockPos lastHitBlock = rayHit.getPos();
            Vec3d lastHitPos = rayHit.getHitVec();
            Vec3d lastHitNormal = surfaceNormal(rayHit.getFace());
            Vec3d lastRayDir = REVERB_RAY_NORMALS[i];

            double totalRayDistance = origin.distanceTo(rayHit.getHitVec());

            // Secondary ray bounces
            for (int j = 0; j < REVERB_RAY_BOUNCES; j++) {

                final float blockReflectivity = ((IBlockStateEffects) ctx.world.getBlockState(lastHitBlock)).getReflectivity();
                final float energyTowardsPlayer = blockReflectivity * ENERGY_COEFF + ENERGY_CONST;

                final Vec3d newRayDir = MathStuff.reflection(lastRayDir, lastHitNormal);
                origin = MathStuff.addScaled(lastHitPos, newRayDir, 0.01F);
                target = MathStuff.addScaled(origin, newRayDir, MAX_REVERB_DISTANCE);

                final BlockRayTraceResult newRayHit = traceContext.trace(origin, target);

                if (isMiss(newRayHit)) {
                    totalRayDistance += lastHitPos.distanceTo(ctx.playerEyePosition);
                } else {

                    bounceRatio[j] += blockReflectivity;
                    totalRayDistance += lastHitPos.distanceTo(newRayHit.getHitVec());

                    lastHitPos = newRayHit.getHitVec();
                    lastHitNormal = surfaceNormal(newRayHit.getFace());
                    lastRayDir = newRayDir;
                    lastHitBlock = newRayHit.getPos();

                    // Cast one final ray towards the player. If it's unobstructed, then the sound source and the
                    // player share airspace.
                    if (!Effects.simplerSharedAirspaceSimulation || j == REVERB_RAY_BOUNCES - 1) {
                        final Vec3d finalRayStart = MathStuff.addScaled(lastHitPos, lastHitNormal, 0.01F);
                        final BlockRayTraceResult finalRayHit = traceContext.trace(finalRayStart, ctx.playerEyePosition);
                        if (isMiss(finalRayHit)) {
                            sharedAirspace += 1.0F;
                        }
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
                if (isMiss(newRayHit)) {
                    break;
                }
            }
        }

        bounceRatio[0] = bounceRatio[0] / REVERB_RAYS;
        bounceRatio[1] = bounceRatio[1] / REVERB_RAYS;
        bounceRatio[2] = bounceRatio[2] / REVERB_RAYS;
        bounceRatio[3] = bounceRatio[3] / REVERB_RAYS;

        if (Effects.simplerSharedAirspaceSimulation) {
            sharedAirspace *= RECIP_PRIMARY_RAYS * 64F;
        } else {
            sharedAirspace *= RECIP_TOTAL_RAYS * 64F;
        }

        final float sharedAirspaceWeight0 = MathStuff.clamp1(sharedAirspace / 20.0F);
        final float sharedAirspaceWeight1 = MathStuff.clamp1(sharedAirspace / 15.0F);
        final float sharedAirspaceWeight2 = MathStuff.clamp1(sharedAirspace / 10.0F);
        final float sharedAirspaceWeight3 = sharedAirspaceWeight2; //MathStuff.clamp1(sharedAirspace / 10.0F);

        final float sendCoeff = -occlusionAccumulation * absorptionCoeff;
        final float exp1 = (float) MathStuff.exp(sendCoeff * 1.0F);
        final float exp2 = (float) MathStuff.exp(sendCoeff * 1.5F);
        sendCutoff0 = exp1 * (1.0F - sharedAirspaceWeight0) + sharedAirspaceWeight0;
        sendCutoff1 = exp1 * (1.0F - sharedAirspaceWeight1) + sharedAirspaceWeight1;
        sendCutoff2 = exp2 * (1.0F - sharedAirspaceWeight2) + sharedAirspaceWeight2;
        sendCutoff3 = sendCutoff2; //exp2 * (1.0F - sharedAirspaceWeight3) + sharedAirspaceWeight3;

        final float averageSharedAirspace = (sharedAirspaceWeight0 + sharedAirspaceWeight1 + sharedAirspaceWeight2
                + sharedAirspaceWeight3) * 0.25F;
        directCutoff = Math.max((float) Math.sqrt(averageSharedAirspace) * 0.2F, directCutoff);

        float directGain = (float) MathStuff.pow(directCutoff, 0.1);

        sendGain1 *= bounceRatio[1];
        sendGain2 *= (float) MathStuff.pow(bounceRatio[2], 3.0);
        //sendGain3 *= (float) MathStuff.pow(bounceRatio[3], 4.0);

        sendGain0 = MathStuff.clamp1(sendGain0);
        sendGain1 = MathStuff.clamp1(sendGain1);
        sendGain2 = MathStuff.clamp1(sendGain2 * 1.05F - 0.05F);
        //sendGain3 = sendGain2; //MathStuff.clamp1(sendGain3 * 1.05F - 0.05F);

        sendGain0 *= (float) MathStuff.pow(sendCutoff0, 0.1);
        sendGain1 *= (float) MathStuff.pow(sendCutoff1, 0.1);
        sendGain2 *= (float) MathStuff.pow(sendCutoff2, 0.1);
        sendGain3  = sendGain2; //*= (float) MathStuff.pow(sendCutoff3, 0.1);

        if (ctx.player.isInWater()) {
            sendCutoff0 *= 0.4F;
            sendCutoff1 *= 0.4F;
            sendCutoff2 *= 0.4F;
            sendCutoff3 = sendCutoff2; //*= 0.4F;
        }

        final LowPassData lp0 = source.getLowPass0();
        final LowPassData lp1 = source.getLowPass1();
        final LowPassData lp2 = source.getLowPass2();
        final LowPassData lp3 = source.getLowPass3();
        final LowPassData direct = source.getDirect();
        final SourcePropertyFloat prop = source.getAirAbsorb();

        synchronized (source.sync()) {
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

    private static void clearSettings(@Nonnull final SourceContext source) {
        synchronized (source.sync()) {
            source.getLowPass0().setProcess(false);
            source.getLowPass1().setProcess(false);
            source.getLowPass2().setProcess(false);
            source.getLowPass3().setProcess(false);
            source.getDirect().setProcess(false);
            source.getAirAbsorb().setProcess(false);
        }
    }

    private static float calculateOcclusion(@Nonnull final WorldContext ctx, @Nonnull final Vec3d origin, @Nonnull final Vec3d target) {

        assert ctx.world != null;
        assert ctx.player != null;

        float accum = 0F;

        final BlockRayTrace traceContext = new BlockRayTrace(ctx.world, origin, target, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.SOURCE_ONLY);
        final Iterator<BlockRayTraceResult> itr = new RayTraceIterator(traceContext);
        for (int i = 0; i < OCCLUSION_RAYS; i++) {
            if (itr.hasNext()) {
                final BlockState state = ctx.world.getBlockState(itr.next().getPos());
                accum += ((IBlockStateEffects) state).getOcclusion();
            } else {
                break;
            }
        }

        return accum;
    }

    private static float calculateWeatherAbsorption(@Nonnull final WorldContext ctx, @Nonnull final Vec3d pt1, @Nonnull final Vec3d pt2) {
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
    private static Vec3d surfaceNormal(@Nonnull final Direction d) {
        return SURFACE_DIRECTION_NORMALS[d.ordinal()];
    }

    private static Vec3d offsetPositionIfSolid(@Nonnull final World world, @Nonnull final Vec3d origin, @Nonnull final Vec3d target) {
        if (!world.isAirBlock(new BlockPos(origin))) {
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

    private static boolean inRange(@Nonnull final Vec3d origin, @Nonnull final Vec3d target, final double distance) {
        return distance > 0 && origin.squareDistanceTo(target) <= (distance * distance);
    }
}

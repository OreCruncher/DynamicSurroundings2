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

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.orecruncher.lib.RayTraceIterator;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.sndctrl.audio.handlers.effects.LowPassData;
import org.orecruncher.sndctrl.audio.handlers.effects.SourceProperty;
import org.orecruncher.sndctrl.xface.IBlockStateEffects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class SoundFXUtils {

    /**
     * Sound categories that are ignored when determing special effects.
     */
    private static final Set<SoundCategory> IGNORE_CATEGORIES = new ReferenceOpenHashSet<>();
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
     * Reciprical of the total number of rays cast.
     */
    private static final float RECIP_TOTAL_RAYS = 1F / (REVERB_RAYS * REVERB_RAY_BOUNCES);
    /**
     * Reciprical of the total primary rays.
     */
    private static final float RECIP_PRIMARY_RAYS = 1F / REVERB_RAYS;
    /**
     * Normals for the direction of each of the rays to be cast.
     */
    private static final Vec3d[] REVERB_RAY_NORMALS = new Vec3d[REVERB_RAYS];
    /**
     * Precalculated vectors to determine end targets relative to an origin.
     */
    private static final Vec3d[] REVERB_RAY_PROJECTED = new Vec3d[REVERB_RAYS];

    static {
        IGNORE_CATEGORIES.add(SoundCategory.WEATHER);
        IGNORE_CATEGORIES.add(SoundCategory.RECORDS);
        IGNORE_CATEGORIES.add(SoundCategory.MUSIC);
        IGNORE_CATEGORIES.add(SoundCategory.MASTER);

        // Pre-calculate the known vectors that will be projected off a sound source when casting about to establish
        // reverb effects.
        for (int i = 0; i < REVERB_RAYS; i++) {
            final float longitude = MathStuff.ANGLE * (float) i;
            final float latitude = (float) Math.asin(((float) i / REVERB_RAYS) * 2.0F - 1.0F);

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

        if (ctx.isNotValid() || source.isDisabled() || IGNORE_CATEGORIES.contains(source.getCategory()) || source.getPosition().equals(Vec3d.ZERO)) {
            clearSettings(source);
            return;
        }

        final float absorptionCoeff = Effects.globalBlockAbsorption * 3.0f;

        float airAbsorptionFactor = 1.0f;

        if (ctx.isPrecipitating) {
            airAbsorptionFactor = calculateWeatherAbsorption(ctx, ctx.playerEyePosition, source.getPosition());
        }

        final float occlusionAccumulation = calculateOcclusion(ctx, ctx.playerEyePosition, source.getPosition());

        float directCutoff = (float) Math.exp(-occlusionAccumulation * absorptionCoeff);

        // Handle any dampening effects from the player - like head in water
        directCutoff *= 1F - ctx.auralDampening;

        // Calculate reverb parameters for this sound
        float sendGain0 = 0F;
        float sendGain1 = 0F;
        float sendGain2 = 0F;
        float sendGain3 = 0F;

        float sendCutoff0 = 1F;
        float sendCutoff1 = 1F;
        float sendCutoff2 = 1F;
        float sendCutoff3 = 1F;

        // Shoot rays around sound
        final float[] bounceRatio = new float[REVERB_RAY_BOUNCES];

        float sharedAirspace = 0F;

        for (int i = 0; i < REVERB_RAYS; i++) {

            Vec3d origin = source.getPosition();
            Vec3d target = origin.add(REVERB_RAY_PROJECTED[i]);

            final BlockRayTraceResult rayHit = ctx.rayTraceBlocks(origin, target, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.SOURCE_ONLY);

            if (isMiss(rayHit))
                continue;

            // Additional bounces
            BlockPos lastHitBlock = rayHit.getPos();
            Vec3d lastHitPos = rayHit.getHitVec();
            Vec3d lastHitNormal = new Vec3d(rayHit.getFace().getDirectionVec());
            Vec3d lastRayDir = REVERB_RAY_NORMALS[i];

            double totalRayDistance = origin.distanceTo(rayHit.getHitVec());

            // Secondary ray bounces
            for (int j = 0; j < REVERB_RAY_BOUNCES; j++) {

                final float blockReflectivity = ((IBlockStateEffects) ctx.world.getBlockState(lastHitBlock)).getReflectivity();
                final float energyTowardsPlayer = (blockReflectivity * 0.75f + 0.25f) * 0.25F * RECIP_TOTAL_RAYS;

                final Vec3d newRayDir = MathStuff.reflection(lastRayDir, lastHitNormal);
                origin = lastHitPos.add(newRayDir.scale(0.01F));
                target = origin.add(newRayDir.scale(MAX_REVERB_DISTANCE));

                final BlockRayTraceResult newRayHit = ctx.rayTraceBlocks(origin, target, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.SOURCE_ONLY);

                if (isMiss(newRayHit)) {
                    totalRayDistance += lastHitPos.distanceTo(ctx.playerEyePosition);
                } else {

                    bounceRatio[j] += blockReflectivity;
                    totalRayDistance += lastHitPos.distanceTo(newRayHit.getHitVec());

                    lastHitPos = newRayHit.getHitVec();
                    lastHitNormal = new Vec3d(newRayHit.getFace().getDirectionVec());
                    lastRayDir = newRayDir;
                    lastHitBlock = newRayHit.getPos();

                    // Cast one final ray towards the player. If it's unobstructed, then the sound source and the
                    // player share airspace.
                    if (!Effects.simplerSharedAirspaceSimulation || j == REVERB_RAY_BOUNCES - 1) {
                        final Vec3d finalRayStart = lastHitPos.add(lastHitNormal.scale(0.01F));
                        final BlockRayTraceResult finalRayHit = ctx.rayTraceBlocks(finalRayStart, ctx.playerEyePosition, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.SOURCE_ONLY);
                        if (isMiss(finalRayHit)) {
                            sharedAirspace += 1.0f;
                        }
                    }
                }

                final float reflectionDelay = (float) Math.max(totalRayDistance, 0.0) * 0.12f * blockReflectivity;

                final float cross0 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 0.0f), 0.0f, 1.0f);
                final float cross1 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 1.0f), 0.0f, 1.0f);
                final float cross2 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 2.0f), 0.0f, 1.0f);
                final float cross3 = MathHelper.clamp(reflectionDelay - 2.0f, 0.0f, 1.0f);

                sendGain0 += cross0 * energyTowardsPlayer * 6.4f;
                sendGain1 += cross1 * energyTowardsPlayer * 12.8f;
                sendGain2 += cross2 * energyTowardsPlayer * 12.8f;
                sendGain3 += cross3 * energyTowardsPlayer * 12.8f;

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

        sharedAirspace *= 64.0f;

        if (Effects.simplerSharedAirspaceSimulation) {
            sharedAirspace *= RECIP_PRIMARY_RAYS;
        } else {
            sharedAirspace *= RECIP_TOTAL_RAYS;
        }

        final float sharedAirspaceWeight0 = MathHelper.clamp(sharedAirspace / 20.0f, 0.0f, 1.0f);
        final float sharedAirspaceWeight1 = MathHelper.clamp(sharedAirspace / 15.0f, 0.0f, 1.0f);
        final float sharedAirspaceWeight2 = MathHelper.clamp(sharedAirspace / 10.0f, 0.0f, 1.0f);
        final float sharedAirspaceWeight3 = MathHelper.clamp(sharedAirspace / 10.0f, 0.0f, 1.0f);

        sendCutoff0 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.0f) * (1.0f - sharedAirspaceWeight0)
                + sharedAirspaceWeight0;
        sendCutoff1 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.0f) * (1.0f - sharedAirspaceWeight1)
                + sharedAirspaceWeight1;
        sendCutoff2 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.5f) * (1.0f - sharedAirspaceWeight2)
                + sharedAirspaceWeight2;
        sendCutoff3 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.5f) * (1.0f - sharedAirspaceWeight3)
                + sharedAirspaceWeight3;

        // attempt to preserve directionality when airspace is shared by
        // allowing some of the dry signal through but filtered
        final float averageSharedAirspace = (sharedAirspaceWeight0 + sharedAirspaceWeight1 + sharedAirspaceWeight2
                + sharedAirspaceWeight3) * 0.25f;
        directCutoff = Math.max((float) Math.pow(averageSharedAirspace, 0.5) * 0.2f, directCutoff);

        float directGain = (float) Math.pow(directCutoff, 0.1);

        sendGain1 *= bounceRatio[1];
        sendGain2 *= (float) Math.pow(bounceRatio[2], 3.0);
        sendGain3 *= (float) Math.pow(bounceRatio[3], 4.0);

        sendGain0 = MathHelper.clamp(sendGain0, 0.0f, 1.0f);
        sendGain1 = MathHelper.clamp(sendGain1, 0.0f, 1.0f);
        sendGain2 = MathHelper.clamp(sendGain2 * 1.05f - 0.05f, 0.0f, 1.0f);
        sendGain3 = MathHelper.clamp(sendGain3 * 1.05f - 0.05f, 0.0f, 1.0f);

        sendGain0 *= (float) Math.pow(sendCutoff0, 0.1);
        sendGain1 *= (float) Math.pow(sendCutoff1, 0.1);
        sendGain2 *= (float) Math.pow(sendCutoff2, 0.1);
        sendGain3 *= (float) Math.pow(sendCutoff3, 0.1);

        if (ctx.player.isInWater()) {
            sendCutoff0 *= 0.4f;
            sendCutoff1 *= 0.4f;
            sendCutoff2 *= 0.4f;
            sendCutoff3 *= 0.4f;
        }

        final LowPassData lp0 = source.getLowPass0();
        final LowPassData lp1 = source.getLowPass1();
        final LowPassData lp2 = source.getLowPass2();
        final LowPassData lp3 = source.getLowPass3();
        final LowPassData direct = source.getDirect();
        final SourceProperty prop = source.getAirAbsorb();

        synchronized (lp0.sync()) {
            lp0.gain = sendGain0;
            lp0.gainHF = sendCutoff0;
            lp0.setProcess(true);
        }

        synchronized (lp1.sync()) {
            lp1.gain = sendGain1;
            lp1.gainHF = sendCutoff1;
            lp1.setProcess(true);
        }

        synchronized (lp2.sync()) {
            lp2.gain = sendGain2;
            lp2.gainHF = sendCutoff2;
            lp2.setProcess(true);
        }

        synchronized (lp3.sync()) {
            lp3.gain = sendGain3;
            lp3.gainHF = sendCutoff3;
            lp3.setProcess(true);
        }

        synchronized (direct.sync()) {
            direct.gain = directGain;
            direct.gainHF = directCutoff;
            direct.setProcess(true);
        }

        synchronized (prop.sync()) {
            prop.setValue(airAbsorptionFactor);
            prop.setProcess(true);
        }
    }

    private static void clearSettings(@Nonnull final SourceContext source) {
        source.getLowPass0().setProcess(false);
        source.getLowPass1().setProcess(false);
        source.getLowPass2().setProcess(false);
        source.getLowPass3().setProcess(false);
        source.getDirect().setProcess(false);
        source.getAirAbsorb().setProcess(false);
        /*
        source.getLowPass0().gain = 0F;
        source.getLowPass0().gainHF = 1F;
        source.getLowPass1().gain = 0F;
        source.getLowPass1().gainHF = 1F;
        source.getLowPass2().gain = 0F;
        source.getLowPass2().gainHF = 1F;
        source.getLowPass3().gain = 0F;
        source.getLowPass3().gain = 1F;
        source.getDirect().gain = 1F;
        source.getDirect().gainHF = 1F;
        source.getAirAbsorb().setValue(1F);
         */
    }

    private static float calculateOcclusion(@Nonnull final WorldContext ctx, @Nonnull final Vec3d source, @Nonnull final Vec3d listener) {

        assert ctx.world != null;
        assert ctx.player != null;

        Vec3d origin = source;

        // The origin may be inside a normal block throwing off the ray cast a bit.  If the source is in a non-air
        // block offset toward the listener by half a block.
        if (!ctx.world.isAirBlock(new BlockPos(origin))) {
            final Vec3d normal = listener.subtract(source).normalize();
            origin = origin.add(normal.scale(0.5F));
        }

        float accum = 0F;

        final Iterator<Pair<BlockPos, BlockState>> itr = new RayTraceIterator(ctx.world, origin, listener, ctx.player);
        for (int i = 0; i < OCCLUSION_RAYS; i++) {
            if (itr.hasNext()) {
                accum += ((IBlockStateEffects) itr.next().getValue()).getOcclusion();
            } else {
                break;
            }
        }

        return accum;
    }

    private static float calculateWeatherAbsorption(@Nonnull final WorldContext ctx, @Nonnull final Vec3d pt1, @Nonnull final Vec3d pt2) {
        assert ctx.world != null;

        final BlockPos low = new BlockPos(pt1);
        final BlockPos mid = new BlockPos(pt1.add(pt2).scale(0.5F));
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

    private static float calcFactor(@Nonnull Biome.RainType type, final float base) {
        return type == Biome.RainType.NONE ? base : base * (type == Biome.RainType.SNOW ? Effects.SNOW_AIR_ABSORPTION_FACTOR : Effects.RAIN_AIR_ABSORPTION_FACTOR);
    }

    private static boolean isMiss(@Nullable final BlockRayTraceResult result) {
        return result == null || result.getType() == RayTraceResult.Type.MISS;
    }
}

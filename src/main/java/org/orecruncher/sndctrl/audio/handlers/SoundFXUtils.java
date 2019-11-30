/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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
 */

package org.orecruncher.sndctrl.audio.handlers;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
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
import org.orecruncher.sndctrl.audio.handlers.effects.ReverbData;
import org.orecruncher.sndctrl.audio.handlers.effects.SourceProperty;
import org.orecruncher.sndctrl.xface.IBlockStateEffects;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class SoundFXUtils {

    /**
     * Maximum number of segements to check when ray tracing.
     */
    private static final int MAX_SEGMENTS = 10;
    /**
     * Dampening effect of snow fall
     */
    private static final float SNOW_FACTOR = 5F;
    /**
     * Dampening effect of rainfall
     */
    private static final float RAIN_FACTOR = 2F;

    /**
     * Number of rays to project when doing reverb calculations.
     */
    private static final int REVERB_RAYS = 256;

    /**
     * Maximum distance to trace a reverb ray segment before stopping.
     */
    private static final float MAX_REVERB_DISTANCE = 16;

    /**
     * dY value that is considered to be a sky projecting ray
     */
    private static final double REVERB_RAY_SKY_ELEVATION = 0.5D;

    /**
     * Normals for the direction of each of the rays to be cast.
     */
    private static final Vec3d[] REVERB_RAY_NORMALS = new Vec3d[REVERB_RAYS];

    /**
     * Precalculated vectors to determine end targets relative to an origin.
     */
    private static final Vec3d[] REVERB_RAY_PROJECTED = new Vec3d[REVERB_RAYS];

    /**
     * Whether a given reverb ray is projected skyward
     */
    private static final boolean[] REVERB_RAY_SKY = new boolean[REVERB_RAYS];

    /**
     * Maxium possible vectors that are considered sky facing.
     */
    private static final int REVERB_RAY_SKY_MAX;

    static {
        // Pre-calculate the known vectors that will be projected off a sound source when casting about to establish
        // reverb effects.
        int reverbSkyTotal = 0;
        for (int i = 0; i < REVERB_RAYS; i++) {
            final float longitude = MathStuff.ANGLE * (float) i;
            final float latitude = (float) Math.asin(((float) i / REVERB_RAYS) * 2.0F - 1.0F);

            REVERB_RAY_NORMALS[i] = new Vec3d(
                    Math.cos(latitude) * Math.cos(longitude),
                    Math.cos(latitude) * Math.sin(longitude),
                    Math.sin(latitude)
            ).normalize();

            REVERB_RAY_SKY[i] = Double.compare(REVERB_RAY_NORMALS[i].getY(), REVERB_RAY_SKY_ELEVATION) >= 0;
            if (REVERB_RAY_SKY[i])
                reverbSkyTotal++;

            REVERB_RAY_PROJECTED[i] = REVERB_RAY_NORMALS[i].scale(MAX_REVERB_DISTANCE);
        }

        REVERB_RAY_SKY_MAX = reverbSkyTotal;
    }

    /**
     * Calculates the occlusion for a sound source for the WorldContext listener (player).  Takes into account the
     * blocks between the player and the sound source, as well as any other effects related to the player head being
     * in a liquid of some sort.
     *
     * @param effectData Effect data reference to receive the calculations
     * @param ctx        WorldContext containing data about the world
     * @param handler    Sound handler that maintains sound state
     */
    public static void calculateOcclusion(@Nonnull final LowPassData effectData, @Nonnull final WorldContext ctx, @Nonnull final SourceContext handler) {

        if (ctx.isNotValid() || handler.getPosition().equals(Vec3d.ZERO)) {
            synchronized (effectData.sync()) {
                effectData.setProcess(false);
            }
            return;
        }

        assert ctx.world != null;
        assert ctx.player != null;

        final Vec3d listener = ctx.playerEyePosition;
        final Vec3d source = handler.getPosition();

        Vec3d origin = source;

        // The origin may be inside a normal block throwing off the ray cast a bit.  If the source is in a non-air
        // block offset toward the listener by half a block.
        if (!ctx.world.isAirBlock(new BlockPos(origin))) {
            final Vec3d normal = listener.subtract(source).normalize();
            origin = origin.add(normal.scale(0.5F));
        }

        float accum = 0F;

        final Iterator<Pair<BlockPos, BlockState>> itr = new RayTraceIterator(ctx.world, origin, listener, ctx.player);
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            if (itr.hasNext()) {
                accum += ((IBlockStateEffects) itr.next()).getOcclusion();
                if (accum > 0.98) {
                    accum = 0.98F;
                    break;
                }
            } else {
                break;
            }
        }

        synchronized (effectData.sync()) {
            // Make the final calculations based on our factors
            effectData.gain = (1F - accum) * ctx.lowPassData.gain;
            effectData.gainHF = (1F - MathHelper.sqrt(accum)) * ctx.lowPassData.gainHF;
            effectData.setProcess(true);
        }
    }

    /**
     * Calculates the reverb properties of the location around the listener.
     *
     * @param effectData Effect data reference to recieve the calculations
     * @param ctx        WorldContext containing data about the world
     */
    public static void calculateReverb(@Nonnull final ReverbData effectData, @Nonnull final WorldContext ctx) {
        if (ctx.isNotValid()) {
            synchronized (effectData.sync()) {
                effectData.setProcess(false);
            }
            return;
        }

        assert ctx.world != null;
        assert ctx.player != null;

        final Vec3d origin = ctx.playerEyePosition;

        int hits = 0;
        int misses = 0;
        int sky = 0;
        float lf = 0;
        float mf = 0;
        float hf = 0;

        Set<BlockPos> visited = new ObjectOpenHashSet<>();

        // Going to cast out rays around the source looking for relfected surfaces and the like.  The rays should be
        // evenly distributed around the source.
        for (int i = 0; i < REVERB_RAYS; i++) {

            // Establish the initial vectors for doing a ray trace
            final Vec3d target = REVERB_RAY_PROJECTED[i].add(origin);

            // Loop through the blocks along the ray until a block that is solid is encountered.
            boolean blocked = false;
            final Iterator<Pair<BlockPos, BlockState>> itr = new RayTraceIterator(ctx.world, origin, target, ctx.player);
            while (itr.hasNext()) {
                final Pair<BlockPos, BlockState> hit = itr.next();
                if (visited.contains(hit.getKey())) {
                    // Already processes this block - skip it
                    break;
                }
                visited.add(hit.getKey());
                final IBlockStateEffects effects = (IBlockStateEffects) hit.getValue();
                lf += effects.getLowFrequencyReflect();
                mf += effects.getMidFrequencyReflect();
                hf += effects.getHighFrequencyReflect();
                if (hit.getValue().getMaterial().blocksMovement()) {
                    break;
                }
            }

            if (blocked) {
                hits++;
            } else {
                misses++;
                if (REVERB_RAY_SKY[i])
                    sky++;
            }
        }

        final float den = lf + mf + hf;
        final float decayFactor = den > 0 ? MathStuff.clamp((hf - lf) / den, 0F, 1F) : 0;

        // A sense of how enclosed the room is around the listener.  0 is surround by air blocks, 1 means enclosed
        // with surfaces.  For example, standing on a super flat world non-sneaking would have a ratio of 0.453,
        // whereas if the player were sneaking it would be 0.456 (assuming 128 rays were cast).
        final float enclosureRatio = (float) (REVERB_RAYS - misses) / REVERB_RAYS;

        // skyRatio gives an indication of whether the listener is in a place where there is open sky.  The higher
        // the ration the more open the sky.  For example, standing on a super flat world without any structures
        // nearby this value would be 1.  If there is a roof above the players head it would be near 0.
        final float skyRatio = (float) sky / REVERB_RAY_SKY_MAX;

        // Scaling factor to apply to reverb strength calculations.  Idea here is that in open areas reverb would
        // be lessened.
        final float skyScale = 1F - skyRatio;

        // Ratio of the number of blocks involved vs the number of rays cast.  Multiple rays could have hit a single
        // block.  This can happen if the listener is closer to blocks, like a wall or to the ground.
        final float blockRatio = (float) hits / REVERB_RAYS;

        // roomSize gives a sense of the volume of the area the listener is in.  Values toward 0 suggest a small
        // room, whereas values toward 1 suggest a larger room.
        final float roomSize = blockRatio * enclosureRatio;

        // Calculate the base impact on gain.  It is derived from the reverb strength and the number of blocks
        // involved in creating that strength.
        final float gainFactor = roomSize * skyScale;

        final float decayTime = 8F * decayFactor * skyScale;
        final float reflectionsGain = gainFactor * 0.05F; //3.16F;
        final float reflectionsDelay = roomSize * 0.03F;
        final float lateReverbGain = gainFactor * 0.01F; // 10F;
        final float lateReverbDelay = roomSize * 0.1F;

        synchronized (effectData.sync()) {
            effectData.decayTime = decayTime;
            effectData.reflectionsGain = reflectionsGain;
            effectData.reflectionsDelay = reflectionsDelay;
            effectData.lateReverbGain = lateReverbGain;
            effectData.lateReverbDelay = lateReverbDelay;
            effectData.setProcess(decayFactor > 0 && (reflectionsGain > 0 || lateReverbGain > 0));
        }
    }

    /**
     * Calculates the impact of weather as it relates to sound absorption.
     *
     * @param prop    Property to receive the calculated value
     * @param ctx     WorldContext containing data about the world
     * @param handler Sound handler that maintains sound state
     */
    public static void calculateWeatherFactor(@Nonnull final SourceProperty prop, @Nonnull final WorldContext ctx, @Nonnull final SourceContext handler) {
        if (ctx.isNotValid() || !ctx.isPrecipitating || handler.getCategory() == SoundCategory.WEATHER || handler.getPosition().equals(Vec3d.ZERO)) {
            synchronized (prop.sync()) {
                prop.setProcess(false);
            }
            return;
        }

        assert ctx.world != null;

        // Pick 3 points to evaluate along the line between player and source
        final BlockPos pos1 = ctx.playerPos;
        final BlockPos pos2 = new BlockPos(ctx.playerEyePosition.add(handler.getPosition()).scale(0.5F));
        final BlockPos pos3 = new BlockPos(handler.getPosition());

        // Determine the precipitation type at each point
        final Biome.RainType rt1 = WorldUtils.getCurrentPrecipitationAt(ctx.world, pos1);
        final Biome.RainType rt2 = WorldUtils.getCurrentPrecipitationAt(ctx.world, pos2);
        final Biome.RainType rt3 = WorldUtils.getCurrentPrecipitationAt(ctx.world, pos3);

        // Calculate the impact of weather on dampening
        float factor = calcFactor(rt1, 0.25F);
        factor += calcFactor(rt2, 0.5F);
        factor += calcFactor(rt3, 0.25F);
        factor *= ctx.precipitationStrength;

        // Set and go!
        synchronized (prop.sync()) {
            prop.setValue(Math.max(factor, 1F));
            prop.setProcess(true);
        }
    }

    private static float calcFactor(@Nonnull Biome.RainType type, final float base) {
        return type == Biome.RainType.NONE ? base : base * (type == Biome.RainType.SNOW ? SNOW_FACTOR : RAIN_FACTOR);
    }

}

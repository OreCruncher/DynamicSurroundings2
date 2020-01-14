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

package org.orecruncher.lib;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import org.orecruncher.sndctrl.misc.ModEnvironment;
import sereneseasons.season.SeasonHooks;

import javax.annotation.Nonnull;

public final class WorldUtils {

    /**
     * Temperatures LESS than this value are considered cold temperatures.
     */
    public static final float COLD_THRESHOLD = 0.2F;

    /**
     * Temperatures LESS than this value are considered cold enough for snow.
     */
    public static final float SNOW_THRESHOLD = 0.15F;

    /**
     * SereneSeasons support to obtain the temperature at a specific block location.
     */
    private interface ITemperatureHandler {
        float getTemp(@Nonnull final World world, @Nonnull final BlockPos pos);
    }

    private static final ITemperatureHandler TEMP;

    static {
        if (ModEnvironment.SereneSeasons.isLoaded())
            TEMP = (world, pos) -> SeasonHooks.getBiomeTemperature(world, world.getBiome(pos), pos);
        else
            TEMP = (world, pos) -> world.getBiome(pos).getTemperature(pos);
    }

    private WorldUtils() {

    }

    @Nonnull
    public static BlockPos getTopSolidOrLiquidBlock(@Nonnull final IWorldReader world, @Nonnull final BlockPos pos) {
        return new BlockPos(pos.getX(), world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()), pos.getZ());
    }

    /**
     * Gets the temperature value for the given BlockPos in the world.
     */
    public static float getTemperatureAt(@Nonnull final World world, @Nonnull final BlockPos pos) {
        return TEMP.getTemp(world, pos);
    }

    /**
     * Determines if the temperature value is considered a cold temperature.
     */
    public static boolean isColdTemperature(final float temp) {
        return temp < COLD_THRESHOLD;
    }

    /**
     * Determines if the temperature value is considered cold enough for snow.
     */
    public static boolean isSnowTemperature(final float temp) {
        return temp < SNOW_THRESHOLD;
    }

    /**
     * Determines if the side of the block at the specified position is considered solid.
     */
    public static boolean isSolid(@Nonnull final IWorldReader world, @Nonnull final BlockPos pos, @Nonnull final Direction dir) {
        final BlockState state = world.getBlockState(pos);
        return Block.doesSideFillSquare(state.getCollisionShape(world, pos, ISelectionContext.dummy()),dir);
    }

    /**
     * Determines if the top side of the block at the specified position is considered solid.
     */
    public static boolean isTopSolid(@Nonnull final IWorldReader world, @Nonnull final BlockPos pos) {
        return isSolid(world, pos, Direction.UP);
    }

    /**
     * Determines if the block at the specified location is solid.
     */
    public static boolean isBlockSolid(@Nonnull final IWorldReader world, @Nonnull final BlockPos pos) {
        final BlockState state = world.getBlockState(pos);
        return state.isSolid();
    }

    /**
     * Gets the precipitation currently falling at the specified location.  It takes into account temperature and the
     * like.
     */
    public static Biome.RainType getCurrentPrecipitationAt(@Nonnull final IWorldReader world, @Nonnull final BlockPos pos) {
        if (!((World) world).isRaining()) {
            // Not currently raining
            return Biome.RainType.NONE;
        }

        final Biome biome = world.getBiome(pos);

        // If the biome has no rain...
        if (biome.getPrecipitation() == Biome.RainType.NONE)
            return Biome.RainType.NONE;

        // Is there a block above that is blocking the rainfall?
        final BlockPos p = getPrecipitationHeight(world, pos);
        if (p.getY() > pos.getY()) {
            return Biome.RainType.NONE;
        }

        // Use the temperature of the biome to get whether it is raining or snowing
        final float temp = getTemperatureAt((World) world, pos);
        return isSnowTemperature(temp) ? Biome.RainType.SNOW : Biome.RainType.RAIN;
    }

    @Nonnull
    public static BlockPos getPrecipitationHeight(@Nonnull final IWorldReader world, @Nonnull final BlockPos pos) {
        return world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos);
    }

}

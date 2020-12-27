/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2020  OreCruncher
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
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
//import sereneseasons.season.SeasonHooks;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
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

    /**
     * Weather support to obtain current rain/thunder strength client side
     */
    private interface IWeatherStrength {
        float getStrength(@Nonnull final World world, final float partialTicks);
    }

    /**
     * Weather support to determine if an aspect of weather is occuring.
     */
    private interface IWeatherAspect {
        boolean isOccuring(@Nonnull final World world);
    }

    private static final ITemperatureHandler TEMP;
    private static final IWeatherStrength RAIN_STRENGTH;
    private static final IWeatherStrength THUNDER_STRENGTH;
    private static final IWeatherAspect RAIN_OCCURING;
    private static final IWeatherAspect THUNDER_OCCURING;

    static {
        ITemperatureHandler TEMP1;
        if (ModEnvironment.SereneSeasons.isLoaded()) {
            try {
                final Class<?> clazz = Class.forName("sereneseasons.season.SeasonHooks");
                final Method method = clazz.getMethod("getBiomeTemperature", World.class, Biome.class, BlockPos.class);
                TEMP1 = (world, pos) -> {
                    try {
                        return (float)method.invoke(null, world, world.getBiome(pos), pos);
                    } catch(@Nonnull final Throwable t) {
                        return world.getBiome(pos).getTemperature(pos);
                    }
                };
                Lib.LOGGER.info("Hooked SereneSeasons getBiomeTemperature()");
            } catch(@Nonnull final Throwable t) {
                Lib.LOGGER.warn("Unable to hook SereneSeasons getBiomeTemperature()!");
                TEMP1 = (world, pos) -> world.getBiome(pos).getTemperature(pos);
            }
        } else {
            TEMP1 = (world, pos) -> world.getBiome(pos).getTemperature(pos);
        }

        TEMP = TEMP1;

        // Place holder for future
        RAIN_STRENGTH = World::getRainStrength;
        RAIN_OCCURING = World::isRaining;
        THUNDER_STRENGTH = World::getThunderStrength;
        THUNDER_OCCURING = World::isThundering;
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
    public static boolean isSolid(@Nonnull final IBlockReader world, @Nonnull final BlockPos pos, @Nonnull final Direction dir) {
        final BlockState state = world.getBlockState(pos);
        return Block.doesSideFillSquare(state.getCollisionShape(world, pos, ISelectionContext.dummy()),dir);
    }

    /**
     * Determines if the top side of the block at the specified position is considered solid.
     */
    public static boolean isTopSolid(@Nonnull final IBlockReader world, @Nonnull final BlockPos pos) {
        return isSolid(world, pos, Direction.UP);
    }

    /**
     * Determines if the block at the specified location is solid.
     */
    public static boolean isBlockSolid(@Nonnull final IBlockReader world, @Nonnull final BlockPos pos) {
        final BlockState state = world.getBlockState(pos);
        return state.isSolid();
    }

    /**
     * Determines if the block at the specified location is an air block.
     */
    public static boolean isAirBlock(@Nonnull final IBlockReader world, @Nonnull final BlockPos pos) {
        return isAirBlock(world.getBlockState(pos));
    }

    /**
     * Determines if the BlockState reference is an air block.
     */
    public static boolean isAirBlock(@Nonnull final BlockState state) {
        return state.getMaterial() == Material.AIR;
    }

    /**
     * Gets the precipitation currently falling at the specified location.  It takes into account temperature and the
     * like.
     */
    public static Biome.RainType getCurrentPrecipitationAt(@Nonnull final IWorldReader world, @Nonnull final BlockPos pos) {
        if (!(world instanceof World) || !isRaining((World) world)) {
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

    public static float getRainStrength(@Nonnull final World world, final float partialTicks) {
        return RAIN_STRENGTH.getStrength(world, partialTicks);
    }

    public static float getThunderStrength(@Nonnull final World world, final float partialTicks) {
        return THUNDER_STRENGTH.getStrength(world, partialTicks);
    }

    public static boolean isRaining(@Nonnull final World world) {
        return RAIN_OCCURING.isOccuring(world);
    }

    public static boolean isThundering(@Nonnull final World world) {
        return THUNDER_OCCURING.isOccuring(world);
    }

    @Nonnull
    public static BlockPos getPrecipitationHeight(@Nonnull final IWorldReader world, @Nonnull final BlockPos pos) {
        return world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos);
    }

    public static boolean hasVoidPartiles(@Nonnull final World world) {
        return world.getDimensionType().hasSkyLight();
    }

}

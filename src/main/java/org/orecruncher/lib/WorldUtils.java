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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import org.orecruncher.sndctrl.misc.ModEnvironment;
import sereneseasons.season.SeasonHooks;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public final class WorldUtils {

    private static final BiFunction<World, BlockPos, Float> TEMP;

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

    public static float getTemperatureAt(@Nonnull final World world, @Nonnull final BlockPos pos) {
        return TEMP.apply(world, pos);
    }

    /**
     * Gets the precipitation currently falling at the specified location.  It takes into account temperature and the
     * like.
     *
     * @param world World where the location is being checked
     * @param pos   Position to check
     * @return Enum value indicating the precipitation that is currently falling.
     */
    public static Biome.RainType getCurrentPrecipitationAt(@Nonnull final World world, @Nonnull final BlockPos pos) {
        if (!world.isRaining()) {
            // Not currently raining
            return Biome.RainType.NONE;
        }

        final Biome biome = world.getBiome(pos);

        // If the biome has no rain...
        if (biome.getPrecipitation() == Biome.RainType.NONE)
            return Biome.RainType.NONE;

        // Is there a block above that is blocking the rainfall?
        final BlockPos p = world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos);
        if (p.getY() > pos.getY()) {
            return Biome.RainType.NONE;
        }

        // Use the temperature of the biome to get whether it is raining or snowing
        final float temp = getTemperatureAt(world, pos);
        return temp < 0.15F ? Biome.RainType.SNOW : Biome.RainType.RAIN;
    }

}

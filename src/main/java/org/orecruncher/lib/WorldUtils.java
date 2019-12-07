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

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public final class WorldUtils {

    private WorldUtils() {

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
            return Biome.RainType.NONE;
        } else if (!world.isSkyLightMax(pos)) {
            return Biome.RainType.NONE;
        } else if (world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos).getY() > pos.getY()) {
            return Biome.RainType.NONE;
        }

        final Biome biome = world.getBiome(pos);
        return biome.doesWaterFreeze(world, pos) ? Biome.RainType.SNOW : Biome.RainType.RAIN;
    }

    @Nonnull
    public static BlockRayTraceResult rayTraceBlock(@Nonnull final IBlockReader world, @Nonnull final Vec3d source, @Nonnull final Vec3d dest) {
        return rayTraceBlock(world, source, dest, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.SOURCE_ONLY);
    }

    @Nonnull
    public static BlockRayTraceResult rayTraceBlock(@Nonnull final IBlockReader world, @Nonnull final Vec3d source, @Nonnull final Vec3d dest, RayTraceContext.BlockMode bm, RayTraceContext.FluidMode fm) {
        final RayTraceContext ctx = new RayTraceContext(source, dest, bm, fm, DummyEntity.INSTANCE);
        return world.rayTraceBlocks(ctx);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isInGame() {
        return Minecraft.getInstance().world != null && Minecraft.getInstance().player != null;
    }
}

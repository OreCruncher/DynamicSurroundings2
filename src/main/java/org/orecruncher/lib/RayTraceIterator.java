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

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockReader;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class RayTraceIterator implements Iterator<Pair<BlockPos, BlockState>> {

    @Nonnull
    private final IBlockReader world;
    @Nonnull
    private final PlayerEntity player;
    @Nonnull
    private final BlockPos targetBlock;
    @Nonnull
    private final Vec3d normal;
    @Nonnull
    private Vec3d origin;
    @Nonnull
    private Vec3d target;

    @Nullable
    private BlockRayTraceResult result;

    public RayTraceIterator(@Nonnull final IBlockReader world, @Nonnull final Vec3d origin, @Nonnull final Vec3d target, @Nonnull final PlayerEntity player) {
        this.world = world;
        this.player = player;
        this.targetBlock = new BlockPos(target);
        this.normal = target.subtract(origin).normalize();
        this.origin = origin;
        this.target = target;
        doTrace();
    }

    private void doTrace() {
        if (this.result != null && this.result.getPos().equals(this.targetBlock)) {
            this.result = null;
        } else {
            this.result = WorldUtils.rayTraceBlock(this.world, this.origin, this.target, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.SOURCE_ONLY, this.player);
        }
    }

    @Override
    public boolean hasNext() {
        return this.result != null && this.result.getType() != RayTraceResult.Type.MISS;
    }

    @Override
    @Nonnull
    public Pair<BlockPos, BlockState> next() {
        assert this.result != null;
        if (this.result.getType() == RayTraceResult.Type.MISS)
            throw new IllegalStateException("No more blocks in trace");
        final BlockPos pos = this.result.getPos();
        final BlockState state = this.world.getBlockState(pos);
        this.origin = this.result.getHitVec().add(this.normal);
        doTrace();
        return Pair.of(this.result.getPos(), state);
    }
}

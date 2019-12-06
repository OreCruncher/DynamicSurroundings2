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

import net.minecraft.util.math.*;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class RayTraceIterator implements Iterator<BlockRayTraceResult> {

    @Nonnull
    private final IBlockReader world;
    @Nonnull
    private final BlockPos targetBlock;
    @Nonnull
    private final Vec3d normal;
    @Nonnull
    private Vec3d origin;
    @Nonnull
    private Vec3d target;

    @Nullable
    private BlockRayTraceResult hitResult;

    public RayTraceIterator(@Nonnull final IBlockReader world, @Nonnull final Vec3d origin, @Nonnull final Vec3d target) {
        this.world = world;
        this.targetBlock = new BlockPos(target);
        this.normal = target.subtract(origin).normalize();
        this.origin = origin;
        this.target = target;
        doTrace();
    }

    private void doTrace() {
        if (this.hitResult != null && this.hitResult.getPos().equals(this.targetBlock)) {
            this.hitResult = null;
        } else {
            this.hitResult = WorldUtils.rayTraceBlock(this.world, this.origin, this.target);
        }
    }

    @Override
    public boolean hasNext() {
        return this.hitResult != null && this.hitResult.getType() != RayTraceResult.Type.MISS;
    }

    @Override
    @Nonnull
    public BlockRayTraceResult next() {
        if (this.hitResult == null || this.hitResult.getType() == RayTraceResult.Type.MISS)
            throw new IllegalStateException("No more blocks in trace");
        final BlockRayTraceResult result = this.hitResult;
        this.origin = this.hitResult.getHitVec().add(this.normal);
        doTrace();
        return result;
    }
}

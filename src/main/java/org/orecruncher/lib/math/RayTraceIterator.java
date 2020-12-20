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

package org.orecruncher.lib.math;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class RayTraceIterator implements Iterator<BlockRayTraceResult> {

    @Nonnull
    private final BlockRayTrace traceContext;
    @Nonnull
    private final BlockPos targetBlock;
    @Nonnull
    private final Vector3d normal;

    @Nullable
    private BlockRayTraceResult hitResult;

    public RayTraceIterator(@Nonnull final BlockRayTrace traceContext) {
        this.traceContext = traceContext;
        this.targetBlock = new BlockPos(traceContext.end);
        this.normal = MathStuff.normalize(traceContext.start, traceContext.end);
        doTrace();
    }

    private void doTrace() {
        if (this.hitResult != null && this.hitResult.getPos().equals(this.targetBlock)) {
            this.hitResult = null;
        } else {
            this.hitResult = this.traceContext.trace();
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
        this.traceContext.start = this.hitResult.getHitVec().add(this.normal);
        doTrace();
        return result;
    }

}

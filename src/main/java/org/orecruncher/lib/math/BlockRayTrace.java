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

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block ray trace and context rolled into one!  With some optimizations specific to blocks.  These routines are
 * based on what the Minecraft raytrace algorithms do.  Pretty standard voxel based ray trace.
 */
public class BlockRayTrace {

    private static final double NUDGE = -1.0E-7D;

    final IBlockReader world;
    final RayTraceContext.BlockMode blockMode;
    final RayTraceContext.FluidMode fluidMode;
    final ISelectionContext selectionCtx;

    // Can be changed dynamically to avoid recreating contexts
    Vector3d start;
    Vector3d end;

    public BlockRayTrace(@Nonnull final IBlockReader world, @Nonnull final RayTraceContext.BlockMode bm, @Nonnull final RayTraceContext.FluidMode fm) {
        this(world, Vector3d.ZERO, Vector3d.ZERO, bm, fm);
    }

    public BlockRayTrace(@Nonnull final IBlockReader world, @Nonnull final Vector3d start, @Nonnull final Vector3d end, @Nonnull final RayTraceContext.BlockMode bm, @Nonnull final RayTraceContext.FluidMode fm) {
        this.world = world;
        this.start = start;
        this.end = end;
        this.blockMode = bm;
        this.fluidMode = fm;
        this.selectionCtx = ISelectionContext.dummy();
    }

    @Nonnull
    public BlockRayTraceResult trace() {
        return traceLoop();
    }

    @Nonnull
    public BlockRayTraceResult trace(@Nonnull final Vector3d start, @Nonnull final Vector3d end) {
        this.start = start;
        this.end = end;
        return traceLoop();
    }

    @Nonnull
    private BlockRayTraceResult traceLoop() {
        if (this.start.equals(this.end)) {
            return miss();
        } else {
            double lerpX = MathHelper.lerp(NUDGE, this.start.x, this.end.x);
            double lerpY = MathHelper.lerp(NUDGE, this.start.y, this.end.y);
            double lerpZ = MathHelper.lerp(NUDGE, this.start.z, this.end.z);
            int posX = MathHelper.floor(lerpX);
            int posY = MathHelper.floor(lerpY);
            int posZ = MathHelper.floor(lerpZ);

            // Do a quick check on the first block.  If there is a hit return
            // that result.  Else, traverse the line segment between start and end
            // points until a hit.
            BlockPos.Mutable mutablePos = new BlockPos.Mutable(posX, posY, posZ);
            BlockRayTraceResult traceResult = hitCheck(mutablePos);
            if (traceResult == null) {
                // No hit.  Do the calcs to traverse the line
                double xLerp = MathHelper.lerp(NUDGE, this.end.x, this.start.x);
                double yLerp = MathHelper.lerp(NUDGE, this.end.y, this.start.y);
                double zLerp = MathHelper.lerp(NUDGE, this.end.z, this.start.z);
                double lenX = xLerp - lerpX;
                double lenY = yLerp - lerpY;
                double lenZ = zLerp - lerpZ;
                int dirX = MathHelper.signum(lenX);
                int dirY = MathHelper.signum(lenY);
                int dirZ = MathHelper.signum(lenZ);
                double deltaX = dirX == 0 ? Double.MAX_VALUE : (dirX / lenX);
                double deltaY = dirY == 0 ? Double.MAX_VALUE : (dirY / lenY);
                double deltaZ = dirZ == 0 ? Double.MAX_VALUE : (dirZ / lenZ);
                double X = deltaX * (dirX > 0 ? 1.0D - MathHelper.frac(lerpX) : MathHelper.frac(lerpX));
                double Y = deltaY * (dirY > 0 ? 1.0D - MathHelper.frac(lerpY) : MathHelper.frac(lerpY));
                double Z = deltaZ * (dirZ > 0 ? 1.0D - MathHelper.frac(lerpZ) : MathHelper.frac(lerpZ));

                // Main processing loop that traverses the line segement between start and end point.  This process
                // will continue until there is a miss or a block is hit.
                do {
                    // Reached the end of the line?
                    if (X > 1.0D && Y > 1.0D && Z > 1.0D) {
                        return miss();
                    }

                    // Delta the axis that needs to be advanced.
                    if (X < Y) {
                        if (X < Z) {
                            posX += dirX;
                            X += deltaX;
                        } else {
                            posZ += dirZ;
                            Z += deltaZ;
                        }
                    } else if (Y < Z) {
                        posY += dirY;
                        Y += deltaY;
                    } else {
                        posZ += dirZ;
                        Z += deltaZ;
                    }

                    // Check for a hit.  If null is returned loop back around.
                    traceResult = hitCheck(mutablePos.setPos(posX, posY, posZ));
                } while (traceResult == null);

            }
            // Return whatever result we have
            return traceResult;
        }
    }

    @Nonnull
    private BlockRayTraceResult miss() {
        final Vector3d directionVec = this.start.subtract(this.end);
        return BlockRayTraceResult.createMiss(this.end, Direction.getFacingFromVector(directionVec.x, directionVec.y, directionVec.z), new BlockPos(this.end));
    }

    // Fast path an empty air block as much as possible.  For tracing this would be the most common block
    // encountered.  As an FYI the logic needs to consider both the solid and fluid aspects of a block since
    // Minecraft now has this notion of water logged.
    @Nullable
    private BlockRayTraceResult hitCheck(@Nonnull final BlockPos pos) {
        // Handle the block
        BlockRayTraceResult traceResult = null;
        final BlockState state = this.world.getBlockState(pos);
        if (!state.isAir(this.world, pos)) {
            final VoxelShape voxelShape = this.blockMode.get(state, this.world, pos, this.selectionCtx);
            if (!voxelShape.isEmpty())
                traceResult = this.world.rayTraceBlocks(this.start, this.end, pos, voxelShape, state);
        }

        // Handle it's fluid state
        BlockRayTraceResult fluidTraceResult = null;
        final FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty() && this.fluidMode.test(fluidState)) {
            final VoxelShape voxelFluidShape = state.getShape(this.world, pos);
            if (!voxelFluidShape.isEmpty())
                fluidTraceResult = voxelFluidShape.rayTrace(this.start, this.end, pos);
        }

        // No results for either
        if (traceResult == fluidTraceResult)
            return null;
        // No fluid result
        if (fluidTraceResult == null)
            return traceResult;
        // No block result
        if (traceResult == null)
            return fluidTraceResult;

        // Get the closest.  It is possible to encounter the water before the solid, like a fence post that is
        // water logged.
        final double blockDistance = this.start.squareDistanceTo(traceResult.getHitVec());
        final double fluidDistance = this.start.squareDistanceTo(fluidTraceResult.getHitVec());
        return blockDistance <= fluidDistance ? traceResult : fluidTraceResult;
    }
}

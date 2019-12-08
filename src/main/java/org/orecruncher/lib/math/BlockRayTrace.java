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
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BlockRayTrace {

    final IBlockReader world;
    final RayTraceContext.BlockMode blockMode;
    final RayTraceContext.FluidMode fluidMode;

    // Can be changed dynamically to avoid recreating contexts
    Vec3d start;
    Vec3d end;

    public BlockRayTrace(@Nonnull final IBlockReader world, @Nonnull final RayTraceContext.BlockMode bm, @Nonnull final RayTraceContext.FluidMode fm) {
        this(world, Vec3d.ZERO, Vec3d.ZERO, bm, fm);
    }

    public BlockRayTrace(@Nonnull final IBlockReader world, @Nonnull final Vec3d start, @Nonnull final Vec3d end, @Nonnull final RayTraceContext.BlockMode bm, @Nonnull final RayTraceContext.FluidMode fm) {
        this.world = world;
        this.start = start;
        this.end = end;
        this.blockMode = bm;
        this.fluidMode = fm;
    }

    private static BlockRayTraceResult traceLoop(BlockRayTrace ctx, BiFunction<BlockRayTrace, BlockPos, BlockRayTraceResult> hitCheck, Function<BlockRayTrace, BlockRayTraceResult> miss) {
        if (ctx.start.equals(ctx.end)) {
            return miss.apply(ctx);
        } else {
            double xLerp = MathHelper.lerp(-1.0E-7D, ctx.end.x, ctx.start.x);
            double yLerp = MathHelper.lerp(-1.0E-7D, ctx.end.y, ctx.start.y);
            double zLerp = MathHelper.lerp(-1.0E-7D, ctx.end.z, ctx.start.z);
            double lerpX = MathHelper.lerp(-1.0E-7D, ctx.start.x, ctx.end.x);
            double lerpY = MathHelper.lerp(-1.0E-7D, ctx.start.y, ctx.end.y);
            double lerpZ = MathHelper.lerp(-1.0E-7D, ctx.start.z, ctx.end.z);
            int posX = MathHelper.floor(lerpX);
            int posY = MathHelper.floor(lerpY);
            int posZ = MathHelper.floor(lerpZ);
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(posX, posY, posZ);
            BlockRayTraceResult traceResult = hitCheck.apply(ctx, mutablePos);
            if (traceResult != null) {
                return traceResult;
            } else {
                double lenX = xLerp - lerpX;
                double lenY = yLerp - lerpY;
                double lenZ = zLerp - lerpZ;
                int dirX = MathHelper.signum(lenX);
                int dirY = MathHelper.signum(lenY);
                int dirZ = MathHelper.signum(lenZ);
                float deltaX = dirX == 0 ? Float.MAX_VALUE : (float) (dirX / lenX);
                float deltaY = dirY == 0 ? Float.MAX_VALUE : (float) (dirY / lenY);
                float deltaZ = dirZ == 0 ? Float.MAX_VALUE : (float) (dirZ / lenZ);
                float X = (float) (deltaX * (dirX > 0 ? 1.0D - MathHelper.frac(lerpX) : MathHelper.frac(lerpX)));
                float Y = (float) (deltaY * (dirY > 0 ? 1.0D - MathHelper.frac(lerpY) : MathHelper.frac(lerpY)));
                float Z = (float) (deltaZ * (dirZ > 0 ? 1.0D - MathHelper.frac(lerpZ) : MathHelper.frac(lerpZ)));

                //BlockRayTraceResult traceResult;
                do {
                    if (X > 1.0F && Y > 1.0F && Z > 1.0F) {
                        return miss.apply(ctx);
                    }

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

                    traceResult = hitCheck.apply(ctx, mutablePos.setPos(posX, posY, posZ));
                } while (traceResult == null);

                return traceResult;
            }
        }
    }

    @Nonnull
    public BlockRayTraceResult trace() {
        return rayTraceBlocks();
    }

    @Nonnull
    public BlockRayTraceResult trace(@Nonnull final Vec3d start, @Nonnull final Vec3d end) {
        this.start = start;
        this.end = end;
        return rayTraceBlocks();
    }

    @Nonnull
    private BlockRayTraceResult rayTraceBlocks() {
        return traceLoop(this, (ctx, pos) -> {
            // Handle the block
            final BlockState state = ctx.world.getBlockState(pos);
            final VoxelShape voxelShape = ctx.blockMode.get(state, ctx.world, pos, ISelectionContext.dummy());
            final BlockRayTraceResult traceResult = ctx.world.rayTraceBlocks(ctx.start, ctx.end, pos, voxelShape, state);

            // Handle it's fluid state
            BlockRayTraceResult fluidTraceResult = null;
            final IFluidState fluidState = state.getFluidState(); //ctx.world.getFluidState(pos);
            if (!fluidState.isEmpty()) {
                final VoxelShape voxelFluidShape = ctx.fluidMode.test(fluidState) ? state.getShape(ctx.world, pos) : VoxelShapes.empty();
                fluidTraceResult = voxelFluidShape.isEmpty() ? null : voxelFluidShape.rayTrace(ctx.start, ctx.end, pos);
            }

            // Get the distances for each
            final float blockDistance = traceResult == null ? Float.MAX_VALUE : (float) ctx.start.squareDistanceTo(traceResult.getHitVec());
            final float fluidDistance = fluidTraceResult == null ? Float.MAX_VALUE : (float) ctx.start.squareDistanceTo(fluidTraceResult.getHitVec());

            // Return the hit result for the closest one
            return blockDistance <= fluidDistance ? traceResult : fluidTraceResult;
        }, (ctx) -> {
            final Vec3d directionVec = ctx.start.subtract(ctx.end);
            return BlockRayTraceResult.createMiss(ctx.end, Direction.getFacingFromVector(directionVec.x, directionVec.y, directionVec.z), new BlockPos(ctx.end));
        });
    }

}

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

import com.google.common.collect.AbstractIterator;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

public final class BlockPosUtil {

    private BlockPosUtil() {

    }

    /**
     * This method determines the BlockPos of the specified entity without doing the
     * offset of Y axis.
     *
     * @param entity Entity for which the BlockPos is returned
     * @return BlockPos with coordinates
     */
    public static BlockPos getNonOffsetPos(@Nonnull final Entity entity) {
        return new BlockPos(entity.posX, entity.posY, entity.posZ);
    }

    public static BlockPos.MutableBlockPos setPos(@Nonnull final BlockPos.MutableBlockPos pos,
                                                  @Nonnull final Vec3d vec) {
        return pos.setPos(vec.x, vec.y, vec.z);
    }

    public static boolean canFormCuboid(@Nonnull final BlockPos p1, @Nonnull final BlockPos p2) {
        return !(p1.getX() == p2.getX() || p1.getZ() == p2.getZ() || p1.getY() == p2.getY());
    }

    public static BlockPos createMinPoint(@Nonnull final BlockPos p1, @Nonnull final BlockPos p2) {
        return new BlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ()));
    }

    public static BlockPos createMaxPoint(@Nonnull final BlockPos p1, @Nonnull final BlockPos p2) {
        return new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ()));
    }

    /**
     * Determines if the test point is contained within the volume described by two
     * other points. It is expected that the calling routine has ensured that the
     * min/max points are valid. If they are not valid the results will more than
     * likely be erroneous.
     *
     * @param test The point that is being tested
     * @param min  The point describing the minimum vertex of the volume
     * @param max  The point describing the maximum vertex of the volume
     * @return Whether the test point is within the boundaries of the volume,
     * inclusive
     */
    public static boolean contains(@Nonnull final BlockPos test, @Nonnull final BlockPos min,
                                   @Nonnull final BlockPos max) {
        return test.getX() >= min.getX() && test.getX() <= max.getX()
                //
                && test.getY() >= min.getY() && test.getY() <= max.getY()
                //
                && test.getZ() >= min.getZ() && test.getZ() <= max.getZ();
    }

    /**
     * Determines if the test point is outside the volume described by two other
     * points. It is expected that the calling routine has ensured that the min/max
     * points are valid. If they are not valid the results will more than likely be
     * erroneous.
     *
     * @param test The point that is being tested
     * @param min  The point describing the minimum vertex of the volume
     * @param max  The point describing the maximum vertex of the volume
     * @return Whether the test point is outside the boundaries of the volume,
     * exclusive
     */
    public static boolean notContains(@Nonnull final BlockPos test, @Nonnull final BlockPos min,
                                      @Nonnull final BlockPos max) {
        return test.getX() < min.getX() || test.getX() > max.getX()
                //
                || test.getY() < min.getY() || test.getY() > max.getY()
                //
                || test.getZ() < min.getZ() || test.getZ() > max.getZ();
    }

    /**
     * Like getAllInBox but reuses a single MutableBlockPos instead. If this method
     * is used, the resulting BlockPos instances can only be used inside the
     * iteration loop.
     * <p>
     * NOTE: This is similar to the logic in Forge. Difference is that it favors
     * iterating along the Y axis first before X/Z. Goal is to maximize chunk
     * caching for area scanning.
     */
    public static Iterable<BlockPos.MutableBlockPos> getAllInBoxMutable(BlockPos from, BlockPos to) {
        final BlockPos blockpos = createMinPoint(from, to);
        final BlockPos blockpos1 = createMaxPoint(from, to);
        return () -> new AbstractIterator<BlockPos.MutableBlockPos>() {
            private BlockPos.MutableBlockPos theBlockPos;

            @Override
            protected BlockPos.MutableBlockPos computeNext() {
                if (this.theBlockPos == null) {
                    this.theBlockPos = new BlockPos.MutableBlockPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                    return this.theBlockPos;
                } else if (this.theBlockPos.equals(blockpos1)) {
                    return endOfData();
                } else {
                    int i = this.theBlockPos.getX();
                    int j = this.theBlockPos.getY();
                    int k = this.theBlockPos.getZ();

                    if (j < blockpos1.getY()) {
                        ++j;
                    } else if (i < blockpos1.getX()) {
                        j = blockpos.getY();
                        ++i;
                    } else if (k < blockpos1.getZ()) {
                        i = blockpos.getX();
                        j = blockpos.getY();
                        ++k;
                    }

                    this.theBlockPos.setPos(i, j, k);
                    return this.theBlockPos;
                }
            }
        };
    }

}
/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.scanner;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.BlockPosUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class Cuboid {

    protected final BlockPos[] vertices = new BlockPos[8];
    protected final int volume;
    protected final BlockPos minPoint;
    protected final BlockPos maxPoint;

    public Cuboid(@Nonnull final BlockPos[] points) {
        this(points[0], points[1]);
    }

    public Cuboid(@Nonnull final BlockPos vx1, @Nonnull final BlockPos vx2) {

        this.minPoint = BlockPosUtil.createMinPoint(vx1, vx2);
        this.maxPoint = BlockPosUtil.createMaxPoint(vx1, vx2);

        final BlockPos t = this.maxPoint.subtract(this.minPoint);
        this.volume = t.getX() * t.getY() * t.getZ();

        this.vertices[0] = this.minPoint;
        this.vertices[1] = this.maxPoint;
        this.vertices[2] = new BlockPos(this.minPoint.getX(), this.maxPoint.getY(), this.maxPoint.getZ());
        this.vertices[3] = new BlockPos(this.maxPoint.getX(), this.minPoint.getY(), this.minPoint.getZ());
        this.vertices[4] = new BlockPos(this.maxPoint.getX(), this.minPoint.getY(), this.maxPoint.getZ());
        this.vertices[5] = new BlockPos(this.minPoint.getX(), this.minPoint.getY(), this.maxPoint.getZ());
        this.vertices[6] = new BlockPos(this.minPoint.getX(), this.maxPoint.getY(), this.minPoint.getZ());
        this.vertices[7] = new BlockPos(this.maxPoint.getX(), this.maxPoint.getY(), this.minPoint.getZ());
    }

    public boolean contains(@Nonnull final BlockPos p) {
        return BlockPosUtil.contains(p, this.minPoint, this.maxPoint);
    }

    @Nonnull
    public BlockPos maximum() {
        return this.maxPoint;
    }

    @Nonnull
    public BlockPos minimum() {
        return this.minPoint;
    }

    public long volume() {
        return this.volume;
    }

    @Nullable
    public Cuboid intersection(@Nonnull final Cuboid o) {
        BlockPos vx1 = null;
        for (final BlockPos vx : this.vertices) {
            if (o.contains(vx)) {
                vx1 = vx;
                break;
            }
        }

        if (vx1 == null)
            return null;

        BlockPos vx2 = null;
        for (final BlockPos vx : o.vertices) {
            if (contains(vx) && BlockPosUtil.canFormCuboid(vx, vx1)) {
                vx2 = vx;
                break;
            }
        }

        return vx2 == null ? null : new Cuboid(vx1, vx2);
    }

}
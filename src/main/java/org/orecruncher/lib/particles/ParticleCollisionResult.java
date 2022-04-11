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

package org.orecruncher.lib.particles;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ParticleCollisionResult {

    public final IBlockReader world;
    public final Vector3d position;
    public final BlockState state;
    public final FluidState fluidState;
    public final boolean onGround;

    public ParticleCollisionResult(@Nonnull final IBlockReader world, @Nonnull final Vector3d pos, @Nonnull final BlockState state, final boolean onGround, @Nullable final FluidState fluid) {
        this.world = world;
        this.position = pos;
        this.state = state;
        this.onGround = onGround;
        this.fluidState = fluid != null ? fluid : Fluids.EMPTY.getDefaultState();
    }
}

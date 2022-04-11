/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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

package org.orecruncher.mobeffects.footsteps.facade;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

interface IFacadeAccessor {

	/*
	 * Name of the facade accessor for logging and identification purposes.
	 */
	@Nonnull
	String getName();

	/*
	 * Determines if the block can be handled by the accessor
	 */
	boolean instanceOf(@Nonnull final Block block);

	/*
	 * Indicates if the accessor is valid. It could be invalid if the associated mod
	 * is not installed.
	 */
	boolean isValid();

	/*
	 * Requests the underlying IBlockState for the block. The underlying IBlockState
	 * is what should be used when generating sound effects.
	 */
	@Nullable
	BlockState getBlockState(@Nonnull final LivingEntity entity, @Nonnull final BlockState state,
							 @Nonnull final IBlockReader world, @Nonnull final Vector3d pos, @Nullable final Direction side);
}

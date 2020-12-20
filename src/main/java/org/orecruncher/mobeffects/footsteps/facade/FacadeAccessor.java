/*
 * Dynamic Surroundings: Mob Effects
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

package org.orecruncher.mobeffects.footsteps.facade;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import org.orecruncher.mobeffects.MobEffects;

@OnlyIn(Dist.CLIENT)
class FacadeAccessor implements IFacadeAccessor {

	protected Class<?> IFacadeClass;
	protected Method accessor;

	public FacadeAccessor(@Nonnull final String clazz, @Nonnull final String method) {
		try {
			this.IFacadeClass = Class.forName(clazz);
			if (this.IFacadeClass != null)
				this.accessor = getMethod(method);
			else
				this.accessor = null;
		} catch (@Nonnull final Throwable t) {
			this.IFacadeClass = null;
			this.accessor = null;
		}
	}

	@Override
	@Nonnull
	public String getName() {
		return isValid() ? this.IFacadeClass.getName() : "INVALID";
	}

	@Override
	public boolean instanceOf(@Nonnull final Block block) {
		return isValid() && this.IFacadeClass.isInstance(block);
	}

	@Override
	public boolean isValid() {
		return this.accessor != null;
	}

	@Override
	@Nullable
	public BlockState getBlockState(@Nonnull final LivingEntity entity, @Nonnull final BlockState state,
									@Nonnull final IWorldReader world, @Nonnull final Vector3d pos, @Nullable final Direction side) {
		if (isValid())
			try {
				if (instanceOf(state.getBlock()))
					return call(state, world, new BlockPos(pos), side);
			} catch (@Nonnull final Throwable ex) {
				MobEffects.LOGGER.error(ex, "Error!");
				this.IFacadeClass = null;
				this.accessor = null;
			}

		return null;
	}

	protected Method getMethod(@Nonnull final String method) throws Throwable {
		return this.IFacadeClass.getMethod(method, BlockState.class, BlockPos.class, Direction.class);
	}

	protected BlockState call(@Nonnull final BlockState state, @Nonnull final IWorldReader world,
			@Nonnull final BlockPos pos, @Nullable final Direction side) throws Throwable {
		return (BlockState) this.accessor.invoke(state.getBlock(), world, pos, side);
	}

}

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
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
final class EnderIOFacadeAccessor extends FacadeAccessor {

	private static final String CLASS = "crazypants.enderio.paint.IPaintable";
	private static final String METHOD = "getPaintSource";

	public EnderIOFacadeAccessor() {
		super(CLASS, METHOD);
	}

	@Override
	protected Method getMethod(@Nonnull final String method) throws Throwable {
		return this.IFacadeClass.getMethod(method, BlockState.class, IWorldReader.class, BlockPos.class);
	}

	@Override
	protected BlockState call(@Nonnull final BlockState state, @Nonnull final IWorldReader world,
			@Nonnull final BlockPos pos, @Nullable final Direction side) throws Throwable {
		return (BlockState) this.accessor.invoke(state.getBlock(), state, world, pos);
	}
}

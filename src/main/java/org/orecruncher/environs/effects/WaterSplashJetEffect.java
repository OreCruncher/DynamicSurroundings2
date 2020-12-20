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

package org.orecruncher.environs.effects;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.effects.emitters.Jet;
import org.orecruncher.environs.effects.emitters.WaterSplashJet;
import org.orecruncher.lib.WorldUtils;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

@OnlyIn(Dist.CLIENT)
public class WaterSplashJetEffect extends JetEffect {

	private final static Vec3i[] cardinal_offsets = {
		new Vec3i(-1, 0, 0),
		new Vec3i(1, 0, 0),
		new Vec3i(0, 0, -1),
		new Vec3i(0, 0, 1)
	};

	public WaterSplashJetEffect(final int chance) {
		super(chance);
	}

	@Override
	@Nonnull
	public BlockEffectType getEffectType() {
		return BlockEffectType.SPLASH_JET;
	}

	private static boolean isUnboundedLiquid(final IBlockReader provider, final BlockPos pos) {
		for (final Vec3i cardinal_offset : cardinal_offsets) {
			final BlockPos tp = pos.add(cardinal_offset);
			final BlockState state = provider.getBlockState(tp);
			if (state.getMaterial() == Material.AIR)
				return true;
			final IFluidState fluidState = state.getFluidState();
			final int height = fluidState.getLevel();
			if (height > 0 && height < 8)
				return true;
		}

		return false;
	}

	/**
	 * Similar to isUnboundedLiquid() but geared towards determine that the liquid is bound on all sides.
	 */
	private static boolean isBoundedLiquid(final IBlockReader provider, final BlockPos pos) {
		for (final Vec3i cardinal_offset : cardinal_offsets) {
			final BlockPos tp = pos.add(cardinal_offset);
			final BlockState state = provider.getBlockState(tp);
			if (state.getMaterial() == Material.AIR)
				return false;
			final IFluidState fluidState = state.getFluidState();
			if (fluidState.isEmpty()) {
				continue;
			}
			if (fluidState.get(FlowingFluid.FALLING))
				return false;
			final int height = fluidState.getLevel();
			if (height > 0 && height < 8)
				return false;
		}

		return true;
	}

	private int liquidBlockCount(final IBlockReader provider, final BlockPos pos) {
		return countVerticalBlocks(provider, pos, FLUID_PREDICATE, 1);
	}

	public static boolean isValidSpawnBlock(@Nonnull final IBlockReader provider, @Nonnull final BlockPos pos) {
		return isValidSpawnBlock(provider, provider.getBlockState(pos), pos);
	}

	private static boolean isValidSpawnBlock(final IBlockReader provider, final BlockState state,
			final BlockPos pos) {
		if (state.getFluidState().isEmpty())
			return false;
		if (provider.getFluidState(pos.up()).isEmpty())
			return false;
		if (isUnboundedLiquid(provider, pos)) {
			final BlockPos down = pos.down();
			if (WorldUtils.isBlockSolid(provider, down))
				return true;
			return isBoundedLiquid(provider, down);
		}
		return false;
	}

	@Override
	public boolean canTrigger(@Nonnull final IBlockReader provider, @Nonnull final BlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		return super.canTrigger(provider, state, pos, random) && isValidSpawnBlock(provider, state, pos);
	}

	@Override
	public void doEffect(@Nonnull final IBlockReader provider, @Nonnull final BlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {

		final int strength = liquidBlockCount(provider, pos);
		if (strength > 1) {
			final float height = state.getFluidState().getActualHeight(provider, pos) + 0.1F;
			final Jet effect = new WaterSplashJet(strength, provider, pos, height);
			addEffect(effect);
		}
	}
}

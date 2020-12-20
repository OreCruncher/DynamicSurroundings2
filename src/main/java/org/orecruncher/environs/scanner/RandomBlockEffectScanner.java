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

import java.util.Collection;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.util.math.BlockPos;
import org.orecruncher.environs.effects.BlockEffect;
import org.orecruncher.environs.library.BlockStateData;
import org.orecruncher.environs.library.BlockStateUtil;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;

/**
 * Modeled after WorldClient::doVoidFogParticles() which handles the random
 * block update client side as well as barrier particles.
 *
 * The routine iterates 667 times. During each iteration it creates particles up
 * to a max of 16 blocks, and then up to a max of 32 blocks. There is some
 * overlap with the 16 block range when generating the 32 block version, but
 * since the iteration has been reduce to 667 (from 1000 in MC 1.7.10) it should
 * compensate.
 */
@OnlyIn(Dist.CLIENT)
public class RandomBlockEffectScanner extends RandomScanner {

	private static final int ITERATION_COUNT = 667;

	public static final int NEAR_RANGE = 16;
	public static final int FAR_RANGE = 32;

	public RandomBlockEffectScanner(@Nonnull final ScanContext locus, final int range) {
		super(locus, "RandomBlockScanner: " + range, range, ITERATION_COUNT);
	}

	@Override
	protected boolean interestingBlock(@Nonnull final BlockState state) {
		return BlockStateUtil.getData(state).hasSoundsOrEffects();
	}

	@Override
	public void blockScan(@Nonnull final BlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {
		final IBlockReader world = this.locus.getWorld();
		final BlockStateData profile = BlockStateUtil.getData(state);
		final Collection<BlockEffect> effects = profile.getEffects();

		for (final BlockEffect be : effects) {
			if (be.canTrigger(world, state, pos, rand))
				be.doEffect(world, state, pos, rand);
		}

		final IAcoustic sound = profile.getSoundToPlay(rand);
		if (sound != null)
			sound.playAt(pos);
	}

}

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.util.math.BlockPos;
import org.orecruncher.environs.effects.BlockEffect;
import org.orecruncher.environs.library.BlockStateUtil;

/**
 * This guy scans a large area around the player looking for blocks to spawn
 * "always on" effects such as waterfall splash and steam jets.
 *
 * The CuboidScanner tries to only scan new blocks that come into range as the
 * player moves. Once all the blocks are scanned in the region (cuboid) it will
 * stop. It will start again once the player moves location.
 */
@OnlyIn(Dist.CLIENT)
public class AlwaysOnBlockEffectScanner extends CuboidScanner {

	public AlwaysOnBlockEffectScanner(@Nonnull final ScanContext locus, final int range) {
		super(locus, "AlwaysOnBlockEffectScanner", range, 0);
	}

	@Override
	protected boolean interestingBlock(final BlockState state) {
		return BlockStateUtil.getData(state).hasAlwaysOnEffects();
	}

	@Override
	public void blockScan(@Nonnull final BlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {
		final IBlockReader provider = this.locus.getWorld();
		final Collection<BlockEffect> effects = BlockStateUtil.getData(state).getAlwaysOnEffects();
		for (final BlockEffect be : effects) {
			if (be.canTrigger(provider, state, pos, rand))
				be.doEffect(provider, state, pos, rand);
 		}
	}

}

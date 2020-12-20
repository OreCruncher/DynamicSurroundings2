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

import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.util.math.BlockPos;

@OnlyIn(Dist.CLIENT)
public abstract class Scanner {

	protected static final Set<BlockState> BLOCKSTATES_TO_IGNORE = new ReferenceArraySet<>(3);

	static {
		// The implementation searches backwards so order so the most common blocks will be hit first
		BLOCKSTATES_TO_IGNORE.add(Blocks.VOID_AIR.getDefaultState());
		BLOCKSTATES_TO_IGNORE.add(Blocks.CAVE_AIR.getDefaultState());
		BLOCKSTATES_TO_IGNORE.add(Blocks.AIR.getDefaultState());
	}

	private final static int MAX_BLOCKS_TICK = 6000;

	protected final String name;

	protected final int xRange;
	protected final int yRange;
	protected final int zRange;

	protected final int xSize;
	protected final int ySize;
	protected final int zSize;
	protected final int blocksPerTick;
	protected final int volume;

	protected final ScanContext locus;

	protected final Random random = new XorShiftRandom();
	protected final BlockPos.Mutable workingPos = new BlockPos.Mutable();

	public Scanner(@Nonnull final ScanContext locus, @Nonnull final String name, final int range) {
		this(locus, name, range, 0);
	}

	public Scanner(@Nonnull final ScanContext locus, @Nonnull final String name, final int range,
				   final int blocksPerTick) {
		this(locus, name, range, range, range, blocksPerTick);
	}

	public Scanner(@Nonnull final ScanContext locus, @Nonnull final String name, final int xRange, final int yRange,
				   final int zRange) {
		this(locus, name, xRange, yRange, zRange, 0);
	}

	public Scanner(@Nonnull final ScanContext locus, @Nonnull final String name, final int xRange, final int yRange,
				   final int zRange, final int blocksPerTick) {
		this.name = name;
		this.xRange = xRange;
		this.yRange = yRange;
		this.zRange = zRange;

		this.xSize = xRange * 2;
		this.ySize = yRange * 2;
		this.zSize = zRange * 2;
		this.volume = this.xSize * this.ySize * this.zSize;
		if (blocksPerTick == 0)
			this.blocksPerTick = Math.min(this.volume / 20, MAX_BLOCKS_TICK);
		else
			this.blocksPerTick = Math.min(blocksPerTick, MAX_BLOCKS_TICK);

		this.locus = locus;
	}

	/**
	 * The volume of the scan area
	 */
	public int getVolume() {
		return this.volume;
	}

	/**
	 * Invoked when a block of interest is discovered. The BlockPos provided is not
	 * safe to hold on to beyond the call so if it needs to be kept it needs to be
	 * copied.
	 */
	public abstract void blockScan(@Nonnull final BlockState state, @Nonnull final BlockPos pos,
								   @Nonnull final Random rand);

	/**
	 * Determines if the block is of interest to the effects. Override to provide
	 * logic beyond the basics.
	 */
	protected boolean interestingBlock(final BlockState state) {
		return state.getMaterial() != Material.AIR;
	}

	public void preScan() {

	}

	public void postScan() {

	}

	public void tick() {

		preScan();

		final IBlockReader provider = this.locus.getWorld();
		for (int count = 0; count < this.blocksPerTick; count++) {
			final BlockPos pos = nextPos(this.workingPos, this.random);
			if (pos == null)
				break;
			final BlockState state = provider.getBlockState(pos);
			if (BLOCKSTATES_TO_IGNORE.contains(state))
				continue;
			if (interestingBlock(state)) {
				blockScan(state, pos, this.random);
			}
		}

		postScan();

	}

	/**
	 * Provide the next block position to be processed. For memory efficiency the
	 * provided mutable should be used to store the coordinate information and
	 * returned from the function call.
	 */
	@Nullable
	protected abstract BlockPos nextPos(@Nonnull final BlockPos.Mutable pos, @Nonnull final Random rand);

}

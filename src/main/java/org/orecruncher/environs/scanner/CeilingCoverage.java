/*
 *  Dynamic Surroundings
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

import java.util.*;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;
import org.orecruncher.environs.handlers.CommonState;
import org.orecruncher.environs.library.DimensionInfo;
import org.orecruncher.environs.library.DimensionLibrary;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

/**
 * Performs area scanning around the player to determine area ceiling coverage.
 * Used to determine if the player is "inside" or "outside".
 */
public final class CeilingCoverage {

	private static final int SURVEY_INTERVAL = 4;
	private static final int INSIDE_SURVEY_RANGE = 3;
	private static final float INSIDE_THRESHOLD = 1.0F - 65.0F / 176.0F;
	private static final Cell[] cells;
	private static final float TOTAL_POINTS;
	private static final ObjectArray<ITag<Block>> NON_CEILING = new ObjectArray<>();

	static {

		final List<Cell> cellList = new ArrayList<>();
		// Build our cell map
		for (int x = -INSIDE_SURVEY_RANGE; x <= INSIDE_SURVEY_RANGE; x++)
			for (int z = -INSIDE_SURVEY_RANGE; z <= INSIDE_SURVEY_RANGE; z++)
				cellList.add(new Cell(new Vector3i(x, 0, z), INSIDE_SURVEY_RANGE));

		// Sort so the highest score cells are first
		Collections.sort(cellList);
		cells = cellList.toArray(new Cell[0]);

		float totalPoints = 0.0F;
		for (final Cell c : cellList)
			totalPoints += c.potentialPoints();
		TOTAL_POINTS = totalPoints;

		// Vanilla tags
		NON_CEILING.add(BlockTags.LEAVES);
		NON_CEILING.add(BlockTags.FENCE_GATES);
		NON_CEILING.add(BlockTags.FENCES);
		NON_CEILING.add(BlockTags.WALLS);

		// Forge tags
		NON_CEILING.add(Tags.Blocks.GLASS_PANES);
		NON_CEILING.add(Tags.Blocks.CHESTS);
		NON_CEILING.add(Tags.Blocks.FENCES);
		NON_CEILING.add(Tags.Blocks.FENCE_GATES);
	}

	private boolean reallyInside = false;

	public void tick() {
		if (TickCounter.getTickCount() % SURVEY_INTERVAL == 0) {
			final DimensionInfo dimInfo = DimensionLibrary.getData(GameUtils.getWorld());
			if (dimInfo.alwaysOutside()) {
				this.reallyInside = false;
			} else {
				final BlockPos pos = CommonState.getPlayerPosition();
				float score = 0.0F;
				for (Cell cell : cells) score += cell.score(pos);
				float ceilingCoverageRatio = 1.0F - (score / TOTAL_POINTS);
				this.reallyInside = ceilingCoverageRatio > INSIDE_THRESHOLD;
			}
		}
	}

	public boolean isReallyInside() {
		return this.reallyInside;
	}

	private static final class Cell implements Comparable<Cell> {

		private final Vector3i offset;
		private final float points;
		private final BlockPos.Mutable working;

		public Cell(@Nonnull final Vector3i offset, final int range) {
			this.offset = offset;
			final float xV = range - MathStuff.abs(offset.getX()) + 1;
			final float zV = range - MathStuff.abs(offset.getZ()) + 1;
			final float candidate = Math.min(xV, zV);
			this.points = candidate * candidate;
			this.working = new BlockPos.Mutable();
		}

		public float potentialPoints() {
			return this.points;
		}

		public float score(@Nonnull final BlockPos playerPos) {
			this.working.setPos(
					playerPos.getX() + this.offset.getX(),
					playerPos.getY() + this.offset.getY(),
					playerPos.getZ() + this.offset.getZ()
			);

			final World world = GameUtils.getWorld();
			final int playerHeight = Math.max(playerPos.getY() + 1, 0);

			// Get the precipitation height
			this.working.setPos(WorldUtils.getPrecipitationHeight(world, this.working));

			// Scan down looking for blocks that are considered "cover"
			while (this.working.getY() > playerHeight) {

				final BlockState state = world.getBlockState(this.working);

				if (actsAsCeiling(state)) {
					// Cover block - no points for you!
					return 0;
				}

				this.working.setY(this.working.getY() - 1);
			}

			// Scanned down to the players head and found nothing. So give the points.
			return this.points;
		}

		@Override
		public int compareTo(@Nonnull final Cell cell) {
			// Want big scores first in the list
			return -Float.compare(potentialPoints(), cell.potentialPoints());
		}

		@Override
		@Nonnull
		public String toString() {
			return this.offset.toString() +
					" points: " + this.points;
		}

		private boolean actsAsCeiling(@Nonnull final BlockState state) {
			// If it doesn't block movement it doesn't count as a ceiling.
			if (!state.getMaterial().blocksMovement())
				return false;

			// Test the block tags in our NON_CEILING set to see if any match
			final Block block = state.getBlock();
			for (final ITag<Block> tag : NON_CEILING) {
				if (tag.contains(block))
					return false;
			}
			return true;
		}
	}

}

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

package org.orecruncher.environs.shaders.aurora;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.orecruncher.environs.handlers.CommonState;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;

import java.time.Instant;

@OnlyIn(Dist.CLIENT)
public class AuroraUtils {

	private AuroraUtils() {

	}

	public static final int PLAYER_FIXED_Y_OFFSET = 64;
	public static final int PLAYER_FIXED_Z_OFFSET = 150;

	public static final int AURORA_PEAK_AGE = 512;
	public static final int AURORA_AGE_RATE = 1;

	/*
	 * The range in chunks of the player view.
	 */
	public static int getChunkRenderDistance() {
		return GameUtils.getGameSettings().renderDistanceChunks;
	}

	/*
	 * Returns a time calculation based on the number of ticks that have occured
	 * combined with the current partial tick count. Not usable for actual time
	 * calculations.
	 */
	public static float getTimeSeconds() {
		return ((float) TickCounter.getTickCount() + GameUtils.getMC().getRenderPartialTicks()) / 20F;
	}

	/*
	 * Use cached dimension info to determine if auroras are possible for the
	 * dimension.
	 */
	public static boolean dimensionHasAuroras() {
		return CommonState.getDimensionInfo().hasAuroras();
	}

	/*
	 * Generate a seed for an aurora
	 */
	public static long getSeed() {
		final long seed = Instant.now().toEpochMilli() / (1000L * 60L * 24L) * 311;
		return seed + CommonState.getClock().getDay();
	}

}

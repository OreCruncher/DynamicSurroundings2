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

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.random.LCGRandom;

import net.minecraft.util.math.BlockPos;

/**
 * Serves up random blocks in an area around the player. Concentration of block
 * selections are closer to the player.
 */
@OnlyIn(Dist.CLIENT)
public abstract class RandomScanner extends Scanner {

	private final LCGRandom lcg = new LCGRandom();

	private int playerX;
	private int playerY;
	private int playerZ;

	public RandomScanner(@Nonnull final ScanContext locus, @Nonnull final String name, final int range,
                         final int blocksPerTick) {
		super(locus, name, range, blocksPerTick);
	}

	private int randomRange(final int range) {
		return this.lcg.nextInt(range) - this.lcg.nextInt(range);
	}

	@Override
	public void preScan() {
		final BlockPos pos = this.locus.getCenter();
		this.playerX = pos.getX();
		this.playerY = pos.getY();
		this.playerZ = pos.getZ();
	}

	@Override
	@Nonnull
	protected BlockPos nextPos(@Nonnull final BlockPos.Mutable workingPos, @Nonnull final Random rand) {
		return workingPos.setPos(this.playerX + randomRange(this.xRange), this.playerY + randomRange(this.yRange),
				this.playerZ + randomRange(this.zRange));
	}

}

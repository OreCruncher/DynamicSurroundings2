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

import javax.annotation.Nonnull;

import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.util.math.BlockPos;
import org.orecruncher.lib.logging.IModLog;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ScanContext {

	private final Supplier<IBlockReader> worldReader;
	private final Supplier<BlockPos> scanCenter;
	private final Supplier<Integer> worldReference;
	private final Supplier<IModLog> logger;

	public ScanContext(
			@Nonnull final Supplier<IBlockReader> worldReader,
			@Nonnull final Supplier<BlockPos> scanCenter,
			@Nonnull final Supplier<IModLog> logger,
			@Nonnull final Supplier<Integer> worldReference
	) {
		this.worldReader = worldReader;
		this.scanCenter = scanCenter;
		this.worldReference = worldReference;
		this.logger = logger;
	}

	@Nonnull
	public IBlockReader getWorld() {
		return this.worldReader.get();
	}

	@Nonnull
	public BlockPos getCenter() {
		return this.scanCenter.get();
	}

	@Nonnull
	public IModLog getLogger() {
		return this.logger.get();
	}

	public int getReference() {
		return this.worldReference.get();
	}

}

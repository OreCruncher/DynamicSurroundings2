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

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import org.orecruncher.lib.BlockPosUtil;

/**
 * Implements a "peeking" iterator. The iterator uses mutables for position so
 * they aren't safe to cache.
 */
@OnlyIn(Dist.CLIENT)
public class CuboidPointIterator implements IPointIterator {

	static final CuboidPointIterator NULL_ITERATOR = new CuboidPointIterator() {

		@Override
		public BlockPos next() {
			return null;
		}

		@Override
		public BlockPos peek() {
			return null;
		}

	};

	protected final Iterator<Mutable> itr;
	protected BlockPos peeked;

	private CuboidPointIterator() {
		this.itr = null;
	}

	public CuboidPointIterator(@Nonnull final BlockPos[] points) {
		this(points[0], points[1]);
	}

	public CuboidPointIterator(@Nonnull final BlockPos p1, @Nonnull final BlockPos p2) {
		// The getAllInBox() deals with figuring the min/max points
		this.itr = BlockPosUtil.getAllInBoxMutable(p1, p2).iterator();
		if (this.itr.hasNext())
			this.peeked = this.itr.next();
	}

	@Override
	@Nullable
	public BlockPos next() {
		final BlockPos result = this.peeked;
		this.peeked = this.itr.hasNext() ? this.itr.next() : null;
		return result;
	}

	@Override
	@Nullable
	public BlockPos peek() {
		return this.peeked;
	}

}

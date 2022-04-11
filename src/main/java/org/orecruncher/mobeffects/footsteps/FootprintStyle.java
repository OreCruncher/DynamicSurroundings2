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

package org.orecruncher.mobeffects.footsteps;


import javax.annotation.Nonnull;

public enum FootprintStyle {

	SHOE,
	SQUARE,
	HORSESHOE,
	BIRD,
	PAW,
	SQUARE_SOLID,
	LOWRES_SQUARE;

	@Nonnull
	public static FootprintStyle getStyle(final int v) {
		if (v >= values().length)
			return LOWRES_SQUARE;
		return values()[v];
	}
}

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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.StringUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum Substrate {

	NORMAL("normal"),
	CARPET("carpet"),
	FOLIAGE("foliage"),
	MESSY("messy"),
	FENCE("bigger");

	private static final Map<String, Substrate> lookup = new Object2ObjectOpenHashMap<>();
	static {
		for (final Substrate s : Substrate.values())
			lookup.put(s.name, s);
	}

	private final String name;

	Substrate(@Nonnull final String name) {
		this.name = name;
	}

	@Nonnull
	public static Substrate get(@Nullable final String name) {
		return StringUtils.isNullOrEmpty(name) ? Substrate.NORMAL : lookup.get(name);
	}
}

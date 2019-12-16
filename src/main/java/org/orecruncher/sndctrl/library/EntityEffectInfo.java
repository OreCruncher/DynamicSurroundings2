/*
 * Dynamic Surroundings: Sound Control
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

package org.orecruncher.sndctrl.library;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.sndctrl.library.config.EntityConfig;

@OnlyIn(Dist.CLIENT)
public class EntityEffectInfo {

	public final String effects;
	public final String variator;

	public EntityEffectInfo() {
		this.effects = StringUtils.EMPTY;
		this.variator = "default";
	}

	public EntityEffectInfo(@Nonnull final EntityConfig ec) {
		this.effects = ec.effects;
		this.variator = ec.variator;
	}

	@Override
	public String toString() {
		if (!this.effects.isEmpty()) {
			return this.effects + "; variator=" + this.variator;
		}
		return "<NONE>";
	}
}

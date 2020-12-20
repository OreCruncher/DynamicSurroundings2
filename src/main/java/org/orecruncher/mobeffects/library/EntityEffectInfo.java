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

package org.orecruncher.mobeffects.library;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.library.config.EntityConfig;
import org.orecruncher.sndctrl.api.acoustics.Library;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class EntityEffectInfo {

	public final Set<ResourceLocation> effects = new HashSet<>(6);
	public final String variator;

	public EntityEffectInfo() {
		this.variator = "default";
	}

	public EntityEffectInfo(@Nonnull final EntityConfig ec) {
		final String[] effects = ec.effects.split(",");
		for (final String e : effects) {
			final ResourceLocation r = Library.resolveResource(MobEffects.MOD_ID, e);
			this.effects.add(r);
		}
		this.variator = ec.variator;
	}

	@Override
	public String toString() {
		if (!this.effects.isEmpty()) {
			return this.effects.toString() + "; variator=" + this.variator;
		}
		return "<NONE>";
	}
}

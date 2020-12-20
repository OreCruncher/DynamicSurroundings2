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

package org.orecruncher.environs.library;

import java.util.Collection;
import java.util.Random;

import javax.annotation.Nonnull;

import com.google.common.base.Joiner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.effects.BlockEffect;
import org.orecruncher.lib.WeightTable;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;

/**
 * Base class for the data being assigned into the IBlockState implementation.
 */
@OnlyIn(Dist.CLIENT)
public class BlockStateData {

	public static final BlockStateData DEFAULT = new BlockStateData();

	protected final ObjectArray<WeightedAcousticEntry> sounds = new ObjectArray<>();
	protected final ObjectArray<BlockEffect> effects = new ObjectArray<>();
	protected final ObjectArray<BlockEffect> alwaysOn = new ObjectArray<>();
	protected int chance = 100;
	protected boolean hasSoundsAndEffects;
	protected boolean hasAlwaysOn;

	public void setChance(final int chance) {
		this.chance = chance;
	}

	public int getChance() {
		return this.chance;
	}

	public void addSound(@Nonnull final WeightedAcousticEntry sound) {
		this.sounds.add(sound);
	}

	public void clearSounds() {
		this.sounds.clear();
	}

	@Nonnull
	public Collection<WeightedAcousticEntry> getSounds() {
		return this.sounds;
	}

	public void addEffect(@Nonnull final BlockEffect effect) {
		if (effect.getChance() > 0)
			this.effects.add(effect);
		else
			this.alwaysOn.add(effect);
	}

	public void clearEffects() {
		this.effects.clear();
		this.alwaysOn.clear();
	}

	@Nonnull
	public Collection<BlockEffect> getEffects() {
		return this.effects;
	}

	@Nonnull
	public Collection<BlockEffect> getAlwaysOnEffects() {
		return this.alwaysOn;
	}

	public IAcoustic getSoundToPlay(@Nonnull final Random random) {
		if (this.sounds.size() > 0 && random.nextInt(getChance()) == 0) {
			final WeightTable<IAcoustic> table = new WeightTable<>();
			for (final WeightedAcousticEntry ae : this.sounds)
				if (ae.matches())
					table.add(ae);
			return table.next();
		}
		return null;
	}

	public boolean hasSoundsOrEffects() {
		return this.hasSoundsAndEffects;
	}

	public boolean hasAlwaysOnEffects() {
		return this.hasAlwaysOn;
	}

	public void trim() {
		this.sounds.trim();
		this.effects.trim();
		this.alwaysOn.trim();
		this.hasSoundsAndEffects = this.sounds.size() > 0 || this.effects.size() > 0;
		this.hasAlwaysOn = this.alwaysOn.size() > 0;
	}

	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if (this.sounds.size() > 0) {
			builder.append(" chance:").append(this.chance);
			builder.append("; sounds [");
			builder.append(Joiner.on(',').join(this.sounds));
			builder.append(']');
		} else {
			builder.append("NO SOUNDS");
		}

		if (this.effects != this.alwaysOn) {
			builder.append("; effects [");
			builder.append(Joiner.on(',').join(this.effects));
			builder.append(',');
			builder.append(Joiner.on(',').join(this.alwaysOn));
			builder.append(']');
		} else {
			builder.append("; NO EFFECTS");
		}

		return builder.toString();
	}
}

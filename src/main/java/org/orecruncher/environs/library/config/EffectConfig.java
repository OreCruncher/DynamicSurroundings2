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

package org.orecruncher.environs.library.config;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.annotations.SerializedName;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.effects.BlockEffectType;
import org.orecruncher.lib.validation.IValidator;
import org.orecruncher.lib.validation.ValidationException;
import org.orecruncher.lib.validation.ValidationHelpers;

import javax.annotation.Nonnull;

public final class EffectConfig implements IValidator<EffectConfig> {

	@SerializedName("effect")
	public String effect = null;
	@SerializedName("conditions")
	public String conditions = StringUtils.EMPTY;
	@SerializedName("chance")
	public Integer chance = null;

	@Override
	public void validate(@Nonnull final EffectConfig obj) throws ValidationException {
		ValidationHelpers.isEnumValue("effect", this.effect, BlockEffectType.class, Environs.LOGGER::warn);
		if (this.chance != null)
			// Min value of 0 means always on
			ValidationHelpers.inRange("chance", this.chance, 0, Integer.MAX_VALUE, Environs.LOGGER::warn);
	}
}

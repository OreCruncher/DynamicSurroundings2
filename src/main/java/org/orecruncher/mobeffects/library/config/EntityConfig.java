/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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

package org.orecruncher.mobeffects.library.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.validation.IValidator;
import org.orecruncher.lib.validation.ValidationException;
import org.orecruncher.lib.validation.ValidationHelpers;
import org.orecruncher.mobeffects.MobEffects;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EntityConfig implements IValidator<EntityConfig> {

	@SerializedName("effects")
	public String effects = StringUtils.EMPTY;
	@SerializedName("variator")
	public String variator = "default";
	@SerializedName("blockedSounds")
	public List<String> blockedSounds = ImmutableList.of();

	@Override
	public void validate(@Nonnull final EntityConfig obj) throws ValidationException {
		ValidationHelpers.notNullOrWhitespace("effects", this.effects, MobEffects.LOGGER::warn);
		ValidationHelpers.notNullOrWhitespace("variator", this.variator, MobEffects.LOGGER::warn);
		for (final String s : this.blockedSounds)
			ValidationHelpers.isProperResourceLocation("blockedSounds", s, MobEffects.LOGGER::warn);
	}
}

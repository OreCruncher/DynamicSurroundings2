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

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Environs;
import org.orecruncher.lib.validation.IValidator;
import org.orecruncher.lib.validation.ValidationException;
import org.orecruncher.lib.validation.ValidationHelpers;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class BlockConfig implements IValidator<BlockConfig> {
	@SerializedName("blocks")
	public List<String> blocks = ImmutableList.of();
	@SerializedName("soundReset")
	public Boolean soundReset = null;
	@SerializedName("effectReset")
	public Boolean effectReset = null;
	@SerializedName("chance")
	public Integer chance = null;
	@SerializedName("acoustics")
	public List<AcousticConfig> acoustics = ImmutableList.of();
	@SerializedName("effects")
	public List<EffectConfig> effects = ImmutableList.of();

	@Override
	public void validate(@Nonnull final BlockConfig obj) throws ValidationException {
		ValidationHelpers.hasElements("blocks", this.blocks, Environs.LOGGER::warn);
		for (final String s : blocks) {
			ValidationHelpers.notNullOrWhitespace("blocks", s, Environs.LOGGER::warn);
			ValidationHelpers.mustBeLowerCase("blocks", s, Environs.LOGGER::warn);
		}
		for (final AcousticConfig ac : this.acoustics)
			ac.validate(ac);
		for (final EffectConfig ec : this.effects)
			ec.validate(ec);
	}
}

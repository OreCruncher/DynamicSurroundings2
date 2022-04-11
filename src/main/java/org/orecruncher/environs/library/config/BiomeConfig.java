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

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.environs.Environs;
import org.orecruncher.lib.validation.IValidator;
import org.orecruncher.lib.validation.ValidationException;
import org.orecruncher.lib.validation.ValidationHelpers;

import javax.annotation.Nonnull;
import java.util.List;

public final class BiomeConfig implements IValidator<BiomeConfig> {
	@SerializedName("conditions")
	public String conditions = StringUtils.EMPTY;
	@SerializedName("_comment")
	public String comment = null;
	@SerializedName("aurora")
	public Boolean hasAurora = null;
	@SerializedName("fogColor")
	public String fogColor = null;
	@SerializedName("visibility")
	public Float visibility = null;
	@SerializedName("soundReset")
	public Boolean soundReset = null;
	@SerializedName("spotSoundChance")
	public Integer spotSoundChance = null;
	@SerializedName("acoustics")
	public List<AcousticConfig> acoustics = ImmutableList.of();

	@Override
	public String toString() {
		return this.comment == null ? this.conditions : this.comment;
	}

	@Override
	public void validate(@Nonnull final BiomeConfig obj) throws ValidationException {
		if (this.visibility != null)
			ValidationHelpers.inRange("visibility", this.visibility, 0F, 1F, Environs.LOGGER::warn);

		for (final AcousticConfig ac : this.acoustics)
			ac.validate(ac);
	}
}

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
import org.orecruncher.lib.validation.IValidator;
import org.orecruncher.lib.validation.ValidationException;
import org.orecruncher.lib.validation.ValidationHelpers;

import javax.annotation.Nonnull;

public class AcousticConfig implements IValidator<AcousticConfig> {
	@SerializedName("acoustic")
	public String acoustic = null;
	@SerializedName("conditions")
	public String conditions = StringUtils.EMPTY;
	@SerializedName("weight")
	public int weight = 10;
	@SerializedName("type")
	public String type = "background";

	@Override
	public void validate(@Nonnull final AcousticConfig obj) throws ValidationException {
		ValidationHelpers.notNullOrWhitespace("acoustic", this.acoustic, Environs.LOGGER::warn);
		ValidationHelpers.inRange("weight", this.weight, 1, Integer.MAX_VALUE, Environs.LOGGER::warn);
		ValidationHelpers.isOneOf("type", this.type, false, new String[]{"spot", "background"}, Environs.LOGGER::warn);
	}
}

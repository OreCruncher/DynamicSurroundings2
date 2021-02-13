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

import javax.annotation.Nonnull;

import com.google.gson.annotations.SerializedName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Environs;
import org.orecruncher.lib.validation.IValidator;
import org.orecruncher.lib.validation.ValidationException;
import org.orecruncher.lib.validation.ValidationHelpers;

@OnlyIn(Dist.CLIENT)
public class DimensionConfig implements IValidator<DimensionConfig> {
	@SerializedName("dimId")
	public String dimensionId = null;
	@SerializedName("seaLevel")
	public Integer seaLevel = null;
	@SerializedName("skyHeight")
	public Integer skyHeight = null;
	@SerializedName("cloudHeight")
	public Integer cloudHeight = null;
	@SerializedName("haze")
	public Boolean hasHaze = null;
	@SerializedName("aurora")
	public Boolean hasAurora = null;
	@SerializedName("weather")
	public Boolean hasWeather = null;
	@SerializedName("fog")
	public Boolean hasFog = null;
	@SerializedName("alwaysOutside")
	public Boolean alwaysOutside = null;

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if (this.dimensionId != null)
			builder.append("dimensionId: ").append(this.dimensionId).append(" ");
		if (this.seaLevel != null)
			builder.append("seaLevel: ").append(this.seaLevel.intValue()).append(" ");
		if (this.skyHeight != null)
			builder.append("skyHeight: ").append(this.skyHeight.intValue()).append(" ");
		if (this.cloudHeight != null)
			builder.append("cloudHeight: ").append(this.cloudHeight.intValue()).append(" ");
		if (this.hasAurora != null)
			builder.append("hasAurora: ").append(this.hasAurora).append(" ");
		if (this.hasHaze != null)
			builder.append("hasHaze: ").append(this.hasHaze).append(" ");
		if (this.hasWeather != null)
			builder.append("hasWeather: ").append(this.hasWeather).append(" ");
		if (this.hasFog != null)
			builder.append("hasFog: ").append(this.hasFog).append(" ");
		if (this.alwaysOutside != null)
			builder.append("alwaysOutside: ").append(this.alwaysOutside).append(" ");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return this.dimensionId != null ? this.dimensionId.hashCode() : 0;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof DimensionConfig) {
			final DimensionConfig dc = (DimensionConfig) obj;
			return (this.dimensionId != null && this.dimensionId.equals(dc.dimensionId));
		}
		return false;
	}

	@Override
	public void validate(@Nonnull final DimensionConfig obj) throws ValidationException {
		ValidationHelpers.isProperResourceLocation("dimId", this.dimensionId, Environs.LOGGER::warn);
	}
}

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

package org.orecruncher.environs.library.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class BiomeConfig {
	@SerializedName("conditions")
	public String conditions = StringUtils.EMPTY;
	@SerializedName("_comment")
	public String comment = null;
	@SerializedName("dust")
	public Boolean hasDust = null;
	@SerializedName("aurora")
	public Boolean hasAurora = null;
	@SerializedName("fog")
	public Boolean hasFog = null;
	@SerializedName("dustColor")
	public String dustColor = null;
	@SerializedName("fogColor")
	public String fogColor = null;
	@SerializedName("fogDensity")
	public Float fogDensity = null;
	@SerializedName("soundReset")
	public Boolean soundReset = null;
	@SerializedName("spotSoundChance")
	public Integer spotSoundChance = null;
	@SerializedName("acoustics")
	public List<AcousticConfig> acoustics = ImmutableList.of();

}

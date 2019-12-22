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

package org.orecruncher.sndctrl.library.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SoundMetadataConfig {

    @SerializedName("category")
    public String category = null;
    @SerializedName("title")
    public String title = null;
    @SerializedName("subtitle")
    public String caption = null;
    @SerializedName("credits")
    public List<String> credits = ImmutableList.of();

    /**
     * Indicates whether the settings in the instance are the default settings.
     *
     * @return true if the properties are the same as defaults; false otherwise
     */
    public boolean isDefault() {
        //@formatter:off
        return StringUtils.isEmpty(this.category)
                && StringUtils.isEmpty(this.title)
                && StringUtils.isEmpty(this.caption)
                && this.credits.size() == 0;
        //@formatter:on
    }

}

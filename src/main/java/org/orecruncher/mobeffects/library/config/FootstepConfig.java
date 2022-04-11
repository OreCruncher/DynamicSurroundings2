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

package org.orecruncher.mobeffects.library.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;
import org.orecruncher.lib.validation.IValidator;
import org.orecruncher.lib.validation.ValidationException;
import org.orecruncher.lib.validation.ValidationHelpers;
import org.orecruncher.mobeffects.MobEffects;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class FootstepConfig implements IValidator<FootstepConfig> {
    @SerializedName("primitives")
    public Map<String, String> primitives = ImmutableMap.of();
    @SerializedName("blockTags")
    public Map<String, String> blockTags = ImmutableMap.of();
    @SerializedName("footsteps")
    public Map<String, String> footsteps = ImmutableMap.of();
    @SerializedName("footprints")
    public List<String> footprints = ImmutableList.of();

    @Override
    public void validate(@Nonnull final FootstepConfig obj) throws ValidationException {
        for (final String fp : this.footprints) {
            ValidationHelpers.notNullOrWhitespace("footprints", fp, MobEffects.LOGGER::warn);
            ValidationHelpers.mustBeLowerCase("footprints", fp, MobEffects.LOGGER::warn);
        }
    }
}

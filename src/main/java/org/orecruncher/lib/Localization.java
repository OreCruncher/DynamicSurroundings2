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

package org.orecruncher.lib;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.LanguageMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Localization {

    private Localization() {
    }

    @Nonnull
    public static String format(@Nonnull final String fmt, @Nullable final Object... args) {
        return I18n.format(fmt, args);
    }

    @Nonnull
    public static String load(@Nonnull final String fmt) {
        return LanguageMap.getInstance().func_230503_a_(fmt);
    }
}

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

package org.orecruncher.lib.gui;

import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class GuiHelpers {

    /**
     * Gets the text associated with the given language key that is formatted so that a line is <= the width
     * specified.
     * @param key Translation key for the associated text
     * @param width Maximum width of a line
     * @param formatting Formatting to apply to each line
     * @return Collection of ITextComponents for the given key
     */
    public static Collection<ITextComponent> getTrimmedText(@Nonnull final String key, final int width, final TextFormatting... formatting) {

        final String prefix;
        if (formatting != null && formatting.length > 0)
            prefix = StringUtils.join(formatting);
        else
            prefix = StringUtils.EMPTY;

        return GameUtils.getMC().fontRenderer.getCharacterManager()
                .func_238362_b_(
                        new TranslationTextComponent(key),
                        width,
                        Style.EMPTY)
                .stream().map(e -> new StringTextComponent(prefix + e.getString()))
                .collect(Collectors.toList());
    }
}

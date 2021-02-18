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

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class GuiHelpers {

    private final static String ELLIPSES =  "...";

    /**
     * Gets the text associated with the given language key that is formatted so that a line is <= the width
     * specified.
     *
     * @param key        Translation key for the associated text
     * @param width      Maximum width of a line
     * @param formatting Formatting to apply to each line
     * @return Collection of ITextComponents for the given key
     */
    public static Collection<ITextComponent> getTrimmedTextCollection(@Nonnull final String key, final int width, @Nullable final TextFormatting... formatting) {
        final Style style = prefixHelper(formatting);
        return GameUtils.getMC().fontRenderer.getCharacterManager()
                .func_238362_b_(
                        new TranslationTextComponent(key),
                        width,
                        style)
                .stream().map(e -> new StringTextComponent(e.getString()))
                .collect(Collectors.toList());
    }

    /**
     * Gets the text associated with the given language key.  Text is truncated to the specified width and an
     * ellipses append if necessary.
     *
     * @param key        Translation key for the associated text
     * @param width      Maximum width of the text in GUI pixels
     * @param formatting Formatting to apply to the text
     * @return ITextComponent fitting the criteria specified
     */
    public static ITextComponent getTrimmedText(@Nonnull final String key, final int width, @Nullable final TextFormatting... formatting) {
        final Style style = prefixHelper(formatting);
        final ITextComponent text = new TranslationTextComponent(key);
        final FontRenderer fr = GameUtils.getMC().fontRenderer;
        final CharacterManager cm = fr.getCharacterManager();
        if (fr.getStringPropertyWidth(text) > width) {
            final int ellipsesWidth = fr.getStringWidth(ELLIPSES);
            final int trueWidth = width - ellipsesWidth;
            final ITextProperties str = cm.func_238358_a_(text, trueWidth, style);
            return new StringTextComponent(str.getString() + ELLIPSES);
        }
        final ITextProperties str = cm.func_238358_a_(text, width, style);
        return new StringTextComponent(str.getString());
    }

    private static Style prefixHelper(@Nullable final TextFormatting[] formatting) {
        final Style style;
        if (formatting != null && formatting.length > 0)
            style = Style.EMPTY.createStyleFromFormattings(formatting);
        else
            style = Style.EMPTY;
        return style;
    }
}

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

package org.orecruncher.lib.gui;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public final class ColorPalette {
    // Minecraft colors mapped to codes
    public static final Color MC_BLACK = new Color(TextFormatting.BLACK);
    public static final Color MC_DARKBLUE = new Color(TextFormatting.DARK_BLUE);
    public static final Color MC_DARKGREEN = new Color(TextFormatting.DARK_GREEN);
    public static final Color MC_DARKAQUA = new Color(TextFormatting.DARK_AQUA);
    public static final Color MC_DARKRED = new Color(TextFormatting.DARK_RED);
    public static final Color MC_DARKPURPLE = new Color(TextFormatting.DARK_PURPLE);
    public static final Color MC_GOLD = new Color(TextFormatting.GOLD);
    public static final Color MC_GRAY = new Color(TextFormatting.GRAY);
    public static final Color MC_DARKGRAY = new Color(TextFormatting.DARK_GRAY);
    public static final Color MC_BLUE = new Color(TextFormatting.BLUE);
    public static final Color MC_GREEN = new Color(TextFormatting.GREEN);
    public static final Color MC_AQUA = new Color(TextFormatting.AQUA);
    public static final Color MC_RED = new Color(TextFormatting.RED);
    public static final Color MC_LIGHTPURPLE = new Color(TextFormatting.LIGHT_PURPLE);
    public static final Color MC_YELLOW = new Color(TextFormatting.YELLOW);
    public static final Color MC_WHITE = new Color(TextFormatting.WHITE);

    public static final Color RED = new Color(255, 0, 0);
    public static final Color ORANGE = new Color(255, 127, 0);
    public static final Color YELLOW = new Color(255, 255, 0);
    public static final Color LGREEN = new Color(127, 255, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color TURQOISE = new Color(0, 255, 127);
    public static final Color CYAN = new Color(0, 255, 255);
    public static final Color AUQUAMARINE = new Color(0, 127, 255);
    public static final Color BLUE = new Color(0, 0, 255);
    public static final Color VIOLET = new Color(127, 0, 255);
    public static final Color MAGENTA = new Color(255, 0, 255);
    public static final Color RASPBERRY = new Color(255, 0, 127);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color PURPLE = new Color(80, 0, 80);
    public static final Color INDIGO = new Color(75, 0, 130);
    public static final Color NAVY = new Color(0, 0, 128);
    public static final Color TAN = new Color(210, 180, 140);
    public static final Color GOLD = new Color(255, 215, 0);
    public static final Color GRAY = new Color(128, 128, 128);
    public static final Color LGRAY = new Color(192, 192, 192);
    public static final Color SLATEGRAY = new Color(112, 128, 144);
    public static final Color DARKSLATEGRAY = new Color(47, 79, 79);

    public static final Color AURORA_RED = new Color(1.0F, 0F, 0F);
    public static final Color AURORA_GREEN = new Color(0.5F, 1.0F, 0.0F);
    public static final Color AURORA_BLUE = new Color(0F, 0.8F, 1.0F);

    private ColorPalette() {

    }

}

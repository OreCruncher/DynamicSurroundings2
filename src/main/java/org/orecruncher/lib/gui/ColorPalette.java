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
    private ColorPalette() {

    }

}

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
package org.orecruncher.lib.gui.config;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SettingsParameters {
    private SettingsParameters() {

    }

    /**
     * Empty string reference to satisfy logic that expects an ITextComponent
     */
    public static final ITextComponent EMPTY = new StringTextComponent("");

    public static final ITextComponent CANCEL_BUTTON_TEXT = new TranslationTextComponent("gui.cancel");

    public static final ITextComponent DONE_BUTTON_TEXT = new TranslationTextComponent("gui.done");

    /**
     * Distance between this GUI's title and the top of the screen
     */
    public static final int TITLE_HEIGHT = 8;

    /**
     * Distance between the options list's top and the top of the screen
     */
    public static final int OPTIONS_LIST_TOP_HEIGHT = 24;

    /**
     * Distance between the options list's bottom and the bottom of the screen
     */
    public static final int OPTIONS_LIST_BOTTOM_OFFSET = 32;

    /**
     * Distance between the top of each button below the options list and the
     * bottom of the screen
     */
    public static final int BOTTOM_BUTTON_HEIGHT_OFFSET = 26;

    /**
     * Height of each item in the options list
     */
    public static final int OPTIONS_LIST_ITEM_HEIGHT = 25;

    /**
     * Width of each button below the options list
     */
    public static final int BOTTOM_BUTTON_WIDTH = 150;

    /**
     * Vertical distance between borders of two adjacent buttons
     */
    public static final int BUTTONS_INTERVAL = 4;

    /**
     * Height of buttons on this mod's GUI
     */
    public static final int BUTTON_HEIGHT = 20;

    /**
     * Vertical distance between the top of a button and the top of the button
     * right below it
     */
    public static final int BUTTONS_TRANSLATION_INTERVAL = BUTTON_HEIGHT + BUTTONS_INTERVAL;
}

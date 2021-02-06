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

package org.orecruncher.sndctrl.api.sound;

import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public interface ISoundCategory {

    /**
     * Gets the internal name of the category
     */
    String getName();

    /**
     * Get the category name suitable for display
     */
    ITextComponent getTextComponent();

    /**
     * Should the category show up in the quick volume set menu
     */
    default boolean doQuickMenu() {
        return false;
    }

    /**
     * Should occlusion processing be performed for sounds of the category
     */
    boolean doOcclusion();

    /**
     * Should any effects be applied to sounds of the category
     */
    boolean doEffects();

    /**
     * Obtains the volume scale factor for the category
     */
    float getVolumeScale();

    /**
     * Sets the volume scale factor for the category
     */
    void setVolumeScale(final float scale);

    /**
     * Get the underlying Minecraft category
     * @return
     */
    @Nonnull
    default SoundCategory getRealCategory() {
        // Do not change from MASTER.  This gets passed down into the Minecraft engine so it can make
        // decisions about what to do.  If tied to a different category it could result in some strange
        // behaviors.
        return SoundCategory.MASTER;
    }
}
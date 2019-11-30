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

package org.orecruncher.sndctrl.audio.handlers.effects;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * https://kcat.strangesoft.net/misc-downloads/Effects%20Extension%20Guide.pdf
 */
@OnlyIn(Dist.CLIENT)
public abstract class EffectBase<T extends EffectData> {

    private static final int NULL_SLOT = 0;
    private static final int NULL_ID = 0;
    protected int id = NULL_ID;
    protected int slot = NULL_SLOT;
    protected boolean process = true;

    protected EffectBase() {
    }

    /**
     * Indicaets whether this effect has been initialized
     *
     * @return true if it has been initialized, false otherwise
     */
    public boolean isInitialized() {
        return this.slot != NULL_SLOT;
    }

    /**
     * Gets the slot of the effect.  If it has not been initialized a safe value is returned.
     *
     * @return Slot ID of the effect; 0 if not available
     */
    public int getSlot() {
        return isInitialized() ? this.slot : NULL_SLOT;
    }

    /**
     * Gets the id of the effect.  If it has not been initialized a safe value is returned.
     *
     * @return ID of the effect; 0 if not available
     */
    public int getId() {
        return isInitialized() ? this.id : NULL_ID;
    }

    /**
     * Initializes the effect.  If it has already been initialized the request is ignored.
     */
    public final void initialize() {
        if (!isInitialized()) {
            this.init0();
        }
    }

    protected abstract void init0();

    /**
     * Applies the specified data parameters to the effect.  If the parameters do not match the effect an
     * exception will be thrown.
     *
     * @param data Data parameters for the effect.
     */
    public abstract void apply(@Nonnull final T data);

}

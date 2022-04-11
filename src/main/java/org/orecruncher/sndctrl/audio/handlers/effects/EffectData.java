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


/**
 * The state context for a given effect.
 */
public abstract class EffectData {

    protected boolean process;

    protected EffectData() {
        this.process = false;
    }

    /**
     * Indicates if the data set should be applied as an effect
     *
     * @return true to apply data; false otherwise
     */
    public boolean doProcess() {
        return this.process;
    }

    /**
     * Sets whether the data should be applied to a sound source.
     *
     * @param flag true to indicate data should be applied; false otherwise
     */
    public void setProcess(final boolean flag) {
        this.process = flag;
    }

    /**
     * Ensures that the effect data is properly bounded.
     */
    public abstract void clamp();
}

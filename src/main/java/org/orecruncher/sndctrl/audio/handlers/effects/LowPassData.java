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

import org.lwjgl.openal.EXTEfx;
import org.orecruncher.lib.math.MathStuff;

public final class LowPassData extends EffectData {

    public float gain = EXTEfx.AL_LOWPASS_DEFAULT_GAIN;
    public float gainHF = EXTEfx.AL_LOWPASS_DEFAULT_GAINHF;

    public LowPassData() {
    }

    /**
     * Ensures that the effect data is properly bounded.
     */
    @Override
    public void clamp() {
        this.gain = MathStuff.clamp(this.gain, EXTEfx.AL_LOWPASS_MIN_GAIN, EXTEfx.AL_LOWPASS_MAX_GAIN);
        this.gainHF = MathStuff.clamp(this.gainHF, EXTEfx.AL_LOWPASS_MIN_GAINHF, EXTEfx.AL_LOWPASS_MAX_GAINHF);
    }
}

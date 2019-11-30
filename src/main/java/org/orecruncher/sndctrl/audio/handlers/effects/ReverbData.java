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

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;
import org.orecruncher.lib.math.MathStuff;

public final class ReverbData extends EffectData {

    // Defaults based on spec
    public float density = EXTEfx.AL_REVERB_DEFAULT_DENSITY;
    public float diffusion = EXTEfx.AL_REVERB_DEFAULT_DIFFUSION;
    public float gain = EXTEfx.AL_REVERB_DEFAULT_GAIN;
    public float gainHF = EXTEfx.AL_REVERB_DEFAULT_GAINHF;
    public float decayTime = EXTEfx.AL_REVERB_DEFAULT_DECAY_TIME;
    public float decayHFRatio = EXTEfx.AL_REVERB_DEFAULT_DECAY_HFRATIO;
    public float reflectionsGain = EXTEfx.AL_REVERB_DEFAULT_REFLECTIONS_GAIN;
    public float reflectionsDelay = EXTEfx.AL_REVERB_DEFAULT_REFLECTIONS_DELAY;
    public float lateReverbGain = EXTEfx.AL_REVERB_DEFAULT_LATE_REVERB_GAIN;
    public float lateReverbDelay = EXTEfx.AL_REVERB_DEFAULT_LATE_REVERB_DELAY;
    public float airAbsorptionGainHF = EXTEfx.AL_REVERB_DEFAULT_AIR_ABSORPTION_GAINHF;
    public float roomRolloffFactor = EXTEfx.AL_REVERB_DEFAULT_ROOM_ROLLOFF_FACTOR;
    public int decayHFLimit = AL10.AL_TRUE;

    public ReverbData() {

    }

    @Override
    public void clamp() {
        this.density = MathStuff.clamp(this.density, EXTEfx.AL_REVERB_MIN_DENSITY, EXTEfx.AL_REVERB_MAX_DENSITY);
        this.diffusion = MathStuff.clamp(this.diffusion, EXTEfx.AL_REVERB_MIN_DIFFUSION, EXTEfx.AL_REVERB_MAX_DIFFUSION);
        this.gain = MathStuff.clamp(this.gain, EXTEfx.AL_REVERB_MIN_GAIN, EXTEfx.AL_REVERB_MAX_GAIN);
        this.gainHF = MathStuff.clamp(this.gainHF, EXTEfx.AL_REVERB_MIN_GAINHF, EXTEfx.AL_REVERB_MAX_GAINHF);
        this.decayTime = MathStuff.clamp(this.decayTime, EXTEfx.AL_REVERB_MIN_DECAY_TIME, EXTEfx.AL_REVERB_MAX_DECAY_TIME);
        this.decayHFRatio = MathStuff.clamp(this.decayHFRatio, EXTEfx.AL_REVERB_MIN_DECAY_HFRATIO, EXTEfx.AL_REVERB_MAX_DECAY_HFRATIO);
        this.reflectionsGain = MathStuff.clamp(this.reflectionsGain, EXTEfx.AL_REVERB_MIN_REFLECTIONS_GAIN, EXTEfx.AL_REVERB_MAX_REFLECTIONS_GAIN);
        this.reflectionsDelay = MathStuff.clamp(this.reflectionsDelay, EXTEfx.AL_REVERB_MIN_REFLECTIONS_DELAY, EXTEfx.AL_REVERB_MAX_REFLECTIONS_DELAY);
        this.lateReverbGain = MathStuff.clamp(this.lateReverbGain, EXTEfx.AL_REVERB_MIN_LATE_REVERB_GAIN, EXTEfx.AL_REVERB_MAX_LATE_REVERB_GAIN);
        this.lateReverbDelay = MathStuff.clamp(this.reflectionsDelay, EXTEfx.AL_REVERB_MIN_LATE_REVERB_DELAY, EXTEfx.AL_REVERB_MAX_LATE_REVERB_DELAY);
        this.airAbsorptionGainHF = MathStuff.clamp(this.airAbsorptionGainHF, EXTEfx.AL_REVERB_MIN_AIR_ABSORPTION_GAINHF, EXTEfx.AL_REVERB_MAX_AIR_ABSORPTION_GAINHF);
        this.roomRolloffFactor = MathStuff.clamp(this.roomRolloffFactor, EXTEfx.AL_REVERB_MIN_ROOM_ROLLOFF_FACTOR, EXTEfx.AL_REVERB_MAX_ROOM_ROLLOFF_FACTOR);
        this.decayHFLimit = MathStuff.clamp(this.decayHFLimit, AL10.AL_FALSE, AL10.AL_TRUE);
    }
}

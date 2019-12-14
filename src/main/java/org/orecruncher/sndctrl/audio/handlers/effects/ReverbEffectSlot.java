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
import org.lwjgl.openal.EXTEfx;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class ReverbEffectSlot extends Slot {

    public ReverbEffectSlot() {
        super(EXTEfx::alGenEffects);
    }

    @Override
    protected void init0() {
        EXTEfx.alEffecti(getSlot(), EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
    }

    public void apply(@Nonnull final ReverbData data, @Nonnull final AuxSlot aux) {
        if (isInitialized()) {
            if (data.doProcess()) {
                data.clamp();
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_DENSITY, data.density);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_DIFFUSION, data.diffusion);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_GAIN, data.gain);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_GAINHF, data.gainHF);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_DECAY_TIME, data.decayTime);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_DECAY_HFRATIO, data.decayHFRatio);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_REFLECTIONS_GAIN, data.reflectionsGain);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_REFLECTIONS_DELAY, data.reflectionsDelay);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_LATE_REVERB_GAIN, data.lateReverbGain);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_LATE_REVERB_DELAY, data.lateReverbDelay);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_AIR_ABSORPTION_GAINHF, data.airAbsorptionGainHF);
                EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_ROOM_ROLLOFF_FACTOR, data.roomRolloffFactor);
                EXTEfx.alEffecti(getSlot(), EXTEfx.AL_EAXREVERB_DECAY_HFLIMIT, data.decayHFLimit);
                EXTEfx.alAuxiliaryEffectSloti(aux.getSlot(), EXTEfx.AL_EFFECTSLOT_EFFECT, getSlot());
            } else {
                EXTEfx.alAuxiliaryEffectSloti(aux.getSlot(), EXTEfx.AL_EFFECTSLOT_EFFECT, EXTEfx.AL_EFFECTSLOT_NULL);
            }
            check("ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT");
        }
    }
}

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
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_DENSITY, data.density), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT density");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_DIFFUSION, data.diffusion), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT diffusion");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_GAIN, data.gain), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT gain");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_GAINHF, data.gainHF), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT gainHF");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_DECAY_TIME, data.decayTime), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT decayTime");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_DECAY_HFRATIO, data.decayHFRatio), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT decayHFRatio");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_REFLECTIONS_GAIN, data.reflectionsGain), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT reflectionsGain");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_REFLECTIONS_DELAY, data.reflectionsDelay), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT reflectionsDelay");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_LATE_REVERB_GAIN, data.lateReverbGain), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT lateReverbGain");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_LATE_REVERB_DELAY, data.lateReverbDelay), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT lateReverbDelay");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_AIR_ABSORPTION_GAINHF, data.airAbsorptionGainHF), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT airAbsorptionGainHF");
                execute(() -> EXTEfx.alEffectf(getSlot(), EXTEfx.AL_EAXREVERB_ROOM_ROLLOFF_FACTOR, data.roomRolloffFactor), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT roomRolloffFactor");
                execute(() -> EXTEfx.alEffecti(getSlot(), EXTEfx.AL_EAXREVERB_DECAY_HFLIMIT, data.decayHFLimit), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT decayHFLimit");
                execute(() -> EXTEfx.alAuxiliaryEffectSloti(aux.getSlot(), EXTEfx.AL_EFFECTSLOT_EFFECT, getSlot()), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT upload");
            } else {
                execute(() -> EXTEfx.alAuxiliaryEffectSloti(aux.getSlot(), EXTEfx.AL_EFFECTSLOT_EFFECT, EXTEfx.AL_EFFECTSLOT_NULL), () -> "ReverbEffectSlot EXTEfx.AL_EFFECTSLOT_EFFECT null");
            }
        }
    }
}

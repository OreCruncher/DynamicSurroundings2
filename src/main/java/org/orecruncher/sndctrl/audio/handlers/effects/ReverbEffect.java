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
import org.orecruncher.sndctrl.audio.handlers.SoundFXProcessor;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public final class ReverbEffect extends SpecialEffect<ReverbData> {

    @Override
    protected void init0() {
        super.init0();
        EXTEfx.alEffecti(getId(), EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_REVERB);
        EXTEfx.alAuxiliaryEffectSloti(getSlot(), EXTEfx.AL_EFFECTSLOT_EFFECT, getId());
    }

    @Override
    public void apply(@Nonnull final ReverbData data) {
        if (isInitialized()) {
            synchronized (data.sync) {
                data.clamp();
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_DENSITY, data.density);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_DIFFUSION, data.diffusion);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_GAIN, data.gain);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_GAINHF, data.gainHF);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_DECAY_TIME, data.decayTime);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_DECAY_HFRATIO, data.decayHFRatio);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_REFLECTIONS_GAIN, data.reflectionsGain);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_REFLECTIONS_DELAY, data.reflectionsDelay);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_LATE_REVERB_GAIN, data.lateReverbGain);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_LATE_REVERB_DELAY, data.lateReverbDelay);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_AIR_ABSORPTION_GAINHF, data.airAbsorptionGainHF);
                EXTEfx.alEffectf(getId(), EXTEfx.AL_REVERB_ROOM_ROLLOFF_FACTOR, data.roomRolloffFactor);
                EXTEfx.alEffecti(getId(), EXTEfx.AL_REVERB_DECAY_HFLIMIT, data.decayHFLimit);
                SoundFXProcessor.validate("ReverbEffect::apply");
            }
        }
    }

}

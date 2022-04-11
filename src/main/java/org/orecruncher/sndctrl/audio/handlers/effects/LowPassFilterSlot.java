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

import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;

import javax.annotation.Nonnull;

public class LowPassFilterSlot extends Slot {

    public LowPassFilterSlot() {
        super(EXTEfx::alGenFilters);
    }

    @Override
    protected void init0() {
        EXTEfx.alFilteri(getSlot(), EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
    }

    public void apply(final int sourceId, @Nonnull final LowPassData data) {
        if (isInitialized()) {
            if (data.doProcess()) {
                data.clamp();
                execute(() -> EXTEfx.alFilterf(getSlot(), EXTEfx.AL_LOWPASS_GAIN, data.gain), () -> "LowPassFilterSlot EXTEfx.AL_DIRECT_FILTER gain");
                execute(() -> EXTEfx.alFilterf(getSlot(), EXTEfx.AL_LOWPASS_GAINHF, data.gainHF), () -> "LowPassFilterSlot EXTEfx.AL_DIRECT_FILTER gainHF");
                execute(() -> AL11.alSourcei(sourceId, EXTEfx.AL_DIRECT_FILTER, getSlot()), () -> "LowPassFilterSlot EXTEfx.AL_DIRECT_FILTER upload");
            } else {
                execute(() -> AL11.alSourcei(sourceId, EXTEfx.AL_DIRECT_FILTER, EXTEfx.AL_EFFECTSLOT_NULL), () -> "LowPassFilterSlot EXTEfx.AL_DIRECT_FILTER null");
            }
        }
    }

    public void apply(final int sourceId, @Nonnull final LowPassData data, final int auxSend, @Nonnull final AuxSlot aux) {
        if (isInitialized()) {
            if (data.doProcess()) {
                data.clamp();
                execute(() -> EXTEfx.alFilterf(getSlot(), EXTEfx.AL_LOWPASS_GAIN, data.gain), () -> "LowPassFilterSlot EXTEfx.AL_AUXILIARY_SEND_FILTER gain");
                execute(() -> EXTEfx.alFilterf(getSlot(), EXTEfx.AL_LOWPASS_GAINHF, data.gainHF), () -> "LowPassFilterSlot EXTEfx.AL_AUXILIARY_SEND_FILTER gainHF");
                execute(() -> AL11.alSource3i(sourceId, EXTEfx.AL_AUXILIARY_SEND_FILTER, aux.getSlot(), auxSend, getSlot()), () -> "LowPassFilterSlot EXTEfx.AL_AUXILIARY_SEND_FILTER upload");
            } else {
                execute(() -> AL11.alSource3i(sourceId, EXTEfx.AL_AUXILIARY_SEND_FILTER, EXTEfx.AL_EFFECTSLOT_NULL, auxSend, EXTEfx.AL_FILTER_NULL), () -> "LowPassFilterSlot EXTEfx.AL_AUXILIARY_SEND_FILTER null");
            }
        }
    }
}

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
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
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
                EXTEfx.alFilterf(getSlot(), EXTEfx.AL_LOWPASS_GAIN, data.gain);
                EXTEfx.alFilterf(getSlot(), EXTEfx.AL_LOWPASS_GAINHF, data.gainHF);
                AL11.alSourcei(sourceId, EXTEfx.AL_DIRECT_FILTER, getSlot());
            } else {
                AL11.alSourcei(sourceId, EXTEfx.AL_DIRECT_FILTER, EXTEfx.AL_EFFECTSLOT_NULL);
            }
            check("LowPassFilterSlot EXTEfx.AL_DIRECT_FILTER");
        }
    }

    public void apply(final int sourceId, @Nonnull final LowPassData data, final int auxSend, @Nonnull final AuxSlot aux) {
        if (isInitialized()) {
            if (data.doProcess()) {
                data.clamp();
                EXTEfx.alFilterf(getSlot(), EXTEfx.AL_LOWPASS_GAIN, data.gain);
                EXTEfx.alFilterf(getSlot(), EXTEfx.AL_LOWPASS_GAINHF, data.gainHF);
                AL11.alSource3i(sourceId, EXTEfx.AL_AUXILIARY_SEND_FILTER, aux.getSlot(), auxSend, getSlot());
            } else {
                AL11.alSource3i(sourceId, EXTEfx.AL_AUXILIARY_SEND_FILTER, EXTEfx.AL_EFFECTSLOT_NULL, auxSend, EXTEfx.AL_FILTER_NULL);
            }
            check("LowPassFilterSlot EXTEfx.AL_AUXILIARY_SEND_FILTER");
        }
    }
}

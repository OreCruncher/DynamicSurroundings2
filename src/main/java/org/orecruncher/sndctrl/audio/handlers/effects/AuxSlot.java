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

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;

public class AuxSlot extends Slot {

    public AuxSlot() {
        super(EXTEfx::alGenAuxiliaryEffectSlots);
    }

    @Override
    protected void init0() {
        execute(() -> EXTEfx.alAuxiliaryEffectSloti(getSlot(), EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE), () -> "AuxSlot EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO");
    }
}

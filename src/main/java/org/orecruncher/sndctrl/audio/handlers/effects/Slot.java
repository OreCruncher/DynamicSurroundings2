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
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public abstract class Slot {

    @Nonnull
    private final Supplier<Integer> factory;
    private int slot = EXTEfx.AL_EFFECTSLOT_NULL;

    public Slot(@Nonnull final Supplier<Integer> slotFactory) {
        this.factory = slotFactory;
    }

    public boolean isInitialized() {
        return this.slot != EXTEfx.AL_EFFECTSLOT_NULL;
    }

    public final void initialize() {
        if (this.slot == EXTEfx.AL_EFFECTSLOT_NULL) {
            this.slot = this.factory.get();
            check();
            this.init0();
            check();
        }
    }

    protected abstract void init0();

    public int getSlot() {
        return this.slot;
    }

    protected void check() {
        SoundFXProcessor.validate();
    }
}

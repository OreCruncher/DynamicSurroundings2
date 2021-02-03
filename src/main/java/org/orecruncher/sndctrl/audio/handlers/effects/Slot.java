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
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;
import org.orecruncher.lib.Lib;
import org.orecruncher.lib.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public abstract class Slot {

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
            execute(() -> this.slot = this.factory.get(), () -> "Slot factory get");
            execute(this::init0, () -> "Slot init0");
        }
    }

    public final void deinitialize() {
        this.slot = EXTEfx.AL_EFFECTSLOT_NULL;
    }

    protected abstract void init0();

    public int getSlot() {
        return this.slot;
    }

    protected void execute(@Nonnull final Runnable func) {
        execute(func, null);
    }

    protected void execute(@Nonnull final Runnable func, @Nullable final Supplier<String> context) {
        func.run();
        final int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            String errorName = AL10.alGetString(error);
            if (StringUtils.isEmpty(errorName))
                errorName = Integer.toString(error);
            final String msg = Utilities.firstNonNull(context != null ? context.get() : null, "NONE");
            Lib.LOGGER.warn(String.format("OpenAL Error: %s [%s]", errorName, msg));
        }
    }
}

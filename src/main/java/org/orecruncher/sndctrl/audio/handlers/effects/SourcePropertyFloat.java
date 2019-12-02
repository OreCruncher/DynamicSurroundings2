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
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.sndctrl.audio.handlers.SoundFXProcessor;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public final class SourcePropertyFloat {

    @Nonnull
    private final Object sync = new Object();

    private final int property;
    private final float min;
    private final float max;
    private float value;
    private boolean process;

    public SourcePropertyFloat(@Nonnull final int property, final float val, final float min, final float max) {
        this.property = property;
        this.min = min;
        this.max = max;
        this.process = false;
    }

    @Nonnull
    public final Object sync() {
        return this.sync;
    }

    public boolean doProcess() {
        return this.process;
    }

    public void setProcess(final boolean flag) {
        this.process = flag;
    }

    public float getValue() {
        return this.value;
    }

    public void setValue(final float f) {
        this.value = MathStuff.clamp(f, this.min, this.max);
    }

    public void apply(final int sourceId) {
        if (doProcess()) {
            synchronized (this.sync) {
                AL11.alSourcef(sourceId, this.property, getValue());
                SoundFXProcessor.validate();
            }
        }
    }
}
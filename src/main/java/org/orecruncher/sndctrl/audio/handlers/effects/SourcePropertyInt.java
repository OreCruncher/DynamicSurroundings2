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

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public final class SourcePropertyInt {

    private final int property;
    private final int min;
    private final int max;
    private int value;
    private boolean process;

    public SourcePropertyInt(final int property, final int val, final int min, final int max) {
        this.property = property;
        this.min = min;
        this.max = max;
        this.value = val;
        this.process = false;
    }

    public boolean doProcess() {
        return this.process;
    }

    public void setProcess(final boolean flag) {
        this.process = flag;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(final int f) {
        this.value = MathStuff.clamp(f, this.min, this.max);
    }

    public void apply(final int sourceId) {
        if (doProcess()) {
            AL11.alSourcei(sourceId, this.property, getValue());
            SoundFXProcessor.validate("SourcePropertyInt apply");
        }
    }
}
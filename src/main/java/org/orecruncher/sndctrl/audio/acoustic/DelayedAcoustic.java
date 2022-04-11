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

package org.orecruncher.sndctrl.audio.acoustic;

import com.google.common.base.MoreObjects;
import net.minecraft.util.ResourceLocation;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * An acoustic that will delay play for a random period of ticks.  The sound instance will be held in the sound engine
 * until the delay threshold is hit.
 */
public class DelayedAcoustic extends SimpleAcoustic {

    private static final Random RANDOM = XorShiftRandom.current();

    private int delayMin;
    private int delayMax;

    public DelayedAcoustic(@Nonnull final ResourceLocation name, @Nonnull AcousticFactory factory) {
        super(name, factory);
    }

    public void setDelay(final int delay) {
        this.delayMin = this.delayMax = delay;
    }

    public void setDelayMin(final int min) {
        this.delayMin = min;
    }

    public void setDelayMax(final int max) {
        this.delayMax = max;
    }

    public boolean hasDelay() {
        return this.delayMax > 0;
    }

    @Override
    protected void play(@Nonnull final ISoundInstance sound) {
        if (hasDelay()) {
            int delay = this.delayMin;
            if (this.delayMax > this.delayMin)
                delay += RANDOM.nextInt(this.delayMax - this.delayMin);
            sound.setPlayDelay(delay);
        }
        super.play(sound);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(getName().toString()).toString();
    }
}

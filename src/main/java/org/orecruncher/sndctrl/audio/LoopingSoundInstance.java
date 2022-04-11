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

package org.orecruncher.sndctrl.audio;

import com.google.common.base.MoreObjects;
import net.minecraft.util.math.vector.Vector3d;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;

import javax.annotation.Nonnull;

public class LoopingSoundInstance extends WrappedSoundInstance {

    private final Vector3d position;

    public LoopingSoundInstance(@Nonnull final ISoundInstance sound, @Nonnull final ISoundCategory category) {
        super(sound, category);
        this.position = null;
    }

    public LoopingSoundInstance(@Nonnull final ISoundInstance sound) {
        super(sound);
        this.position = null;
    }

    public LoopingSoundInstance(@Nonnull final ISoundInstance sound, @Nonnull final Vector3d position) {
        super(sound);
        this.position = position;
    }

    @Override
    public boolean canRepeat() {
        return true;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public int getPlayDelay() {
        return this.sound.getPlayDelay();
    }

    @Override
    public void setPlayDelay(final int delay) {
        this.sound.setPlayDelay(delay);
    }

    @Override
    public double getX() {
        return this.position != null ? (float) this.position.x : super.getX();
    }

    @Override
    public double getY() {
        return this.position != null ? (float) this.position.y : super.getY();
    }

    @Override
    public double getZ() {
        return this.position != null ? (float) this.position.z : super.getZ();
    }

    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.LINEAR;
    }

    @Override
    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(getSoundLocation().toString())
                .addValue(getSoundCategory().toString())
                .addValue(getState().toString())
                .add("v", getVolume())
                .add("ev", SoundInstance.getEffectiveVolume(this))
                .add("p", getPitch())
                .add("x", getX())
                .add("y", getY())
                .add("z", getZ())
                .toString();
    }

}

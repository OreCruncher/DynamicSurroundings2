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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class PlayerCenteredSoundInstance extends WrappedSoundInstance {
    public PlayerCenteredSoundInstance(@Nonnull final ISoundInstance sound, @Nonnull final ISoundCategory category) {
        super(sound, category);
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.NONE;
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double getZ() {
        return 0;
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
                .toString();
    }
}

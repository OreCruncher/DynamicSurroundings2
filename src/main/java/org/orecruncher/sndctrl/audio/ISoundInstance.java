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

package org.orecruncher.sndctrl.audio;

import net.minecraft.client.audio.ISound;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Additional state information associated with a sound instance.
 */
@OnlyIn(Dist.CLIENT)
public interface ISoundInstance extends ISound {

    /**
     * The current SoundState of the sound instance.
     *
     * @return SoundState of the sound instance
     */
    @Nonnull
    SoundState getState();

    /**
     * Sets the SoundState of the sound instance to the specified value.
     *
     * @param state SoundState to set for the SoundInstance
     */
    void setState(@Nonnull final SoundState state);

    /**
     * Indicates if the sound can be muted by the other major sound sources, like
     * battle music.
     *
     * @return true if the sound instance can be muted, false otherwise.
     */
    boolean canMute();

}

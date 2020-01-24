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

package org.orecruncher.sndctrl.api.acoustics;

import net.minecraft.client.audio.ISound;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.audio.SoundState;

import javax.annotation.Nonnull;

/**
 * Additional state information associated with a sound instance.
 */
@OnlyIn(Dist.CLIENT)
public interface ISoundInstance extends ISound {

    /**
     * The sound category the sound instance belongs to.
     *
     * @return The category the sound belongs to
     */
    ISoundCategory getSoundCategory();

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
     * The number of ticks to delay playing the sound.  Keep in mind a tick is 50msecs.
     *
     * @return Number of ticks to delay play
     */
    int getPlayDelay();

    /**
     * Sets the delay in ticks for the playing sound.  Keep in mind a tick is 50 msecs.
     *
     * @param delay Number of ticks to delay play
     */
    void setPlayDelay(final int delay);

    /**
     * Indicates if the sound is delayed when played
     *
     * @return true if the sound is to be delayed, false otherwise
     */
    default boolean isDelayed() {
        return getPlayDelay() > 0;
    }

}

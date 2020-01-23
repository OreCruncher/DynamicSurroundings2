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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Used by sounds that have long term state that gets manipulated
 * by the sound engine.  Intended to mitigate the constant polling
 * of the sound engine by mod logic to figure out what is happening
 * with a sound.
 */

@OnlyIn(Dist.CLIENT)
public enum SoundState {
    /**
     * The sound was just created.
     */
    NONE(false, false),
    /**
     * The sound is in the process of being evaluated for play.
     */
    QUEUING(false, true),
    /**
     * Currently playing in the sound engine.
     */
    PLAYING(true, false),
    /**
     * The sound is in the sound engine DELAYED queue waiting to play.
     */
    DELAYED(true, false),
    /**
     * The sound is in the process of being stopped.
     */
    STOPPING(true, false),
    /**
     * The sound has completed it play.
     */
    DONE(false, true),
    /**
     * The sound was blocked from playing because it was explicitly blocked or culled by SoundControl, some
     * other mod decided to kill the sound (like a sound muffler), or the calculated volume would be too low
     * to be heard.
     */
    BLOCKED(false, true),
    /**
     * The sound instance was replaced by another mod via the Forge hooks.  Mods that do volume scaling due to
     * their game mechanics (blocks, items, etc.) recreate the sound with the adjusted parameters.  This is why
     * SoundControl uses ASM to do volume scaling - the originating mod isn't aware that it is happening.
     */
    REPLACED(false, true),
    /**
     * There was an error of some sort playing the sound.
     */
    ERROR(false, true);

    private final boolean isActive;
    private final boolean isTerminal;

    SoundState(final boolean active, final boolean terminal) {
        this.isActive = active;
        this.isTerminal = terminal;
    }

    /**
     * A sound in this state is actively queued in the SoundManager.
     *
     * @return true if this is an active state; false otherwise
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * A sound in this state is considered terminal. It was processed by the
     * SoundManager and has reached a state where it has completed either because it
     * ran it's course or ended in error.
     *
     * @return true if this is a terminal state; false otherwise
     */
    public boolean isTerminal() {
        return this.isTerminal;
    }
}

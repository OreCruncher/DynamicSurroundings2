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

package org.orecruncher.sndctrl.events;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@SuppressWarnings("unused")
public class AudioEvent extends Event {
    public AudioEvent() {
    }

    /**
     * Raised by sound system to determine if music should fade in volume. Canceling
     * the event will cause the MusicFader to fade music. Otherwise, the volume will
     * return to normal levels. As an example, Dynamic Surroundings battle music
     * will fade out the background music while battle music is playing.
     */
    @Cancelable
    public final static class MusicFadeAudioEvent extends AudioEvent {
        public MusicFadeAudioEvent() {
        }
    }

}

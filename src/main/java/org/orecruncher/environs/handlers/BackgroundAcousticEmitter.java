/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.handlers;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Environs;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.IAcousticFactory;
import org.orecruncher.sndctrl.api.sound.IFadableSoundInstance;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.audio.SoundState;

/*
 * Emitters are used to produce sounds that are continuous
 * or on repeat. They ensure that the sound is always queue
 * in the sound system even if the underlying sound system
 * cancels the sound.
 */
@OnlyIn(Dist.CLIENT)
public final class BackgroundAcousticEmitter {

	// Number of ticks to standoff requing a sound if for some reason it is replaced
	// by mod logic.  Don't want to keep spamming.
	private static final int REPLACED_STANDOFF = 33;

	@Nonnull
	protected final IAcoustic acousticSource;
	@Nonnull
	protected final IFadableSoundInstance activeSound;

	protected int standoff = 0;
	protected boolean done = false;

	public BackgroundAcousticEmitter(@Nonnull final IAcoustic acoustic) {
		this.acousticSource = acoustic;
		this.activeSound = acoustic.getFactory().createBackgroundSound();
	}

	@Nonnull
	public IAcoustic getSource() {
		return this.acousticSource;
	}

	public void tick() {

		// If the current sound is playing and the sound is fading just terminate the sound.
		if (this.activeSound.getState().isActive()) {
			if ((isFading() && this.activeSound.getState() == SoundState.DELAYED)) {
				AudioEngine.stop(this.activeSound);
			}
			return;
		} else if (isFading()) {
			// If we get here the sound is no longer playing and is in the
			// fading state. This is possible because the actual sound
			// volume down in the engine could have hit 0 but the tick
			// handler on the sound did not have a chance to get there
			// first.
			this.done = true;
			return;
		}

		// Play the sound if need be
		if (!this.activeSound.getState().isActive()) {
			// Don't proceed if we are standing off
			if (this.standoff > 0) {
				this.standoff--;
			} else {
				AudioEngine.play(this.activeSound);
				if (this.activeSound.getState() == SoundState.REPLACED) {
					this.standoff = REPLACED_STANDOFF;
				}
			}
		}
	}

	public void setVolumeThrottle(final float throttle) {
		this.activeSound.setFadeVolume(throttle);
	}

	public void fade() {
		Environs.LOGGER.debug("FADE: %s", this.activeSound.toString());
		this.activeSound.fade();
	}

	public boolean isFading() {
		return this.activeSound.isFading();
	}

	public void unfade() {
		Environs.LOGGER.debug("UNFADE: %s", this.activeSound.toString());
		this.activeSound.unfade();
	}

	public boolean isDonePlaying() {
		return this.done || this.activeSound.isDonePlaying();
	}

	public void stop() {
		AudioEngine.stop(this.activeSound);
	}

	@Override
	public String toString() {
		return this.activeSound.toString();
	}

}

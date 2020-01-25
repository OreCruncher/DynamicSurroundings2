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

import com.google.common.base.MoreObjects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.sndctrl.api.acoustics.IFadableSoundInstance;
import org.orecruncher.sndctrl.api.acoustics.ISoundInstance;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FadableSoundInstance extends WrappedSoundInstance implements IFadableSoundInstance {

    private static final float INITIAL_FADE = 0.00002F;
    private static final float FADE_AMOUNT = 0.02F;

    private boolean isFading;
    private float fadeScale;
    private float fadeScaleTarget;
    private boolean isDonePlaying;
    private long lastTick;

    public FadableSoundInstance(@Nonnull final ISoundInstance sound) {
        super(sound);

        this.fadeScale = INITIAL_FADE;
        this.fadeScaleTarget = 1F;
        this.lastTick = TickCounter.getTickCount();
    }

    @Override
    public float getVolume() {
        return super.getVolume() * this.fadeScale;
    }

    @Override
    public boolean isDonePlaying() {
        return this.isDonePlaying || super.isDonePlaying();
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
    public void tick() {

        // If we are being ticked again, dont process
        final long tickDelta = TickCounter.getTickCount() - this.lastTick;
        if (tickDelta < 1)
            return;

        super.tick();

        // If we are done playing just return
        if (isDonePlaying())
            return;

        // Update our last tick amount
        this.lastTick = TickCounter.getTickCount();

        // Adjust the fadeScale so it moves to the proper value.
        if ((this.fadeScale < this.fadeScaleTarget) && !isFading())
            this.fadeScale += FADE_AMOUNT * tickDelta;
        else if (isFading() || this.fadeScale > this.fadeScaleTarget) {
            this.fadeScale -= FADE_AMOUNT * tickDelta;
        }

        // Clamp it so we are valid
        this.fadeScale = MathStuff.clamp1(this.fadeScale);

        // Do the float compare.  Takes into account epsilon.
        if (Float.compare(this.fadeScale, 0) == 0) {
            this.isDonePlaying = true;
        }
    }

    @Override
    public void fade() {
        this.isFading = true;
    }

    @Override
    public void unfade() {
        this.isFading = false;
    }

    @Override
    public boolean isFading() {
        return this.isFading;
    }

    /**
     * Set's the fade scale target.  The volume of the sound will glide up/down to this volume
     * over time.
     *
     * @param scale Value between 0F and 1F inclusive.
     */
    @Override
    public void setFadeVolume(final float scale) {
        this.fadeScaleTarget = MathStuff.clamp1(scale);
    }

    @Override
    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(getSoundLocation().toString())
                .addValue(getSoundCategory().toString())
                .addValue(getState().toString())
                .add("v", getVolume())
                .add("p", getPitch())
                .add("f", this.fadeScale)
                .toString();
    }

}

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
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.math.MathStuff;

import javax.annotation.Nonnull;

/**
 * A BackgroundSoundInstance is intended to play continuously in the background, similar to the music
 * of Minecraft.  The difference here is that the volume can fade in and out.  Used by Dynamic Surroundings
 * to scale background sound volumes based on biome distribution.
 */
@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public final class BackgroundSoundInstance extends WrappedSoundInstance {

    private static final float DONE_FADE_THRESHOLD = 0.00001F;
    private static final float FADE_AMOUNT = 0.02F;

    private boolean isFading;
    private float fadeScale;
    private float fadeScaleTarget;
    private boolean isDonePlaying;
    private long lastTick;

    public BackgroundSoundInstance(@Nonnull final ISoundInstance sound) {
        super(sound);

        this.fadeScale = DONE_FADE_THRESHOLD * 2;
        this.fadeScaleTarget = 1F;
        this.lastTick = TickCounter.getTickCount();
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public boolean canRepeat() {
        // ASM is used to increase the buffer size 4x.  Reason is the loop code in the sound engine loops on cached
        // buffers, meaning the entire sound has to be loaded.  If for some reason this breaks you will get a choppy
        // termination before restarting the loop.
        return true;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return super.getVolume() * this.fadeScale;
    }

    @Override
    public float getX() {
        return 0;
    }

    @Override
    public float getY() {
        return 0;
    }

    @Override
    public float getZ() {
        return 0;
    }

    public void fade() {
        this.isFading = true;
    }

    public void unfade() {
        this.isFading = false;
    }

    public boolean isFading() {
        return this.isFading;
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

    @Nonnull
    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.NONE;
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

        // If the fadeScale amount is beneath our low end threshold the sound
        // is effectively done.
        if (this.fadeScale < DONE_FADE_THRESHOLD) {
            this.isDonePlaying = true;
            this.fadeScale = 0F;
        }
    }

    /**
     * Set's the fade scale target.  The volume of the sound will drift up/down to this scale factor
     * over time.
     *
     * @param scale Value between 0F and 1F inclusive.
     */
    public void setFadeScaleTarget(final float scale) {
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

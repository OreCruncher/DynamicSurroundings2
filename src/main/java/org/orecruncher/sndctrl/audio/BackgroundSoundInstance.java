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

import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
        this.lastTick = TickCounter.getTickCount() + 1;
    }

    @Nonnull
    @Override
    public SoundCategory getCategory() {
        // Background sounds are always ambient
        return SoundCategory.AMBIENT;
    }

    @Nonnull
    @Override
    public ISoundCategory getSoundCategory() {
        return Category.AMBIENT;
    }

    @Override
    public boolean canRepeat() {
        return !isDonePlaying() && super.canRepeat();
    }

    @Override
    public boolean isGlobal() {
        // Background sounds are always global
        return true;
    }

    @Override
    public float getVolume() {
        return super.getVolume() * this.fadeScale;
    }

    @Override
    public float getX() {
        // Always 0
        return 0;
    }

    @Override
    public float getY() {
        // Always 0
        return 0;
    }

    @Override
    public float getZ() {
        // Always 0
        return 0;
    }

    @Nonnull
    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.NONE;
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
    public boolean canMute() {
        return true;
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
        if (tickDelta == 0)
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
}

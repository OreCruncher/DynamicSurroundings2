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

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Base class for special sounds that aggregate the true sound being played.
 */
@OnlyIn(Dist.CLIENT)
public class WrappedSoundInstance implements ISoundInstance, ITickableSound {

    @Nonnull
    protected final ISoundInstance sound;
    @Nonnull
    protected final ISoundCategory category;

    public WrappedSoundInstance(@Nonnull final ISoundInstance sound, @Nonnull final ISoundCategory category) {
        this.sound = Objects.requireNonNull(sound);
        this.category = Objects.requireNonNull(category);
    }

    public WrappedSoundInstance(@Nonnull final ISoundInstance sound) {
        this.sound = Objects.requireNonNull(sound);
        this.category = sound.getSoundCategory();
    }

    @Override
    public boolean isDonePlaying() {
        return getState().isTerminal();
    }

    @Override
    public void tick() {
        if (this.sound instanceof ITickableSound)
            ((ITickableSound) this.sound).tick();
    }

    @Nonnull
    @Override
    public SoundState getState() {
        return this.sound.getState();
    }

    @Override
    public void setState(@Nonnull SoundState state) {
        this.sound.setState(state);
    }

    @Override
    public int getPlayDelay() {
        return this.sound.getPlayDelay();
    }

    @Override
    public void setPlayDelay(int delay) {
        this.sound.setPlayDelay(delay);
    }

    @Nonnull
    @Override
    public ResourceLocation getSoundLocation() {
        return this.sound.getSoundLocation();
    }

    @Nullable
    @Override
    public SoundEventAccessor createAccessor(SoundHandler handler) {
        return this.sound.createAccessor(handler);
    }

    @Nonnull
    @Override
    public Sound getSound() {
        return this.sound.getSound();
    }

    @Nonnull
    @Override
    public SoundCategory getCategory() {
        return this.sound.getCategory();
    }

    @Nonnull
    @Override
    public ISoundCategory getSoundCategory() {
        return this.category;
    }

    @Override
    public boolean canRepeat() {
        return this.sound.canRepeat();
    }

    @Override
    public boolean isGlobal() {
        return this.sound.isGlobal();
    }

    @Override
    public int getRepeatDelay() {
        return this.sound.getRepeatDelay();
    }

    @Override
    public float getVolume() {
        return this.sound.getVolume();
    }

    @Override
    public float getPitch() {
        return this.sound.getPitch();
    }

    @Override
    public double getX() {
        return this.sound.getX();
    }

    @Override
    public double getY() {
        return this.sound.getY();
    }

    @Override
    public double getZ() {
        return this.sound.getZ();
    }

    @Nonnull
    @Override
    public AttenuationType getAttenuationType() {
        return this.sound.getAttenuationType();
    }
}

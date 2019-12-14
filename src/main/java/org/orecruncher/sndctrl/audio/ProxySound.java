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
import net.minecraft.client.audio.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * Simple wrapper for an ISound instance that got substituted for our orignal sound play.  This is possible because
 * another mod might have replaced it.  For example, mods that have equipment items that alter sound perception
 * commonly wrap the sound we submit with a proxy.
 */
@OnlyIn(Dist.CLIENT)
public final class ProxySound implements IProxySound, ISoundInstance, ITickableSound {

    @Nonnull
    private final ISound sound;

    @Nonnull
    private final ISoundInstance original;

    @Nonnull
    private final Optional<ITickableSound> tickable;

    @Nonnull
    private SoundState state;

    ProxySound(@Nonnull final ISoundInstance original, @Nonnull final ISound sound) {
        this.original = Objects.requireNonNull(original);
        this.sound = Objects.requireNonNull(sound);
        this.state = SoundState.ERROR;  // When constructed the overall submission state is ERROR
        this.tickable = Utilities.safeCast(this.sound, ITickableSound.class);
    }

    @Nonnull
    @Override
    public ISound getTrueSound() {
        return this.sound;
    }

    @Nonnull
    @Override
    public ISoundInstance getOriginalSound() {
        return this.original;
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
        return this.original.getSoundCategory();
    }

    @Override
    public boolean canRepeat() {
        return false;
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
    public float getX() {
        return this.sound.getX();
    }

    @Override
    public float getY() {
        return this.sound.getY();
    }

    @Override
    public float getZ() {
        return this.sound.getZ();
    }

    @Nonnull
    @Override
    public AttenuationType getAttenuationType() {
        return this.sound.getAttenuationType();
    }

    @Nonnull
    @Override
    public SoundState getState() {
        return this.state;
    }

    @Override
    public void setState(@Nonnull SoundState state) {
        this.state = state;
    }

    @Override
    public boolean canMute() {
        return this.sound.getCategory() == SoundCategory.MUSIC;
    }

    @Override
    public boolean isDonePlaying() {
        return this.tickable.map(ITickableSound::isDonePlaying).orElse(false);
    }

    @Override
    public int getPlayDelay() {
        return this.original.getPlayDelay();
    }

    @Override
    public void setPlayDelay(final int delay) {
        this.original.setPlayDelay(delay);
    }

    @Override
    public void tick() {
        this.tickable.ifPresent(ITickableSound::tick);
    }

    @Nonnull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("aggregate", this.sound.toString()).toString();
    }
}

/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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

package org.orecruncher.sndctrl.audio.acoustic;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.orecruncher.sndctrl.api.acoustics.AcousticEvent;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.IAcousticFactory;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A simple acoustic that uses an AcousticFactory to produce sound instances for playing.
 */
public class SimpleAcoustic implements IAcoustic {

    private final AcousticFactory factory;
    private final ResourceLocation name;

    public SimpleAcoustic(@Nonnull final SoundEvent event, @Nonnull final ISoundCategory category) {
        this(event.getName(), event);
        this.factory.setCategory(category);
    }

    public SimpleAcoustic(@Nonnull final SoundEvent event) {
        this(event.getName(), event);
    }

    public SimpleAcoustic(@Nonnull final ResourceLocation name, @Nonnull final SoundEvent evt) {
        this(name, new AcousticFactory(evt));
    }

    public SimpleAcoustic(@Nonnull final ResourceLocation name, @Nonnull final AcousticFactory factory) {
        this.name = Objects.requireNonNull(name);
        this.factory = factory;
    }

    @Nonnull
    public AcousticFactory getFactory() {
        return this.factory;
    }

    @Nonnull
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public void play(@Nonnull final AcousticEvent ignored) {
        play(this.factory.createSound());
    }

    @Override
    public void playAt(@Nonnull final BlockPos pos, @Nonnull final AcousticEvent ignored) {
        play(this.factory.createSoundAt(pos));
    }

    @Override
    public void playAt(@Nonnull final Vector3d pos, @Nonnull final AcousticEvent ignored) {
        play(this.factory.createSoundAt(pos));
    }

    @Override
    public void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent ignored) {
        play(this.factory.createSoundNear(entity));
    }

    @Override
    public void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent ignored, final int minRange, final int maxRange) {
        play(this.factory.createSoundNear(entity, minRange, maxRange));
    }

    @Override
    public void playBackground(@Nonnull final AcousticEvent ignored) {
        play(this.factory.createBackgroundSound());
    }

    @Override
    public IAcousticFactory getFactory(@Nonnull final AcousticEvent event) {
        return this.factory;
    }

    protected void play(@Nonnull final ISoundInstance sound) {
        AudioEngine.play(sound);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(getName().toString()).toString();
    }
}

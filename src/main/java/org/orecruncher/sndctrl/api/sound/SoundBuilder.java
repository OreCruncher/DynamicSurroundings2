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

package org.orecruncher.sndctrl.api.sound;

import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.sndctrl.audio.SoundInstance;
import org.orecruncher.sndctrl.library.SoundLibrary;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Random;

/**
 * SoundBuilder is a factory object that produces ISoundInstances within the specified parameters.
 */
@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class SoundBuilder {

    private static final Random RANDOM = XorShiftRandom.current();
    private static final float[] volumeDelta = {-0.2F, 0.0F, 0.0F, 0.2F, 0.2F, 0.2F};
    private static final float[] pitchDelta = {-0.2F, 0.0F, 0.0F, 0.2F, 0.2F, 0.2F};

    @Nonnull
    private final SoundEvent soundEvent;
    @Nonnull
    private ISoundCategory soundCategory;
    @Nonnull
    private Vector3d position = Vector3d.ZERO;
    @Nonnull
    private AttenuationType attenuation = AttenuationType.LINEAR;

    private float volumeMin = 1F;
    private float volumeMax = 1F;
    private float pitchMin = 1F;
    private float pitchMax = 1F;
    private boolean repeatable;
    private int repeatDelayMin;
    private int repeatDelayMax;
    private boolean global;

    private boolean variableVolume;
    private boolean variablePitch;

    private int playDelay;

    public SoundBuilder(@Nonnull final SoundBuilder builder) {
        this.soundEvent = builder.soundEvent;
        this.soundCategory = builder.soundCategory;
        this.position = builder.position;
        this.attenuation = builder.attenuation;
        this.volumeMin = builder.volumeMin;
        this.volumeMax = builder.volumeMax;
        this.pitchMin = builder.pitchMin;
        this.pitchMax = builder.pitchMax;
        this.repeatable = builder.repeatable;
        this.repeatDelayMin = builder.repeatDelayMin;
        this.repeatDelayMax = builder.repeatDelayMax;
        this.global = builder.global;
        this.variableVolume = builder.variableVolume;
        this.variablePitch = builder.variablePitch;
        this.playDelay = builder.playDelay;
    }

    protected SoundBuilder(@Nonnull final SoundEvent evt) {
        this(evt, Category.NEUTRAL);
    }

    protected SoundBuilder(@Nonnull final SoundEvent evt, @Nonnull final ISoundCategory cat) {
        Objects.requireNonNull(evt);
        Objects.requireNonNull(cat);

        this.soundEvent = evt;
        this.soundCategory = cat;
    }

    @Nonnull
    public static SoundBuilder builder(@Nonnull final SoundEvent evt) {
        return builder(evt, SoundLibrary.getSoundCategory(evt.getName(), Category.AMBIENT));
    }

    @Nonnull
    public static SoundBuilder builder(@Nonnull final SoundEvent evt, @Nonnull final ISoundCategory cat) {
        return new SoundBuilder(evt, cat);
    }

    @Nonnull
    public static SoundBuilder builder(@Nonnull final SoundInstance proto) {
        Objects.requireNonNull(proto);
        final SoundEvent se = SoundLibrary.getSound(proto.getSoundLocation()).orElseThrow(NullPointerException::new);
        final ISoundCategory sc = Category.getCategory(proto.getCategory()).orElseThrow(NullPointerException::new);
        return new SoundBuilder(se, sc).from(proto);
    }

    @Nonnull
    public static SoundInstance create(@Nonnull final SoundEvent evt, @Nonnull final ISoundCategory cat) {
        return new SoundInstance(evt, cat);
    }

    @Nonnull
    public static ISoundInstance createConfigPlay(@Nonnull final String name, final float volume) {
        Objects.requireNonNull(name);

        final ResourceLocation resource = new ResourceLocation(name);
        final SoundEvent se = SoundLibrary.getSound(resource).orElseThrow(NullPointerException::new);
        final ISoundCategory cat = SoundLibrary.getSoundCategory(resource, Category.MASTER);
        final SoundBuilder builder = new SoundBuilder(se, cat);
        builder.setVolume(volume);
        builder.setAttenuation(AttenuationType.NONE);
        return builder.build();
    }

    @Nonnull
    public SoundBuilder from(@Nonnull final LocatableSound ps) {
        Objects.requireNonNull(ps);

        this.soundCategory = Category.getCategory(ps.getCategory()).orElse(Category.MASTER);
        this.position = new Vector3d(ps.getX(), ps.getY(), ps.getZ());
        this.attenuation = ps.getAttenuationType();
        this.global = ps.isGlobal();
        this.repeatable = ps.canRepeat();
        this.repeatDelayMin = this.repeatDelayMax = ps.getRepeatDelay();

        this.volumeMin = this.volumeMax = ps.volume;
        this.pitchMin = this.pitchMax = ps.pitch;
        return this;
    }

    @Nonnull
    public ResourceLocation getResourceName() {
        return this.soundEvent.getName();
    }

    @Nonnull
    public SoundBuilder setCategory(@Nonnull final ISoundCategory cat) {
        this.soundCategory = cat;
        return this;
    }

    @Nonnull
    public SoundBuilder setPosition(final float x, final float y, final float z) {
        this.position = new Vector3d(x, y, z);
        return this;
    }

    @Nonnull
    public SoundBuilder setPosition(@Nonnull final BlockPos pos) {
        Objects.requireNonNull(pos);

        return setPosition(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
    }

    @Nonnull
    public SoundBuilder setPosition(@Nonnull final Vector3d pos) {
        Objects.requireNonNull(pos);

        this.position = pos;
        return this;
    }

    /**
     * Sets the possible volume range for a sound instance.  Will disable variable volume setting.
     */
    @Nonnull
    public SoundBuilder setVolumeRange(final float min, final float max) {
        this.volumeMin = MathStuff.min(min, max);
        this.volumeMax = MathStuff.max(min, max);
        this.variableVolume = false;
        return this;
    }

    @Nonnull
    public SoundBuilder setVariableVolume(final boolean f) {
        this.variableVolume = f;
        if (f)
            this.volumeMax = this.volumeMin;
        return this;
    }

    /**
     * Sets the possible pitch range for a sound instance.  Will disable variable pitch setting.
     */
    @Nonnull
    public SoundBuilder setPitchRange(final float min, final float max) {
        this.pitchMin = MathStuff.min(min, max);
        this.pitchMax = MathStuff.max(min, max);
        this.variablePitch = false;
        return this;
    }

    @Nonnull
    public SoundBuilder setVariablePitch(final boolean f) {
        this.variablePitch = f;
        if (f)
            this.pitchMax = this.pitchMin;
        return this;
    }

    @Nonnull
    public SoundBuilder setRepeateDelayRange(final int min, final int max) {
        this.repeatable = true;
        this.repeatDelayMin = MathStuff.min(min, max);
        this.repeatDelayMax = MathStuff.max(min, max);
        return this;
    }

    @Nonnull
    public SoundBuilder setGlobal(final boolean flag) {
        this.global = flag;
        return this;
    }

    private float getVolume() {
        if (Float.compare(this.volumeMin, this.volumeMax) == 0) {
            float result = this.volumeMin;
            if (this.variableVolume)
                result *= 1F + volumeDelta[RANDOM.nextInt(volumeDelta.length)];
            return result;
        }
        return this.volumeMin + RANDOM.nextFloat() * (this.volumeMax - this.volumeMin);
    }

    @Nonnull
    public SoundBuilder setVolume(final float v) {
        this.volumeMin = this.volumeMax = v;
        return this;
    }

    private float getPitch() {
        if (Float.compare(this.pitchMin, this.pitchMax) == 0) {
            float result = this.pitchMin;
            if (this.variablePitch)
                result *= 1F + pitchDelta[RANDOM.nextInt(pitchDelta.length)];
            return result;
        }
        return this.pitchMin + RANDOM.nextFloat() * (this.pitchMax - this.pitchMin);
    }

    @Nonnull
    public SoundBuilder setPitch(final float p) {
        this.pitchMin = this.pitchMax = p;
        return this;
    }

    private int getRepeatDelay() {
        if (this.repeatDelayMin == this.repeatDelayMax)
            return this.repeatDelayMin;
        return this.repeatDelayMin + RANDOM.nextInt(this.repeatDelayMax - this.repeatDelayMin + 1);
    }

    @Nonnull
    public SoundBuilder setRepeatDelay(final int delay) {
        this.repeatable = true;
        this.repeatDelayMin = this.repeatDelayMax = delay;
        return this;
    }

    @Nonnull
    public SoundBuilder setAttenuation(final AttenuationType type) {
        Objects.requireNonNull(type);
        this.attenuation = type;
        return this;
    }

    @Nonnull
    public SoundBuilder setPlayDelay(final int delay) {
        this.playDelay = delay;
        return this;
    }

    @Nonnull
    protected SoundInstance makeSound() {
        final SoundInstance sound = create(this.soundEvent, this.soundCategory);
        sound.setVolume(this.getVolume());
        sound.setPitch(this.getPitch());
        sound.setRepeat(this.repeatable);
        sound.setRepeatDelay(this.getRepeatDelay());
        sound.setGlobal(this.global);
        sound.setPlayDelay(this.playDelay);

        if (!this.global) {
            sound.setPosition(this.position);
            sound.setAttenuationType(this.attenuation);
        } else {
            sound.setAttenuationType(AttenuationType.NONE);
        }

        return sound;
    }

    @Nonnull
    public ISoundInstance build() {
        return makeSound();
    }

}

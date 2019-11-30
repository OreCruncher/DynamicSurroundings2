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

import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.sndctrl.mixins.ILocatableSoundMixin;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Random;

/**
 * SoundBuilder is a factory object that produces ISoundInstances within the specified parameters.
 */
@OnlyIn(Dist.CLIENT)
public final class SoundBuilder {

    private static final Random RANDOM = XorShiftRandom.current();

    @Nonnull
    private final SoundEvent soundEvent;
    @Nonnull
    private SoundCategory soundCategory;
    @Nonnull
    private Vec3d position = Vec3d.ZERO;
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
    private boolean canMute;

    private SoundBuilder(@Nonnull final SoundEvent evt, @Nonnull final SoundCategory cat) {
        Objects.requireNonNull(evt);
        Objects.requireNonNull(cat);

        this.soundEvent = evt;
        this.soundCategory = cat;
    }

    @Nonnull
    public static SoundBuilder builder(@Nonnull final SoundEvent evt) {
        return builder(evt, SoundCategory.AMBIENT);
    }

    @Nonnull
    public static SoundBuilder builder(@Nonnull final SoundEvent evt, @Nonnull final SoundCategory cat) {
        return new SoundBuilder(evt, cat);
    }

    @Nonnull
    public static SoundBuilder builder(@Nonnull final SoundInstance proto) {
        Objects.requireNonNull(proto);

        final SoundEvent se = SoundRegistry.getSound(proto.getSoundLocation());
        final SoundCategory sc = proto.getCategory();
        return new SoundBuilder(se, sc).from(proto);
    }

    @Nonnull
    public static SoundInstance create(@Nonnull final SoundEvent evt, @Nonnull final SoundCategory cat) {
        return new SoundInstance(evt, cat);
    }

    @Nonnull
    public static ISoundInstance createConfigPlay(@Nonnull final String name, final float volume) {
        Objects.requireNonNull(name);

        final ResourceLocation resource = new ResourceLocation(name);
        final SoundEvent se = SoundRegistry.getSound(resource);
        final SoundCategory cat = SoundRegistry.getSoundCategory(resource, SoundCategory.MASTER);
        final SoundBuilder builder = new SoundBuilder(se, cat);
        builder.setVolume(volume);
        builder.setCanMute(false);
        builder.setAttenuation(SoundUtils.noAttenuation());
        return builder.build();
    }

    @Nonnull
    public SoundBuilder from(@Nonnull final LocatableSound ps) {
        Objects.requireNonNull(ps);

        this.soundCategory = ps.getCategory();
        this.position = new Vec3d(ps.getX(), ps.getY(), ps.getZ());
        this.attenuation = ps.getAttenuationType();
        this.global = ps.isGlobal();
        this.repeatable = ps.canRepeat();
        this.repeatDelayMin = this.repeatDelayMax = ps.getRepeatDelay();
        this.canMute = this.soundCategory == SoundCategory.MUSIC;

        final ILocatableSoundMixin sound = (ILocatableSoundMixin) ps;
        this.volumeMin = this.volumeMax = sound.getVolumeRaw();
        this.pitchMin = this.pitchMax = sound.getPitchRaw();
        return this;
    }

    @Nonnull
    public SoundBuilder setPosition(final float x, final float y, final float z) {
        this.position = new Vec3d(x, y, z);
        return this;
    }

    @Nonnull
    public SoundBuilder setPosition(@Nonnull final BlockPos pos) {
        Objects.requireNonNull(pos);

        return setPosition(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
    }

    @Nonnull
    public SoundBuilder setPosition(@Nonnull final Vec3d pos) {
        Objects.requireNonNull(pos);

        this.position = pos;
        return this;
    }

    @Nonnull
    public SoundBuilder setVolumeRange(final float min, final float max) {
        this.volumeMin = Math.min(min, max);
        this.volumeMax = Math.max(min, max);
        return this;
    }

    @Nonnull
    public SoundBuilder setPitchRange(final float min, final float max) {
        this.pitchMin = Math.min(min, max);
        this.pitchMax = Math.max(min, max);
        return this;
    }

    @Nonnull
    public SoundBuilder setRepeateDelayRange(final int min, final int max) {
        this.repeatable = true;
        this.repeatDelayMin = Math.min(min, max);
        this.repeatDelayMax = Math.max(min, max);
        return this;
    }

    @Nonnull
    public SoundBuilder setGlobal(final boolean flag) {
        this.global = flag;
        return this;
    }

    private float getVolume() {
        if (Float.compare(this.volumeMin, this.volumeMax) == 0)
            return this.volumeMin;
        return this.volumeMin + RANDOM.nextFloat() * (this.volumeMax - this.volumeMin);
    }

    @Nonnull
    public SoundBuilder setVolume(final float v) {
        this.volumeMin = this.volumeMax = v;
        return this;
    }

    private float getPitch() {
        if (Float.compare(this.pitchMin, this.pitchMax) == 0)
            return this.pitchMin;
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
    public SoundBuilder setCanMute(final boolean f) {
        this.canMute = f;
        return this;
    }

    @Nonnull
    public ISoundInstance build() {
        final SoundInstance sound = create(this.soundEvent, this.soundCategory);
        sound.setVolume(this.getVolume());
        sound.setPitch(this.getPitch());
        sound.setRepeat(this.repeatable);
        sound.setRepeatDelay(this.getRepeatDelay());
        sound.setGlobal(this.global);
        sound.setCanMute(this.canMute);

        if (!this.global) {
            sound.setPosition(this.position);
            sound.setAttenuationType(this.attenuation);
        } else {
            sound.setAttenuationType(SoundUtils.noAttenuation());
        }

        return sound;
    }

    @Nonnull
    public ISoundInstance build(@Nonnull Entity entity) {
        Objects.requireNonNull(entity);
        return new EntitySoundInstance(entity, build());
    }

}

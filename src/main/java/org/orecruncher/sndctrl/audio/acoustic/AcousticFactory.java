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

package org.orecruncher.sndctrl.audio.acoustic;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.random.LCGRandom;
import org.orecruncher.sndctrl.api.acoustics.IAcousticFactory;
import org.orecruncher.sndctrl.api.sound.IFadableSoundInstance;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.audio.BackgroundSoundInstance;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;
import org.orecruncher.sndctrl.api.sound.SoundBuilder;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.audio.EntitySoundInstance;
import org.orecruncher.sndctrl.audio.SoundInstance;

import javax.annotation.Nonnull;

/**
 * Helper that creates sound instances using a SoundBuilder, but tweaks based on the circumstances requested.
 */
@OnlyIn(Dist.CLIENT)
public class AcousticFactory extends SoundBuilder implements IAcousticFactory {

    private static final int SOUND_RANGE = 12;
    private static final LCGRandom RANDOM = new LCGRandom();

    public AcousticFactory(@Nonnull final SoundEvent event) {
        super(event, Category.AMBIENT);
    }

    public AcousticFactory(@Nonnull final SoundEvent event, @Nonnull final ISoundCategory category) {
        super(event, category);
    }

    private static float randomRange() {
        return RANDOM.nextInt(AcousticFactory.SOUND_RANGE) - RANDOM.nextInt(AcousticFactory.SOUND_RANGE);
    }

    /**
     * Creates a sound instance that is non-attenuated
     */
    @Override
    @Nonnull
    public ISoundInstance createSound() {
        return createSoundAt(BlockPos.ZERO);
    }

    /**
     * Creates a sound instance centered in the block at the specified position.
     */
    @Override
    @Nonnull
    public ISoundInstance createSoundAt(@Nonnull final BlockPos pos) {
        final SoundInstance sound = makeSound();
        sound.setPosition(pos);
        return sound;
    }

    /**
     * Creates a sound instance centered at the specified location.
     */
    @Override
    @Nonnull
    public ISoundInstance createSoundAt(@Nonnull final Vector3d pos) {
        final SoundInstance sound = makeSound();
        sound.setPosition(pos);
        return sound;
    }

    /**
     * Creates a sound instance within a random range centered on the specified entity.
     */
    @Override
    @Nonnull
    public ISoundInstance createSoundNear(@Nonnull final Entity entity) {
        final float posX = (float) (entity.getPosX() + randomRange());
        final float posY = (float) (entity.getPosY() + entity.getEyeHeight() + randomRange());
        final float posZ = (float) (entity.getPosZ() + randomRange());
        final SoundInstance sound = makeSound();
        sound.setPosition(posX, posY, posZ);
        return sound;
    }

    /**
     * Attaches a sound instance to the specified entity, and will move as the entity moves.
     */
    @Override
    @Nonnull
    public ISoundInstance attachSound(@Nonnull final Entity entity) {
        return new EntitySoundInstance(entity, makeSound());
    }

    /**
     * Creates a sound instance with no attenuation and is not affected by range or other sound effects.
     */
    @Override
    @Nonnull
    public IFadableSoundInstance createBackgroundSound() {
        return new BackgroundSoundInstance(createSound());
    }

}

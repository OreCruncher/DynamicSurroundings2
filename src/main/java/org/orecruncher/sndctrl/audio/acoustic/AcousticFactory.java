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

import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.sndctrl.audio.BackgroundSoundInstance;
import org.orecruncher.sndctrl.audio.Category;
import org.orecruncher.sndctrl.audio.ISoundInstance;
import org.orecruncher.sndctrl.audio.SoundBuilder;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Helper that creates sound instances using a SoundBuilder, but tweaks based on the circumstances requested.
 */
@OnlyIn(Dist.CLIENT)
public class AcousticFactory implements IAcousticFactory {

    private static final int SOUND_RANGE = 12;
    private static final Random RANDOM = XorShiftRandom.current();

    @Nonnull
    private final SoundBuilder builder;

    public AcousticFactory(@Nonnull final SoundBuilder builder) {
        this.builder = builder;
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
        final SoundBuilder copy = new SoundBuilder(this.builder);
        return copy
                .setAttenuation(ISound.AttenuationType.NONE)
                .setPosition(Vec3d.ZERO).build();
    }

    /**
     * Creates a sound instance centered in the block at the specified position.
     */
    @Override
    @Nonnull
    public ISoundInstance createSoundAt(@Nonnull final BlockPos pos) {
        return this.builder.setPosition(pos).build();
    }

    /**
     * Creates a sound instance centered at the specified location.
     */
    @Override
    @Nonnull
    public ISoundInstance createSoundAt(@Nonnull final Vec3d pos) {
        return this.builder.setPosition(pos).build();
    }

    /**
     * Creates a sound instance within a random range centered on the specified entity.
     */
    @Override
    @Nonnull
    public ISoundInstance createSoundNear(@Nonnull final Entity entity) {
        // TODO: Test with headphones!  May need adjustments to sound right if the sound is centered on the player.
        final float posX = (float) (entity.posX + randomRange());
        final float posY = (float) (entity.posY + entity.getEyeHeight() + randomRange());
        final float posZ = (float) (entity.posZ + randomRange());
        return this.builder.setPosition(posX, posY, posZ).build();
    }

    /**
     * Attaches a sound instance to the specified entity, and will move as the entity moves.
     */
    @Override
    @Nonnull
    public ISoundInstance attachSound(@Nonnull final Entity entity) {
        return this.builder.build(entity);
    }

    /**
     * Creates a sound instance with no attenuation and is not affected by range or other sound effects.
     */
    @Override
    @Nonnull
    public ISoundInstance createBackgroundSound() {
        final SoundBuilder copy = new SoundBuilder(this.builder);
        final ISoundInstance sound = copy
                .setAttenuation(ISound.AttenuationType.NONE)
                .setPosition(Vec3d.ZERO)
                .setCategory(Category.AMBIENT)
                .build();
        return new BackgroundSoundInstance(sound);
    }

}

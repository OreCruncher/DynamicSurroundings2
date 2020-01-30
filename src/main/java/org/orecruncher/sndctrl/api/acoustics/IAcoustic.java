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

package org.orecruncher.sndctrl.api.acoustics;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.audio.acoustic.AcousticException;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public interface IAcoustic {

    /**
     * Get's the name of the acoustic, if any
     *
     * @return Name of the acoustic
     */
    ResourceLocation getName();

    /**
     * Play the acoustic on the MASTER channel with no attenuation.
     */
    default void play() {
        play(AcousticEvent.NONE);
    }

    void play(@Nonnull final AcousticEvent event);

    /**
     * Play the acoustic at the specified block position.
     */
    default void playAt(@Nonnull final BlockPos pos) {
        playAt(pos, AcousticEvent.NONE);
    }

    void playAt(@Nonnull final BlockPos pos, @Nonnull final AcousticEvent event);

    /**
     * Play the acoustic at the specified block position
     */
    default void playAt(@Nonnull final Vec3d pos) {
        playAt(pos, AcousticEvent.NONE);
    }

    void playAt(@Nonnull final Vec3d pos, @Nonnull final AcousticEvent event);

    /**
     * Play the acoustic near the entity
     */
    default void playNear(@Nonnull final Entity entity) {
        playNear(entity, AcousticEvent.NONE);
    }

    void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent event);

    /**
     * Play the acoustic in the background
     */
    default void playBackground() {
        playBackground(AcousticEvent.NONE);
    }

    void playBackground(@Nonnull final AcousticEvent event);

    /**
     * Obtains the underlying factory for creating sounds.
     */
    default IAcousticFactory getFactory() {
        return getFactory(AcousticEvent.NONE);
    }

    IAcousticFactory getFactory(@Nonnull final AcousticEvent event);
}

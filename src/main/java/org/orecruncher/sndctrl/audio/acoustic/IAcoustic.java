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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public interface IAcoustic {

    /**
     * Get's the name of the acoustic, if any
     *
     * @return Name of the acoustic
     */
    String getName();

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
}

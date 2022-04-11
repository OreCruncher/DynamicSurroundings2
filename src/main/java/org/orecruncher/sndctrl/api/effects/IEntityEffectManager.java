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

package org.orecruncher.sndctrl.api.effects;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nonnull;

public interface IEntityEffectManager {

    boolean isActive();

    @Nonnull
    LivingEntity getEntity();

    boolean isEntityAlive();

    double rangeToPlayerSq();

    boolean isFirstPersonView();

    void addParticle(@Nonnull final Particle particle);

    boolean isActivePlayer(@Nonnull final LivingEntity player);

    @Nonnull
    PlayerEntity thePlayer();
}

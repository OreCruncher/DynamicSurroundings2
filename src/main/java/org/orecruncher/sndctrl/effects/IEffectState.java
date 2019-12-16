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

package org.orecruncher.sndctrl.effects;

import javax.annotation.Nonnull;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Interface common to all states within the effect framework.
 */
@OnlyIn(Dist.CLIENT)
public interface IEffectState {

	/**
	 * Used to add a Particle to the system.
	 *
	 * @param particle The Particle instance to add to the particle system.
	 */
	void addParticle(@Nonnull final Particle particle);

	/**
	 * Indicates if the specified player is the one sitting behind the screen.
	 *
	 * @param player The EntityPlayer to check
	 * @return true if it is the local player, false otherwise
	 */
	boolean isActivePlayer(@Nonnull final Entity player);

	/**
	 * Obtain a reference to the client's player
	 *
	 * @return Reference to the PlayerEntity. Will not be null.
	 */
	@Nonnull
	PlayerEntity thePlayer();

}

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
import org.orecruncher.lib.GameUtils;
import org.orecruncher.sndctrl.audio.ISoundInstance;

@OnlyIn(Dist.CLIENT)
public class EffectStateBase implements IEffectState {

	public EffectStateBase() {

	}

	/**
	 * Used by an EntityEffect to add a Particle to the system.
	 *
	 * @param particle The Particle instance to add to the particle system.
	 */
	@Override
	public void addParticle(@Nonnull final Particle particle) {
		GameUtils.getMC().particles.addEffect(particle);
	}

	/**
	 * Determines if the specified Entity is the current active player.
	 *
	 * @param player The Entity to evaluate
	 * @return true if the Entity is the current player, false otherwise
	 */
	@Override
	public boolean isActivePlayer(@Nonnull final Entity player) {
		final PlayerEntity ep = thePlayer();
		return ep.getEntityId() == player.getEntityId();
	}

	/**
	 * Obtain a reference to the client's player
	 *
	 * @return Reference to the EntityPlayer. Will not be null.
	 */
	@Override
	@Nonnull
	public PlayerEntity thePlayer() {
		return GameUtils.getPlayer();
	}

}

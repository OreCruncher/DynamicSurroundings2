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

package org.orecruncher.lib.effects;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.GameSettings;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.collections.ObjectArray;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;

/**
 * An EntityEffectManager is responsible for managing the effects that are
 * attached to an entity.
 */
@OnlyIn(Dist.CLIENT)
public class EntityEffectManager implements IEntityEffectManager {

	protected final WeakReference<Entity> subject;

	protected final ObjectArray<AbstractEntityEffect> activeEffects;
	protected boolean isActive = true;
	protected double rangeToPlayer;

	public EntityEffectManager(@Nonnull final Entity entity) {
		this.subject = new WeakReference<>(entity);
		this.activeEffects = null;
	}

	public EntityEffectManager(@Nonnull final Entity entity, @Nonnull final ObjectArray<AbstractEntityEffect> effects) {
		this.subject = new WeakReference<>(entity);
		this.activeEffects = effects;
		for (final AbstractEntityEffect ee : this.activeEffects)
			ee.intitialize(this);
	}

	/**
	 * Updates the state of the EntityEffectHandler as well as the state of the
	 * EntityEffects that are attached.
	 */
	public void update() {
		if (!isActive())
			return;

		this.isActive = isEntityAlive();
		final Entity entity = this.subject.get();
		if (entity != null) {
			this.rangeToPlayer = entity.getDistanceSq(thePlayer());

			for (int i = 0; i < this.activeEffects.size(); i++) {
				final AbstractEntityEffect e = this.activeEffects.get(i);
				if (this.isActive || e.receiveLastCall())
					e.update(entity);
			}
		}
	}

	/**
	 * Instructs the EntityEffectHandler that it should cleanup state because it is
	 * about to die.
	 */
	public void die() {
		this.isActive = false;
		for (final AbstractEntityEffect e : this.activeEffects)
			e.die();
	}

	/**
	 * Used for metric collection to distinguish between active handlers and
	 * dummies.
	 *
	 * @return true if it is an active handler, false for a dummy
	 */
	public boolean isDummy() {
		return false;
	}

	/**
	 * Used for collecting diagnostic information.
	 *
	 * @return List of attached handlers
	 */
	@Nonnull
	public List<String> getAttachedEffects() {
		final List<String> result = new ArrayList<>();
		if (this.activeEffects.size() == 0) {
			result.add("No effects");
		} else {
			for (final AbstractEntityEffect e : this.activeEffects)
				result.add(e.toString());
		}
		return result;
	}

	/**
	 * Whether the EntityEffectHandler is alive or dead.
	 *
	 * @return true if the EntityEffectHandler is active, false otherwise.
	 */
	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public boolean isEntityAlive() {
		final Entity entity = this.subject.get();
		return entity != null && entity.isAlive();
	}

	/**
	 * Provides the distance, squared, to the player entity behind the keyboard.
	 *
	 * @return Range to client player, squared.
	 */
	@Override
	public double rangeToPlayerSq() {
		return this.rangeToPlayer;
	}

	/**
	 * Indicates if the keyboard jockey is in 1st person view.
	 *
	 * @return true if in first person view, false otherwise
	 */
	@Override
	public boolean isFirstPersonView() {
		final GameSettings settings = GameUtils.getGameSettings();
		return settings.thirdPersonView == 0;
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

	/**
	 * Dummy do nothing handler.
	 */
	public static class Dummy extends EntityEffectManager {
		public Dummy(@Nonnull final Entity entity) {
			super(entity);
		}

		@Override
		public void update() {
		}

		@Override
		public void die() {
			this.isActive = false;
		}

		@Override
		public boolean isDummy() {
			return true;
		}

		@Nonnull
		@Override
		public List<String> getAttachedEffects() {
			return ImmutableList.of("Dummy EffectHandler");
		}
	};


}

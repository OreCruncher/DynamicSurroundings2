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

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.client.GameSettings;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.collections.ObjectArray;

import com.google.common.collect.ImmutableList;

import org.orecruncher.sndctrl.api.effects.AbstractEntityEffect;
import org.orecruncher.sndctrl.api.effects.IEntityEffectManager;

/**
 * An EntityEffectManager is responsible for managing the effects that are
 * attached to an entity.
 */
public class EntityEffectManager implements IEntityEffectManager {

	private static final List<String> DUMMY_EFFECTS = ImmutableList.of("Dummy EffectHandler");
	private static final List<String> NO_EFFECTS = ImmutableList.of("No Effects");

	protected final LivingEntity subject;
	protected final ObjectArray<AbstractEntityEffect> activeEffects;
	protected boolean isActive = true;
	protected double rangeToPlayer;

	public EntityEffectManager(@Nonnull final LivingEntity entity) {
		this.subject = entity;
		this.activeEffects = null;
	}

	public EntityEffectManager(@Nonnull final LivingEntity entity, @Nonnull final ObjectArray<AbstractEntityEffect> effects) {
		this.subject = entity;
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
		if (this.activeEffects != null) {
			this.rangeToPlayer = this.subject.getDistanceSq(thePlayer());
			for (final AbstractEntityEffect eff : this.activeEffects)
				if (this.isActive || eff.receiveLastCall())
					eff.update();
		}
	}

	/**
	 * Instructs the EntityEffectHandler that it should cleanup state because it is
	 * about to die.
	 */
	public void die() {
		this.isActive = false;
		if (this.activeEffects != null)
			for (final AbstractEntityEffect e : this.activeEffects)
				e.die();
	}

	/**
	 * Used for collecting diagnostic information.
	 *
	 * @return List of attached handlers
	 */
	@Nonnull
	public List<String> getAttachedEffects() {
		if (this.activeEffects == null)
			return DUMMY_EFFECTS;
		if (this.activeEffects.size() == 0)
			return NO_EFFECTS;
		return this.activeEffects.stream().map(AbstractEntityEffect::toString).collect(Collectors.toList());
	}

	/**
	 * Gets the entity associated with this effect manager
	 *
	 * @return Entity if present, null otherwise
	 */
	@Nonnull
	public LivingEntity getEntity() {
		return this.subject;
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
		return this.subject.isAlive();
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
		return settings.getPointOfView() == PointOfView.FIRST_PERSON;
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
	public boolean isActivePlayer(@Nonnull final LivingEntity player) {
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

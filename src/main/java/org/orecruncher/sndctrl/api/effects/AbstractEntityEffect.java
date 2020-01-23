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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Interface for an effect.
 */
@OnlyIn(Dist.CLIENT)
public abstract class AbstractEntityEffect {

	private IEntityEffectManager manager;
	private final ResourceLocation name;

	/**
	 * Do not perform any heavy initialization in the CTOR! Do it in the
	 * initialize() method!
	 */
	protected AbstractEntityEffect(@Nonnull final ResourceLocation name) {
		this.name = name;
	}

	/**
	 * Returns the name of the handler
	 *
	 * @return The name of the handler
	 */
	@Nonnull
	public ResourceLocation getName() {
		return this.name;
	}

	/**
	 * Get's the entity associated with this effect.
	 *
	 * @return Entity associated with the effect, null otherwise
	 */
	@Nullable
	public Entity getEntity() {
		return this.manager.getEntity();
	}

	/**
	 * Called by the EntityEffectLibrary during the initialization of an
	 * EntityEffectHandler. Override this method to perform any initialization
	 * specific to the EntityEffect. Remember to call the super class!
	 */
	public void intitialize(@Nonnull final IEntityEffectManager manager) {
		this.manager = manager;
	}

	/**
	 * Called when an EntityEffect should update it's state and take action based on
	 * results. Called once per tick.
	 */
	public abstract void update();

	/**
	 * Indicates to the EntityEffectHandler that the EntityEffect wants to be called
	 * one last time after the Entity dies.
	 */
	public boolean receiveLastCall() {
		return false;
	}

	/**
	 * Called when the EntityEffectHandler is cleaning up giving the EntityEffect a
	 * chance to do some house cleaning, like unregistering events.
	 */
	public void die() {

	}

	/**
	 * Indicates if the keyboard jockey is in 1st person view.
	 *
	 * @return true if in first person view, false otherwise
	 */
	public boolean isFirstPersonView() {
		return this.manager.isFirstPersonView();
	}

	/**
	 * Used by an EntityEffect to add a Particle to the system.
	 *
	 * @param particle The Particle instance to add to the particle system.
	 */
	public void addParticle(@Nonnull final Particle particle) {
		this.manager.addParticle(particle);
	}

	/**
	 * Determines if the specified Entity is the current active player.
	 *
	 * @param entity The Entity to evaluate
	 * @return true if the Entity is the current player, false otherwise
	 */
	public boolean isActivePlayer(@Nonnull final Entity entity) {
		return this.manager.isActivePlayer(entity);
	}

	/**
	 * Obtain a reference to the client's player
	 *
	 * @return Reference to the EntityPlayer. Will not be null.
	 */
	@Nonnull
	public PlayerEntity thePlayer() {
		return this.manager.thePlayer();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).addValue(getName().toString()).toString();
	}

}

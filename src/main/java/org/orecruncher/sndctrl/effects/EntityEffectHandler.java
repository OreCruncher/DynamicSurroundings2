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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.collections.ObjectArray;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;

/**
 * An EntityEffectHandler is responsible for managing the effects that are
 * attached to an entity.
 */
@OnlyIn(Dist.CLIENT)
public class EntityEffectHandler extends EntityEffectStateBase implements IEntityEffectHandlerState {

	/**
	 * Dummy do nothing handler.
	 */
	public static class Dummy extends EntityEffectHandler {
		public Dummy(@Nonnull final Entity entity) {
			super(entity);
		}

		@Override
		public void update() {
		}

		@Override
		public void die() {
			this.isAlive = false;
		}

		@Override
		public boolean isDummy() {
			return true;
		}

		@Override
		public List<String> getAttachedEffects() {
			return ImmutableList.of("Dummy EffectHandler");
		}
	};

	protected final ObjectArray<EntityEffect> activeEffects;
	protected boolean isAlive = true;
	protected double rangeToPlayer;

	public EntityEffectHandler(@Nonnull final Entity entity) {
		super(entity);
		this.activeEffects = null;
	}

	public EntityEffectHandler(@Nonnull final Entity entity, @Nonnull final ObjectArray<EntityEffect> effects) {
		super(entity);
		this.activeEffects = effects;
		for (final EntityEffect ee : this.activeEffects)
			ee.intitialize(this);
	}

	/**
	 * Updates the state of the EntityEffectHandler as well as the state of the
	 * EntityEffects that are attached.
	 */
	public void update() {
		if (!isAlive())
			return;

		this.isAlive = isSubjectAlive();
		final Entity entity = this.subject.get();
		if (entity != null) {
			final PlayerEntity player = GameUtils.getPlayer();
			this.rangeToPlayer = entity.getDistanceSq(player);

			for (int i = 0; i < this.activeEffects.size(); i++) {
				final EntityEffect e = this.activeEffects.get(i);
				if (this.isAlive || e.receiveLastCall())
					e.update(entity);
			}
		}
	}

	/**
	 * Instructs the EntityEffectHandler that it should cleanup state because it is
	 * about to die.
	 */
	public void die() {
		this.isAlive = false;
		for (final EntityEffect e : this.activeEffects)
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
			for (final EntityEffect e : this.activeEffects)
				result.add(e.toString());
		}
		return result;
	}

	// ================================================
	// IEntityEffectHandlerState interface
	// ================================================

	/**
	 * Whether the EntityEffectHandler is alive or dead.
	 *
	 * @return true if the EntityEffectHandler is active, false otherwise.
	 */
	@Override
	public boolean isAlive() {
		return this.isAlive;
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

}

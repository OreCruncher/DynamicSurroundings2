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

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;

/**
 * Interface for an effect.
 */
@OnlyIn(Dist.CLIENT)
public abstract class EntityEffect {

	private IEntityEffectHandlerState state;

	/**
	 * Do not perform any heavy initialization in the CTOR! Do it in the
	 * initialize() method!
	 */
	public EntityEffect() {

	}

	/**
	 * Returns the name of the handler
	 *
	 * @return The name of the handler
	 */
	@Nonnull
	public abstract String name();

	/**
	 * Called by the EntityEffectLibrary during the initialization of an
	 * EntityEffectHandler. Override this method to perform any initialization
	 * specific to the EntityEffect. Remember to call the super class!
	 *
	 * @param state The state provided by the EntityEffectLibrary
	 */
	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		this.state = state;
	}

	/**
	 * Accessor to obtain the IEntityEffectHandlerState associated with this
	 * EntityEffect instance.
	 *
	 * @return Associated IEntityEffectHandlerState instance
	 */
	protected IEntityEffectHandlerState getState() {
		return this.state;
	}

	/**
	 * Called when an EntityEffect should update it's state and take action based on
	 * results. Called once per tick.
	 */
	public abstract void update(@Nonnull final Entity subject);

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
		final GameSettings settings = GameUtils.getGameSettings();
		return settings.thirdPersonView == 0;
	}

	@Override
	public String toString() {
		return name();
	}

}

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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * An EventEffect is a special effect that can take place in response to a Forge
 * event. For example, the JUMP sound would play in response to a
 * LivingJumpEvent.
 */
@OnlyIn(Dist.CLIENT)
public abstract class EventEffect {

	private IEventEffectLibraryState library;

	public EventEffect() {
	}

	/**
	 * Used by the framework to configure the EffectLibrary to which it is
	 * associated. It is set during registration.
	 *
	 * @param state The library set to configure for the EventEffect
	 */
	void setState(@Nonnull final IEventEffectLibraryState state) {
		this.library = state;
	}

	/**
	 * Accessor to obtain the IEventEffectLibraryState associated with this
	 * EventEffect instance.
	 *
	 * @return Associated IEventEffectLibraryState instance
	 */
	protected IEventEffectLibraryState getState() {
		return this.library;
	}

	/**
	 * Determines if the EntityEvent is valid in terms of a proper Entity being
	 * configured and that the event is fired on Side.CLIENT.
	 *
	 * @param event The event to evaluate
	 * @return true if valid, false otherwise
	 */
	protected boolean isClientValid(@Nonnull final EntityEvent event) {
		if (event.getEntity() != null) {
			return event.getEntity().getEntityWorld().isRemote;
		}

		return false;
	}

	/**
	 * Determines if the PlayerEvent is valid in terms of a proper Entity being
	 * configured and that the event is fired on Side.CLIENT.
	 *
	 * @param event The event to evaluate
	 * @return true if valid, false otherwise
	 */
	protected boolean isClientValid(@Nonnull final PlayerEvent event) {
		if (event.getPlayer() != null) {
			return event.getPlayer().getEntityWorld().isRemote;
		}

		return false;
	}

}

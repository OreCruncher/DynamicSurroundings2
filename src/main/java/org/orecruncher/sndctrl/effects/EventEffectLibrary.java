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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

/**
 * The EventEffectLibrary is the focal point of EventEffect management. It is
 * responsible for registration and tear down of associated events as needed.
 *
 */
@OnlyIn(Dist.CLIENT)
public class EventEffectLibrary extends EffectStateBase implements IEventEffectLibraryState {

	protected final List<EventEffect> effects = new ArrayList<>();

	/**
	 * Registers the EventEffect with the EventEffectLibrary. The reference will
	 * automatically be registered with Forge, and will be tracked.
	 *
	 * @param effect EventEffect instance to register
	 */
	public void register(@Nonnull final EventEffect effect) {
		effect.setState(this);
		this.effects.add(effect);
		MinecraftForge.EVENT_BUS.register(effect);
	}

	/**
	 * Unregisters all EventEffects that have been registered prior to going out of
	 * scope.
	 */
	public void cleanup() {
		this.effects.forEach(MinecraftForge.EVENT_BUS::unregister);
		this.effects.clear();
	}

	/**
	 * Indicates if the specified player is the one sitting behind the screen.
	 *
	 * @param player The EntityPlayer to check
	 * @return true if it is the local player, false otherwise
	 */
	@Override
	public boolean isActivePlayer(@Nonnull final Entity player) {
		final PlayerEntity ep = thePlayer();
		return ep.getEntityId() == player.getEntityId();
	}

}

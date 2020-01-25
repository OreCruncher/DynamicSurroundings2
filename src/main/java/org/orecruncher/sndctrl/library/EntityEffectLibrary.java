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

package org.orecruncher.sndctrl.library;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.entity.Entity;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.sndctrl.api.effects.AbstractEntityEffect;
import org.orecruncher.sndctrl.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.api.effects.IEntityEffectFactoryHandler;

@OnlyIn(Dist.CLIENT)
public final class EntityEffectLibrary {

	private static final ObjectArray<IEntityEffectFactoryHandler> entityEffectfactoryHandlers = new ObjectArray<>();

	private EntityEffectLibrary() {

	}

	public static void initialize() {
		// Kick the class ctor
	}

	public static void complete() {
		if (Config.CLIENT.logging.get_enableLogging()) {
			SoundControl.LOGGER.info("Registered Handlers");
			SoundControl.LOGGER.info("===================");
			for (final IEntityEffectFactoryHandler h : entityEffectfactoryHandlers) {
				SoundControl.LOGGER.info(h.getName().toString());
			}
		}
	}

	@Nonnull
	public static ObjectArray<AbstractEntityEffect> getEffects(@Nonnull final Entity entity) {
		final ObjectArray<AbstractEntityEffect> result = new ObjectArray<>();
		entityEffectfactoryHandlers.forEach(h -> {
			if (h.appliesTo(entity))
				result.add(h.get(entity));
		});
		result.trim();
		return result;
	}

	/**
	 * Registers an IEntityEffectFactoryFilter/IEntityEffectFactory pair. The filter
	 * is used by the EntityEffectLibrary to determine if an EntityEffect applies to
	 * a target entity.
	 *
	 * @param handler Factory handler to register with the system
	 */
	public static void register(@Nonnull final IEntityEffectFactoryHandler handler) {
		entityEffectfactoryHandlers.add(handler);
	}

}

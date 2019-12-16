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

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.collections.ObjectArray;

import net.minecraft.entity.Entity;
import org.orecruncher.sndctrl.library.EntityEffectInfo;
import org.orecruncher.sndctrl.library.EntityEffectLibrary;

/**
 * Central repository for a collection of IEntityEffectFactory instances and the
 * IFactoryFilters associated with them. Typically there will be a single
 * instance of the EntityEffectLibrary for a project, but multiples can be
 * created based on the circumstances.
 */
@OnlyIn(Dist.CLIENT)
public final class Effects {

	protected static final ObjectArray<IEntityEffectFactoryFilter> filters = new ObjectArray<>();
	protected static final ObjectArray<IEntityEffectFactory> factories = new ObjectArray<>();

	private Effects() {
	}

	/**
	 * Registers an IEntityEffectFactoryFilter/IEntityEffectFactory pair. The filter
	 * is used by the EntityEffectLibrary to determine if an EntityEffect applies to
	 * a target entity.
	 *
	 * @param filter  IEntityEffectFactoryFilter used to determine if the
	 *                IEntityEffectFactory should be used to create an EntityEffect.
	 * @param factory IEntityEffectFactory used to create an EntityEffect if the
	 *                IEntityEffectFactoryFilter returns true.
	 */
	public static void register(@Nonnull final IEntityEffectFactoryFilter filter,
			@Nonnull final IEntityEffectFactory factory) {
		filters.add(filter);
		factories.add(factory);
	}

	/**
	 * Creates an EntityEffectHandler for the specified Entity. The IEffects
	 * attached to the EntityEffectHandler is determined by an IFactoryFitler. An
	 * EntityEffectHandler will always be created.
	 *
	 * @param entity The subject Entity for which an EntityEffectHandler is created
	 * @return An EntityEffectHandler for the Entity
	 */
	@Nonnull
	public static Optional<EntityEffectHandler> create(@Nonnull final Entity entity) {
		final ObjectArray<EntityEffect> effectToApply = new ObjectArray<>();

		final EntityEffectInfo eei = EntityEffectLibrary.getEffects(entity);
		for (int i = 0; i < filters.size(); i++)
			if (filters.get(i).applies(entity, eei)) {
				final List<EntityEffect> r = factories.get(i).create(entity, eei);
				effectToApply.addAll(r);
			}

		final EntityEffectHandler result;
		if (effectToApply.size() > 0) {
			result = new EntityEffectHandler(entity, effectToApply);
		} else {
			// No effects. Return a dummy handler.
			result = new EntityEffectHandler.Dummy(entity);
		}

		return Optional.of(result);
	}

}

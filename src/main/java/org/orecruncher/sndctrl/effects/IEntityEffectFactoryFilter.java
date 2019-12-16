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

import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.library.EntityEffectInfo;

/**
 * An IEntityEffectFactoryFilter is used by the EntityEffectLibrary to determine
 * if a particular EntityEffect would be applied to an Entity.
 *
 */
@OnlyIn(Dist.CLIENT)
@FunctionalInterface
public interface IEntityEffectFactoryFilter {

	/**
	 * Evaluates the Entity to determine if an EntityEffect will apply.
	 *
	 * @param entity The subject of the evaluation
	 * @param eei    An object containing the Entities effect parameters
	 * @return true if the EntityEffect applies, false otherwise
	 */
	boolean applies(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei);

}

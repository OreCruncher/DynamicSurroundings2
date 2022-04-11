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

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public interface IEntityEffectFactoryHandler {

    /**
     * Resource name of the effect.  Ensure that it is properly scoped to the mod.
     *
     * @return ResourceLocation to serve as the name of the effect.
     */
    ResourceLocation getName();

    /**
     * Called by the framework to determine if the effect is to be applied to the specified entity.
     *
     * @param entity Entity that is being evaluated for effects
     * @return true if the effect is to be applied; false otherwise
     */
    boolean appliesTo(@Nonnull final LivingEntity entity);

    /**
     * Obtains an instance of the effect.  This effect could be unique to the entity, or a singleton.  It's up
     * to the effect implementation.
     *
     * @param entity Entity that is to have the effect
     * @return Effect instance
     */
    @Nonnull
    AbstractEntityEffect get(@Nonnull final LivingEntity entity);
}

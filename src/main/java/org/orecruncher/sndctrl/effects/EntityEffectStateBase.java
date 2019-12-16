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

import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityEffectStateBase extends EffectStateBase implements IEntityEffectState {

	protected final WeakReference<Entity> subject;

	public EntityEffectStateBase(@Nonnull final Entity entity) {
		super();

		this.subject = new WeakReference<>(entity);
	}

	/**
	 * The Entity subject the EntityEffectHandler is associated with. May be null if
	 * the Entity is no longer in scope.
	 *
	 * @return Reference to the subject Entity, if any.
	 */
	@Override
	@Nullable
	public Entity subject() {
		return this.subject.get();
	}

	/**
	 * Indicates if the subject is alive.
	 *
	 * @return true if the subject is alive, false otherwise
	 */
	@Override
	public boolean isSubjectAlive() {
		final Entity e = this.subject.get();
		return e != null && e.isAlive();
	}

	/**
	 * Determines the distance between the Entity subject and the specified Entity.
	 *
	 * @param entity The Entity to which the distance is measured.
	 * @return The distance between the two Entities in blocks, squared.
	 */
	@Override
	public double distanceSq(final Entity entity) {
		final Entity e = this.subject.get();
		if (e == null)
			return Double.MAX_VALUE;
		return e.getDistanceSq(entity);
	}

	/**
	 * Returns the total world time, in ticks, the entity belongs to.
	 *
	 * @return Total world time
	 */
	@Override
	public long getWorldTime() {
		final Entity e = this.subject.get();
		return e == null ? 0 : e.getEntityWorld().getGameTime();
	}

}

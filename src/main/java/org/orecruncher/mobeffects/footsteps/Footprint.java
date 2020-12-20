/*
 * Dynamic Surroundings: Mob Effects
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

package org.orecruncher.mobeffects.footsteps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Footprint {

	private FootprintStyle style;
	private LivingEntity entity;
	private Vec3d stepLoc;
	private boolean isRightFoot;
	private float rotation;
	private float scale;

	public static Footprint produce(@Nonnull final FootprintStyle style, @Nonnull final LivingEntity entity,
			@Nonnull final Vec3d stepLoc, final float rotation, final float scale, final boolean rightFoot) {
		final Footprint print = new Footprint();
		print.style = style;
		print.entity = entity;
		print.stepLoc = stepLoc;
		print.rotation = rotation;
		print.isRightFoot = rightFoot;
		print.scale = scale;
		return print;
	}

	public FootprintStyle getStyle() {
		return this.style;
	}

	public LivingEntity getEntity() {
		return this.entity;
	}

	@Nullable
	public Vec3d getStepLocation() {
		return this.stepLoc;
	}

	public boolean isRightFoot() {
		return this.isRightFoot;
	}

	public float getRotation() {
		return this.rotation;
	}

	public float getScale() {
		return this.scale;
	}

}

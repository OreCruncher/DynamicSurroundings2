/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.shaders.aurora;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.math.MathStuff;

@OnlyIn(Dist.CLIENT)
final class Panel {

	private static final float COS_DEG90_FACTOR = MathStuff.cos(MathStuff.PI_F / 2.0F);
	private static final float COS_DEG270_FACTOR = MathStuff.cos(MathStuff.PI_F / 2.0F + MathStuff.PI_F);
	private static final float SIN_DEG90_FACTOR = MathStuff.sin(MathStuff.PI_F / 2.0F);
	private static final float SIN_DEG270_FACTOR = MathStuff.sin(MathStuff.PI_F / 2.0F + MathStuff.PI_F);

	public float dZ = 0.0F;
	public float dY = 0.0F;

	public float cosDeg90 = 0.0F;
	public float cosDeg270 = 0.0F;
	public float sinDeg90 = 0.0F;
	public float sinDeg270 = 0.0F;

	public float angle;
	public float posX;
	public float posY;
	public float posZ;

	public float tetX = 0.0F;
	public float tetX2 = 0.0F;
	public float tetZ = 0.0F;
	public float tetZ2 = 0.0F;

	public Panel(@Nonnull final Panel template, final int offset) {
		final float rads = MathStuff.toRadians(90.0F + template.angle);
		this.posX = template.posX + MathStuff.cos(rads) * offset;
		this.posY = template.posY - 2.0F;
		this.posZ = template.posZ + MathStuff.sin(rads) * offset;
		this.angle = template.angle;
	}

	public Panel(final float x, final float y, final float z, final float theta) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.angle = theta;
	}

	public float getModdedZ() {
		return this.posZ + this.dZ;
	}

	public float getModdedY() {
		final float y = this.posY + this.dY;
		return MathStuff.max(0, y);
	}

	public void setWidth(final float w) {
		this.cosDeg270 = COS_DEG270_FACTOR * w;
		this.cosDeg90 = COS_DEG90_FACTOR * w;
		this.sinDeg270 = SIN_DEG270_FACTOR * w;
		this.sinDeg90 = SIN_DEG90_FACTOR * w;
		
		this.tetX = this.posX + this.cosDeg90;
		this.tetX2 = this.posX + this.cosDeg270;
	}

}

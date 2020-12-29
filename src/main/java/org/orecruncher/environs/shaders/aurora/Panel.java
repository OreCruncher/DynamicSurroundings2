/*
 *  Dynamic Surroundings
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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.math.MathStuff;

@OnlyIn(Dist.CLIENT)
final class Panel {

	private static final float COS_DEG90_FACTOR = MathStuff.cos(MathStuff.PI_F / 2.0F);
	private static final float COS_DEG270_FACTOR = MathStuff.cos(MathStuff.PI_F / 2.0F + MathStuff.PI_F);
	private static final float SIN_DEG90_FACTOR = MathStuff.sin(MathStuff.PI_F / 2.0F);

	private float dZ = 0.0F;
	private float dY = 0.0F;

	private float sinDeg90 = 0.0F;

	public final float posX;
	public final float posY;
	public final float posZ;

	public float tetX = 0.0F;
	public float tetX2 = 0.0F;
	public float tetZ = 0.0F;

	public Panel(final float x, final float y, final float z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
	}

	public void translate(final float dY, final float dZ) {
		this.dZ = dZ;
		this.dY = dY;

		final float mZ = this.getModdedZ();
		this.tetZ = mZ + this.sinDeg90;
	}

	public float getModdedZ() {
		return this.posZ + this.dZ;
	}

	public float getModdedY() {
		final float y = this.posY + this.dY;
		return MathStuff.max(0, y);
	}

	public void setWidth(final float w) {
		final float cosDeg270 = COS_DEG270_FACTOR * w;
		final float cosDeg90 = COS_DEG90_FACTOR * w;

		this.sinDeg90 = SIN_DEG90_FACTOR * w;
		
		this.tetX = this.posX + cosDeg90;
		this.tetX2 = this.posX + cosDeg270;
	}

}

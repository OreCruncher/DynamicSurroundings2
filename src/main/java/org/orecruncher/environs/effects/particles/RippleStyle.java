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

package org.orecruncher.environs.effects.particles;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Environs;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.util.ResourceLocation;

@OnlyIn(Dist.CLIENT)
public enum RippleStyle {

	CIRCLE("textures/particles/ripple.png"),
	DARK_CIRCLE("textures/particles/ripple1.png"),
	DARK_SQUARE("textures/particles/ripple2.png"),
	PIXELATED_CIRCLE("textures/particles/pixel_ripples.png") {
		private final int FRAMES = 7;
		private final float DELTA = 1F / this.FRAMES;
		private final int MAX_AGE = this.FRAMES * 2;

		@Override
		public float getU1(final int age) {
			return (age / 2) * this.DELTA;
		}

		@Override
		public float getU2(final int age) {
			return getU1(age) + this.DELTA;
		}

		@Override
		public boolean doScaling() {
			return false;
		}

		@Override
		public int getMaxAge() {
			return this.MAX_AGE;
		}
	};

	private final ResourceLocation resource;

	RippleStyle(@Nonnull final String texture) {
		this.resource = new ResourceLocation(Environs.MOD_ID, texture);
	}

	@Nonnull
	public ResourceLocation getTexture() {
		return this.resource;
	}

	public float getU1(final int age) {
		return 0F;
	}

	public float getU2(final int age) {
		return 1F;
	}

	public float getV1(final int age) {
		return 0F;
	}

	public float getV2(final int age) {
		return 1F;
	}

	public boolean doScaling() {
		return true;
	}

	public boolean doAlpha() {
		return true;
	}

	public int getMaxAge() {
		return 12 + XorShiftRandom.current().nextInt(8);
	}
}

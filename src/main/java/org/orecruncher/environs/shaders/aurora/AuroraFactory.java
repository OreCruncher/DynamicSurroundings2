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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class AuroraFactory {

	@Nonnull
	public static IAurora produce(final long seed) {
		return new AuroraShaderBand(seed);
	}

	/**
	 * Preset geometry of an Aurora. A preset is selected by the server when an
	 * Aurora spawns.
	 */
	public static final class AuroraGeometry {

		public final int length;
		public final float nodeLength;
		public final float nodeWidth;
		public final int alphaLimit;

		private static final List<AuroraGeometry> PRESET = new ArrayList<>();

		static {
			// 10/5; 90/45
			PRESET.add(new AuroraGeometry(128, 30.0F, 2.0F, 96));
			PRESET.add(new AuroraGeometry(128, 15.0F, 2.0F, 96));
			PRESET.add(new AuroraGeometry(64, 30.0F, 2.0F, 96));
			PRESET.add(new AuroraGeometry(64, 15.0F, 2.0F, 96));

			PRESET.add(new AuroraGeometry(128, 30.0F, 2.0F, 80));
			PRESET.add(new AuroraGeometry(128, 15.0F, 2.0F, 80));
			PRESET.add(new AuroraGeometry(64, 30.0F, 2.0F, 80));
			PRESET.add(new AuroraGeometry(64, 15.0F, 2.0F, 80));

			PRESET.add(new AuroraGeometry(128, 30.0F, 2.0F, 64));
			PRESET.add(new AuroraGeometry(128, 15.0F, 2.0F, 64));
			PRESET.add(new AuroraGeometry(64, 30.0F, 2.0F, 64));
			PRESET.add(new AuroraGeometry(64, 15.0F, 2.0F, 64));
		}

		private AuroraGeometry(final int length, final float nodeLength, final float nodeWidth, final int alphaLimit) {
			this.length = length;
			this.nodeLength = nodeLength;
			this.nodeWidth = nodeWidth;
			this.alphaLimit = alphaLimit;
		}

		@Nonnull
		public static AuroraGeometry get(@Nonnull final Random random) {
			final int idx = random.nextInt(PRESET.size());
			return PRESET.get(idx);
		}

		@Override
		@Nonnull
		public String toString() {
			return "bandLength:" + this.length +
					";nodeLength:" + this.nodeLength +
					";nodeWidth:" + this.nodeWidth +
					";alphaLimit:" + this.alphaLimit;
		}
	}

}

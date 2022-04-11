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

package org.orecruncher.environs.library;

import javax.annotation.Nonnull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class WTFFakeBiomeAdapter extends FakeBiomeAdapter {

	public WTFFakeBiomeAdapter() {
		super("WTFJustHappened");
	}

	@Override
	public Biome.RainType getPrecipitationType() {
		return Biome.RainType.NONE;
	}

	@Override
	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return 0F;
	}

	@Override
	public float getTemperature() {
		return 0F;
	}

	@Override
	public boolean isHighHumidity() {
		return false;
	}

	@Override
	public float getDownfall() {
		return 0F;
	}
}

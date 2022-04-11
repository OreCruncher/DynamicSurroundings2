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

import java.util.Set;

import javax.annotation.Nonnull;


import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;
import org.orecruncher.lib.biomes.BiomeUtilities;

public class BiomeAdapter implements IBiome {

	protected final Biome biome;
	protected final Set<Type> types;

	public BiomeAdapter(@Nonnull final Biome biome) {
		this.biome = biome;
		this.types = BiomeUtilities.getBiomeTypes(this.biome);
	}

	@Override
	public Biome getBiome() {
		return this.biome;
	}

	@Override
	public ResourceLocation getKey() {
		return this.biome.getRegistryName();
	}

	@Override
	public String getName() {
		return BiomeUtilities.getBiomeName(biome);
	}

	@Override
	public Set<Type> getTypes() {
		return this.types;
	}

	@Override
	public Biome.RainType getPrecipitationType() {
		return this.biome.getPrecipitation();
	}

	@Override
	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return this.biome.getTemperature(pos);
	}

	@Override
	public float getTemperature() {
		return this.biome.getTemperature();
	}

	@Override
	public boolean isHighHumidity() {
		return this.biome.isHighHumidity();
	}

	@Override
	public float getDownfall() {
		return this.biome.getDownfall();
	}

	@Override
	public boolean isFake() {
		return false;
	}

}

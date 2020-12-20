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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary.Type;
import org.orecruncher.environs.Environs;
import org.orecruncher.lib.GameUtils;

@OnlyIn(Dist.CLIENT)
public class FakeBiomeAdapter implements IBiome {

	protected final String name;
	protected final ResourceLocation key;

	@Nonnull
	protected final BiomeInfo biomeData;

	public FakeBiomeAdapter(@Nonnull final String name) {
		this.name = name;
		this.key = new ResourceLocation(Environs.MOD_ID, ("fake_" + name).replace(' ', '_').toLowerCase());
		this.biomeData = new BiomeInfo(this);
	}

	@Nonnull
	public BiomeInfo getBiomeData() {
		return this.biomeData;
	}

	@Override
	public Biome.RainType getPrecipitationType() {
		return getTrueBiome().getPrecipitationType();
	}


	@Override
	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return getTrueBiome().getFloatTemperature(pos);
	}

	@Override
	public float getTemperature() {
		return getTrueBiome().getTemperature();
	}

	@Override
	public TempCategory getTempCategory() {
		return getTrueBiome().getTempCategory();
	}

	@Override
	public boolean isHighHumidity() {
		return getTrueBiome().isHighHumidity();
	}

	@Override
	public float getDownfall() {
		return getTrueBiome().getRainfall();
	}

	@Override
	public Biome getBiome() {
		return null;
	}

	@Override
	public ResourceLocation getKey() {
		return this.key;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Set<Type> getTypes() {
		return ImmutableSet.of();
	}

	@Override
	public boolean isFake() {
		return true;
	}

	private static BiomeInfo getTrueBiome() {
		return BiomeLibrary.getPlayerBiome(GameUtils.getPlayer(), true);
	}

}

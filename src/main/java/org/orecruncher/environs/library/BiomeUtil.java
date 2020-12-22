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

import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.environs.misc.IMixinBiomeData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public final class BiomeUtil {

    @Nonnull
    public static BiomeInfo getBiomeData(@Nonnull final Biome biome) {
        BiomeInfo result = ((IMixinBiomeData) (Object) biome).getInfo();
        if (result == null) {
            // Get the data from the Forge registries
            final Biome forge = ForgeRegistries.BIOMES.getValue(biome.getRegistryName());
            if (forge != null) {
                result = ((IMixinBiomeData) (Object) forge).getInfo();
            }

            if (result == null) {
                final BiomeAdapter handler = new BiomeAdapter(biome);
                result = new BiomeInfo(handler);
            }

            ((IMixinBiomeData) (Object) biome).setInfo(result);
        }
        return result;
    }

    public static void setBiomeData(@Nonnull final Biome biome, @Nullable final BiomeInfo data) {
        ((IMixinBiomeData) (Object) biome).setInfo(data);
    }
}

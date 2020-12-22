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

import com.google.common.collect.ImmutableSet;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.misc.IMixinBiomeData;
import org.orecruncher.lib.gui.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class BiomeUtil {

    private static final Color NO_COLOR = new Color(1F, 1F, 1F);

    @Nonnull
    public static BiomeInfo getBiomeData(@Nonnull final Biome biome) {
        BiomeInfo result = ((IMixinBiomeData) (Object) biome).getInfo();
        if (result == null) {
            final BiomeAdapter handler = new BiomeAdapter(biome);
            ((IMixinBiomeData) (Object) biome).setInfo(result = new BiomeInfo(handler));
        }
        return result;
    }

    public static void setBiomeData(@Nonnull final Biome biome, @Nullable final BiomeInfo data) {
        ((IMixinBiomeData) (Object) biome).setInfo(data);
    }

    // ===================================
    //
    // Miscellaneous Support Functions
    //
    // ===================================
    @Nonnull
    public static Collection<Type> getBiomeTypes() {
        return BiomeDictionary.Type.getAll();
    }

    @Nonnull
    public static Color getColorForLiquid(@Nonnull final IBlockReader world, @Nonnull final BlockPos pos) {
        final FluidState fluidState = world.getFluidState(pos);

        if (fluidState.isEmpty())
            return NO_COLOR;

        final Fluid fluid = fluidState.getFluid();
        return new Color(fluid.getAttributes().getColor());
    }

    @Nonnull
    public static Set<Type> getBiomeTypes(@Nonnull final Biome biome) {
        // It's possible to have a biome that is not registered come through here
        // There is an internal check that will throw an exception if that is the
        // case. Seen this with OTG installed.
        try {
            RegistryKey<Biome> key = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, biome.getRegistryName());
            return BiomeDictionary.getTypes(key);
        } catch (@Nonnull final Throwable t) {
            final String name = biome.toString(); //biome.getDisplayName().getFormattedText();
            Environs.LOGGER.warn("Unable to get biome type data for biome '%s'", name);
        }
        return ImmutableSet.of();
    }

}

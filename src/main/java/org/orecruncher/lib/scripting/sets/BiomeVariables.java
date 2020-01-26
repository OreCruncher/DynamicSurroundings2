/*
 * Dynamic Surroundings: Sound Control
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

package org.orecruncher.lib.scripting.sets;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.scripting.VariableSet;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class BiomeVariables extends VariableSet<IBiomeVariables> implements IBiomeVariables {

    private Biome biome;
    private final LazyVariable<Set<BiomeDictionary.Type>> biomeTraits = new LazyVariable<>(() -> BiomeDictionary.getTypes(this.biome));
    private final LazyVariable<Set<String>> biomeTraitNames = new LazyVariable<>(() -> this.biomeTraits.get().stream().map(BiomeDictionary.Type::getName).collect(Collectors.toSet()));
    private final LazyVariable<String> traits = new LazyVariable<>(() -> String.join(" ", this.biomeTraitNames.get()));
    private final LazyVariable<String> name = new LazyVariable<>(() -> this.biome.getDisplayName().getFormattedText());
    private final LazyVariable<String> modid = new LazyVariable<>(() -> this.biome.getRegistryName().getNamespace());
    private final LazyVariable<String> id = new LazyVariable<>(() -> this.biome.getRegistryName().toString());
    private final LazyVariable<String> category = new LazyVariable<>(() -> this.biome.getCategory().getName());
    private final LazyVariable<String> rainType = new LazyVariable<>(() -> this.biome.getPrecipitation().getName());
    private final LazyVariable<String> temp = new LazyVariable<>(() -> this.biome.getTempCategory().getName());

    public BiomeVariables() {
        super("biome");
        setBiome(Biomes.PLAINS);
    }

    public void setBiome(@Nonnull final Biome biome) {
        if (this.biome != biome) {
            update();
            this.biome = biome;
        }
    }

    @Nonnull
    @Override
    public IBiomeVariables getInterface() {
        return this;
    }

    @Override
    public void update() {
        Biome newBiome = null;
        if (GameUtils.isInGame()) {
            newBiome = GameUtils.getWorld().getBiome(GameUtils.getPlayer().getPosition());
        } else {
            newBiome = Biomes.PLAINS;
        }

        if (newBiome != this.biome) {
            this.biome = newBiome;
            this.name.reset();
            this.modid.reset();
            this.id.reset();
            this.category.reset();
            this.rainType.reset();
            this.temp.reset();
            this.traits.reset();
            this.biomeTraits.reset();
            this.biomeTraitNames.reset();
        }
    }

    @Override
    public String getModId() {
        return this.modid.get();
    }

    @Override
    public String getId() {
        return this.id.get();
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name.get();
    }

    @Override
    public float getRainfall() {
        return this.biome.getDownfall();
    }

    @Override
    public float getTemperature() {
        return this.biome.getDefaultTemperature();
    }

    @Override
    public String getCategory() {
        return this.category.get();
    }

    @Override
    public String getTemperatureCategory() {
        return this.temp.get();
    }

    @Override
    public String getRainType() {
        return this.rainType.get();
    }

    @Override
    public String getTraits() {
        return this.traits.get();
    }

    @Override
    public boolean is(@Nonnull final String traitName) {
        return this.biomeTraitNames.get().contains(traitName.toUpperCase());
    }

    public boolean is(@Nonnull final BiomeDictionary.Type type) {
        return this.biomeTraits.get().contains(type);
    }
}

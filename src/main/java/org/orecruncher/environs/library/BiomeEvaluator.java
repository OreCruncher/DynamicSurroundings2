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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary;
import org.orecruncher.lib.scripting.ExecutionContext;

import javax.annotation.Nonnull;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class BiomeEvaluator {

    private final ExecutionContext context = new ExecutionContext("Biomes");

    public BiomeEvaluator() {

    }

    public void update(@Nonnull final BiomeInfo biome) {
        final Map<String, Object> props = new HashMap<>();

        props.put("name", biome.getBiomeName());
        props.put("id", biome.getKey().toString());
        props.put("modid", biome.getKey().getNamespace());
        props.put("isFake", biome.isFake());
        if (biome.isFake()) {
            props.put("temperature", 0);
            props.put("rainfall", 0);
        } else {
            props.put("temperature", biome.getTemperature());
            props.put("rainfall", biome.getRainfall());
        }

        final Collection<BiomeDictionary.Type> types = BiomeUtil.getBiomeTypes();
        final Set<BiomeDictionary.Type> biomeTypes = biome.getBiomeTypes();

        for (final BiomeDictionary.Type t : types) {
            final String name = "is" + t.getName().substring(0, 1).toUpperCase() + t.getName().substring(1).toLowerCase();
            props.put(name, biomeTypes.contains(t));
        }

        this.context.put("biome", props);
    }

    public boolean matches(@Nonnull final String conditions) {
        if (conditions.length() == 0)
            return true;
        Optional<Object> result = this.context.eval(conditions);
        return result.isPresent() && (boolean) result.get();
    }

}

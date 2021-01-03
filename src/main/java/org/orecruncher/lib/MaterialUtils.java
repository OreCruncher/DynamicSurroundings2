/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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

package org.orecruncher.lib;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.material.Material;
import org.orecruncher.lib.reflection.ReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@SuppressWarnings("unused")
public final class MaterialUtils {

    private static final Reference2ObjectOpenHashMap<Material, String> materialMap = new Reference2ObjectOpenHashMap<>();
    private static final Map<String, Material> materialMapInv = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        materialMap.defaultReturnValue("CUSTOM");

        for(final Field f : ReflectionHelper.getStaticFields(Material.class)) {
            try {
                final Material type = (Material) f.get(null);
                materialMap.put(type, f.getName());
            } catch(@Nonnull final Throwable ignore) {
            }
        }

        // Create the inverse map
        for (final Map.Entry<Material, String> kvp : materialMap.entrySet()) {
            materialMapInv.put(kvp.getValue(), kvp.getKey());
        }
    }

    private MaterialUtils() {

    }

    @Nonnull
    public static Set<Material> getMaterials() {
        return materialMap.keySet();
    }

    @Nullable
    public static Material getMaterial(@Nonnull final String name) {
        return materialMapInv.get(name);
    }

    @Nullable
    public static String getMaterialName(@Nonnull final Material mat) {
        return materialMap.get(mat);
    }
}

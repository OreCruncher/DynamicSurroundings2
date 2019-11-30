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

package org.orecruncher.lib;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.SoundType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.TreeMap;

public final class SoundTypeUtils {

    private static final Map<SoundType, String> soundTypeMap = new Reference2ObjectOpenHashMap<>();
    private static final Map<String, SoundType> soundTypeMapInv = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        soundTypeMap.put(SoundType.WOOD, "WOOD");
        soundTypeMap.put(SoundType.GROUND, "GROUND");
        soundTypeMap.put(SoundType.PLANT, "PLANT");
        soundTypeMap.put(SoundType.STONE, "STONE");
        soundTypeMap.put(SoundType.METAL, "METAL");
        soundTypeMap.put(SoundType.GLASS, "GLASS");
        soundTypeMap.put(SoundType.CLOTH, "CLOTH");
        soundTypeMap.put(SoundType.SAND, "SAND");
        soundTypeMap.put(SoundType.SNOW, "SNOW");
        soundTypeMap.put(SoundType.LADDER, "LADDER");
        soundTypeMap.put(SoundType.ANVIL, "ANVIL");
        soundTypeMap.put(SoundType.SLIME, "SLIME");
        soundTypeMap.put(SoundType.WET_GRASS, "WET_GRASS");
        soundTypeMap.put(SoundType.CORAL, "CORAL");
        soundTypeMap.put(SoundType.BAMBOO, "BAMBOO");
        soundTypeMap.put(SoundType.BAMBOO_SAPLING, "BAMBOO_SAPLING");
        soundTypeMap.put(SoundType.SCAFFOLDING, "SCAFFOLDING");
        soundTypeMap.put(SoundType.SWEET_BERRY_BUSH, "SWEET_BERRY_BUSH");
        soundTypeMap.put(SoundType.CROP, "CROP");
        soundTypeMap.put(SoundType.STEM, "STEM");
        soundTypeMap.put(SoundType.NETHER_WART, "NETHER_WART");
        soundTypeMap.put(SoundType.LANTERN, "LANTERN");

        // Create the inverse map
        for (final Map.Entry<SoundType, String> kvp : soundTypeMap.entrySet()) {
            soundTypeMapInv.put(kvp.getValue(), kvp.getKey());
        }
    }

    private SoundTypeUtils() {

    }

    @Nullable
    public static SoundType getSoundType(@Nonnull final String name) {
        return soundTypeMapInv.get(name);
    }

    @Nullable
    public static String getSoundTypeName(@Nonnull final SoundType st) {
        return soundTypeMap.get(st);
    }

}

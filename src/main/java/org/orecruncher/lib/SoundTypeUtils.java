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
import net.minecraft.block.SoundType;
import org.orecruncher.lib.reflection.ReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("unused")
public final class SoundTypeUtils {

    private static final Reference2ObjectOpenHashMap<SoundType, String> soundTypeMap = new Reference2ObjectOpenHashMap<>();
    private static final Map<String, SoundType> soundTypeMapInv = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        soundTypeMap.defaultReturnValue("CUSTOM");

        for(final Field f : ReflectionHelper.getStaticFields(SoundType.class)) {
            try {
                final SoundType type = (SoundType) f.get(null);
                soundTypeMap.put(type, f.getName());
            } catch(@Nonnull final Throwable ignore) {
            }
        }

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

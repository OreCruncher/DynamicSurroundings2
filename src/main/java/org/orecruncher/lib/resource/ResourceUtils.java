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

package org.orecruncher.lib.resource;

import net.minecraft.util.ResourceLocation;
import org.orecruncher.lib.fml.ForgeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public final class ResourceUtils {
    private ResourceUtils() {

    }

    /**
     * Scans the local disk as well as resource packs and JARs locating and creating accessors for the config file
     * in question.  Configs on disk have priority over resource packs and JARs, and 3rd party jars have priority
     * over the provided mod ID.
     * @param modId Mod ID that is considered the least in priority
     * @param root Location on disk where external configs can be cached
     * @param config The config file that is of interest
     * @return A collection of resource accessors that match the config criteria
     */
    public static Collection<IResourceAccessor> findConfigs(@Nonnull final String modId, @Nonnull final File root, @Nonnull final String config) {

        final Map<ResourceLocation, IResourceAccessor> locations = new HashMap<>();

        final List<String> modList = ForgeUtils.getConfigLocations();

        // Scan the local disk looking for config files.  These configs override everything else.
        for (final String mod : modList) {
            final ResourceLocation location = new ResourceLocation(mod, config);
            final IResourceAccessor accessor = IResourceAccessor.createExternalResource(root, location);
            if (accessor.exists())
                locations.put(location, accessor);
        }

        // Scan JARs looking for additional configs.  Do not include the parent - it goes last.  These JARs will
        // only contain configs that apply to themselves.
        for (final String mod : modList) {
            if (mod.equals(modId))
                continue;
            final String container = String.format("%s/configs", mod);
            final ResourceLocation location = new ResourceLocation(mod, config);
            if (!locations.containsKey(location)) {
                final IResourceAccessor accessor = IResourceAccessor.createJarResource(container, location);
                if (accessor.exists())
                    locations.put(location, accessor);
            }
        }

        // Lastly scan the parent.  The parent can contain references to multiple mods (dsurround)
        final String container = String.format("%s/configs", modId);
        for (final String mod1 : modList) {
            final ResourceLocation location = new ResourceLocation(mod1, config);
            if (!locations.containsKey(location)) {
                final IResourceAccessor accessor = IResourceAccessor.createJarResource(container, location);
                if (accessor.exists())
                    locations.put(location, accessor);
            }
        }

        return locations.values();
    }

    /**
     * Obtains the string content of the resource at the specified asset location with the JAR
     * @param location The resource to load
     * @return The content of the specified resource, or null if not found
     */
    @Nullable
    public static String readResource(@Nonnull final ResourceLocation location) {
        return readResource("", location);
    }

    /**
     * Obtains the string content of the resource at the specified asset location with the JAR
     * @param root Location is relative to this root in the JAR
     * @param location The resource to load
     * @return The content of the specified resource, or null if not found
     */
    @Nullable
    public static String readResource(@Nonnull final String root, @Nonnull final ResourceLocation location) {
        final IResourceAccessor accessor = IResourceAccessor.createJarResource(root, location);
        return accessor.asString();
    }
}

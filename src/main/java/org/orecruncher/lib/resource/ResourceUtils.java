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

import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.Lib;
import org.orecruncher.lib.fml.ForgeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public final class ResourceUtils {
    private ResourceUtils() {

    }

    private static final String MANIFEST_FILE = "manifest.json";

    private static Collection<String> cachedNamespaces = null;

    /**
     * Clears any cached namespace information
     */
    public static void  clearCache() {
        cachedNamespaces = null;
    }

    /**
     * Scans the local disk as well as resource packs and JARs locating and creating accessors for the config file
     * in question.  Configs on disk have priority over resource packs and JARs, and 3rd party jars have priority
     * over the provided mod ID.  These resources are read from CLIENT resources (assets) rather than SERVER
     * resources (data).
     * @param modId Mod ID that is considered the least in priority
     * @param root Location on disk where external configs can be cached
     * @param config The config file that is of interest
     * @return A collection of resource accessors that match the config criteria
     */
    public static Collection<IResourceAccessor> findConfigs(@Nonnull final String modId, @Nonnull final File root, @Nonnull final String config) {

        final String specialConfigFolder = modId + "_configs";
        final String resourceContainer = specialConfigFolder + "/%s";
        final String parentModConfigs = modId + "/configs";

        final Map<ResourceLocation, IResourceAccessor> locations = new HashMap<>();
        final Collection<ResourcePackInfo> packs = ForgeUtils.getEnabledResourcePacks();

        // Cache the namespaces so we don't do discovery unnecessarily.
        final Collection<String> namespaceList = discoverNamespaces(resourceContainer);

        // Look for resources in resource packs.  Mod resources will be exposed by an internal Forge resource pack
        // so we do not need to scan JARs directly.  The collection returned is already sorted so that the first
        // entries in the collection are lower priority that those further in the collection.  The result is that
        // higher priority resource packs will replace data from lower priority packs if there is an overlap.
        for (final ResourcePackInfo pack : packs) {
            final IResourcePack rp = pack.getResourcePack();
            final Set<String> namespaces = rp.getResourceNamespaces(ResourcePackType.CLIENT_RESOURCES);
            for (final String mod : namespaceList) {
                if (namespaces.contains(mod)) {
                    final String container = String.format(resourceContainer, config);
                    final ResourceLocation location = new ResourceLocation(mod, config);
                    IResourceAccessor accessor = IResourceAccessor.createPackResource(
                            rp,
                            location,
                            new ResourceLocation(mod, container));
                    if (accessor.exists()) {
                        // Need to make sure we use namespace:config as the location since it can be overridden
                        // by subsequent configs.
                        final ResourceLocation loc = new ResourceLocation(mod, config);
                        locations.put(loc, accessor);
                    }
                }
            }
        }

        // Scan resources from external configuration sources as well as default configs found in the parent JAR.
        // Data from external configs will replace any existing entries in the location dictionary since they are
        // considered highest priority.  Those from the parent JAR will only be applied if no other configs have
        // been loaded for the mod in question.
        for (final String mod : namespaceList) {
            ResourceLocation location = new ResourceLocation(mod, config);
            IResourceAccessor accessor = IResourceAccessor.createExternalResource(root, location);
            if (accessor.exists()) {
                locations.put(location, accessor);
                continue;
            }

            accessor = IResourceAccessor.createJarResource(parentModConfigs, location);
            if (accessor.exists())
                locations.put(location, accessor);
        }

        return locations.values();
    }

    /**
     * Scans resource packs locating sound.json configurations.
     * @return Collection of accessors to retrieve sound.json configurations.
     */
    public static Collection<IResourceAccessor> findSounds() {
        final List<IResourceAccessor> results = new ArrayList<>();
        final Collection<ResourcePackInfo> packs = ForgeUtils.getEnabledResourcePacks();

        for (final ResourcePackInfo pack : packs) {
            final IResourcePack rp = pack.getResourcePack();
            final Set<String> embeddedNamespaces = rp.getResourceNamespaces(ResourcePackType.CLIENT_RESOURCES);
            for (final String ns : embeddedNamespaces) {
                final ResourceLocation location = new ResourceLocation(ns, "sounds.json");
                final IResourceAccessor accessor = IResourceAccessor.createPackResource(pack.getResourcePack(), location, location);
                if (accessor.exists()) {
                    results.add(accessor);
                }
            }
        }

        return results;
    }

    private static Collection<String> discoverNamespaces(@Nonnull final String resourceContainer) {

        if (cachedNamespaces != null)
            return cachedNamespaces;

        final String container = String.format(resourceContainer, MANIFEST_FILE);

        // Initial namespace list is based on the currently loaded mod list
        final List<String> namespaces = new ArrayList<>(ForgeUtils.getModIdList());

        // Resource packs are a bit of a challenge.  There are two different ways that a pack can provide resource
        // information to us:
        //
        // 1. The pack contains resource info specific to another mod in the mod pack.  These resources should not be
        //    processed unless the mod is present in the pack.  The constructed Forge resource pack is an example of
        //    this style.
        //
        // 2. The pack itself has it's own namespace, and it is providing resources that are not associated with any
        //    other mod present.  We identify these namespaces by the presence of a manifest file.
        //
        final Collection<ResourcePackInfo> packs = ForgeUtils.getEnabledResourcePacks();

        for (final ResourcePackInfo pack : packs) {
            final IResourcePack rp = pack.getResourcePack();
            final Set<String> embeddedNamespaces = rp.getResourceNamespaces(ResourcePackType.CLIENT_RESOURCES);
            for (final String ns : embeddedNamespaces) {
                if (!namespaces.contains(ns)) {
                    final ResourceLocation location = new ResourceLocation(ns, container);
                    final IResourceAccessor accessor = IResourceAccessor.createPackResource(pack.getResourcePack(), location, location);
                    if (accessor.exists()) {
                        final Manifest manifest = accessor.as(Manifest.class);
                        if (manifest != null) {
                            Lib.LOGGER.info("Resource Pack namespace detected: %s", manifest.getName());
                            namespaces.add(ns);
                        }
                    }
                }
            }
        }

        cachedNamespaces = namespaces;
        return cachedNamespaces;
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

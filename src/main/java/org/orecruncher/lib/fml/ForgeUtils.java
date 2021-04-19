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

package org.orecruncher.lib.fml;

import net.minecraft.block.BlockState;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ForgeUtils {
    private ForgeUtils() {

    }

    @Nonnull
    public static Optional<? extends ModContainer> findModContainer(@Nonnull final String modId) {
        return ModList.get().getModContainerById(modId);
    }

    @Nonnull
    public static Optional<IModInfo> getModInfo(@Nonnull final String modId) {
        return findModContainer(modId).map(ModContainer::getModInfo);
    }

    @Nonnull
    public static String getModDisplayName(@Nonnull final String modId) {
        if ("minecraft".equalsIgnoreCase(modId))
            return "Minecraft";
        return getModInfo(modId).map(IModInfo::getDisplayName).orElse("UNKNOWN");
    }

    @Nonnull
    public static String getModDisplayName(@Nonnull final ResourceLocation resource) {
        Objects.requireNonNull(resource);
        return getModDisplayName(resource.getNamespace());
    }

    @Nullable
    public static ArtifactVersion getForgeVersion() {
        return getModInfo("forge").map(IModInfo::getVersion).orElse(null);
    }

    @Nonnull
    public static List<String> getModIdList() {
        List<String> result = ModList.get().getModFiles()
                .stream()
                .flatMap(e -> e.getMods().stream())
                .map(IModInfo::getModId)
                .distinct()
                .collect(Collectors.toList());

        // Make sure minecraft is the first element.  This is done to ensure that any baseline configs are applied
        // first so that other configs can properly override.
        result.remove("minecraft");
        result.add(0, "minecraft");

        return result;
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static Collection<ResourcePackInfo> getEnabledResourcePacks() {
        return GameUtils.getMC().getResourcePackList().getEnabledPacks();
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static List<String> getResourcePackIdList() {
        return getEnabledResourcePacks()
                .stream()
                .flatMap(e -> e.getResourcePack().getResourceNamespaces(ResourcePackType.CLIENT_RESOURCES).stream())
                .collect(Collectors.toList());
    }

    @Nonnull
    public static Collection<Biome> getBiomes() {
        return ForgeRegistries.BIOMES.getValues();
    }

    @Nonnull
    public static Collection<BlockState> getBlockStates() {
        return StreamSupport.stream(ForgeRegistries.BLOCKS.spliterator(), false)
                .map(block -> block.getStateContainer().getValidStates())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static boolean isModLoaded(@Nonnull final String mod) {
        return ModList.get().isLoaded(mod.toLowerCase());
    }
}

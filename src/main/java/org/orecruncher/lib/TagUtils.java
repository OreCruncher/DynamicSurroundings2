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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.tags.ITag;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.fml.ForgeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * Utility functions for dealing with tags.  With the current implementation of Minecraft/Forge, tags are not 100%
 * reliable until the client connects to the server.  The reason is that the tagging from the server is provided to
 * the client with the thought that datapacks on the server could make tweaks to tagging.  However, this does nothing
 * for mods that want to configure and do things based on collections similar to what was possible with the
 * OreDictionary in prior versions.
 *
 * The following routines will gather the tag data directly from resources on the CLIENT which should be fine in the
 * vast majority of cases.  If an authoritative source is needed use the Forge tag collections.
 */
@SuppressWarnings("unused")
public final class TagUtils {

    private static final Singleton<ITagCollection<Block>> blockTags = new Singleton<>(
            () -> {
                final ITagCollection<Block> tags = new TagCollection<>(
                        r -> Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(r)),
                        "tags/blocks",
                        false,
                        "block");

                return TagUtils.getTagCollection(tags);
            }
    );
    private static final Singleton<ITagCollection<Item>> itemTags = new Singleton<>(
            () -> {
                final ITagCollection<Item> tags = new TagCollection<>(
                        r -> Optional.ofNullable(ForgeRegistries.ITEMS.getValue(r)),
                        "tags/items",
                        false,
                        "item");

                return TagUtils.getTagCollection(tags);
            }
    );

    private TagUtils() {

    }

    /**
     * Obtain a tag collection based on client side data packs.  For the vast majority of cases for mod configuration
     * this is all that is required.
     *
     * @return TagCollection for blocks
     */
    private static <T> ITagCollection<T> getTagCollection(@Nonnull final ITagCollection<T> tags) {
        try {

            final SimpleReloadableResourceManager resourceManager = new SimpleReloadableResourceManager(ResourcePackType.SERVER_DATA);

            ForgeUtils.getEnabledResourcePacks()
                    .stream()
                    .map(ResourcePackInfo::getResourcePack)
                    .forEach(resourceManager::addResourcePack);

            final Map<ResourceLocation, Tag.Builder<T>> mapping = tags.reload(resourceManager, ForkJoinPool.commonPool()).get();
            tags.registerAll(mapping);

        } catch (@Nonnull final Throwable t) {
            Lib.LOGGER.error(t, "Unable to load tags!");
        }

        return tags;
    }

    @Nonnull
    public static Collection<ITag<Block>> getBlockStateTags(@Nonnull final BlockState state) {
        final ObjectArray<ITag<Block>> tags = new ObjectArray<>();
        for (final ResourceLocation res : state.getBlock().getTags()) {
            final ITag<Block> tag = getBlockTag(res);
            if (tag != null)
                tags.add(tag);
        }
        return tags;
    }

    @Nullable
    public static ITag<Block> getBlockTag(@Nonnull final String name) {
        return getBlockTag(new ResourceLocation(name));
    }

    @Nullable
    public static ITag<Block> getBlockTag(@Nonnull final ResourceLocation res) {
        return blockTags.instance().get(res);
    }

    @Nullable
    public static ITag<Item> getItemTag(@Nonnull final String name) {
        return getItemTag(new ResourceLocation(name));
    }

    @Nullable
    public static ITag<Item> getItemTag(@Nonnull final ResourceLocation res) {
        return itemTags.instance().get(res);
    }
}

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
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class TagUtils {

    private static final Singleton<TagCollection<Block>> blockTags = new Singleton<>(
            () -> {
                final TagCollection<Block> tags = new TagCollection<>(
                        r -> Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(r)),
                        "tags/blocks",
                        false,
                        "block");

                return TagUtils.getBlockTagCollection(tags);
            }
    );
    private static final Singleton<TagCollection<Item>> itemTags = new Singleton<>(
            () -> {
                final TagCollection<Item> tags = new TagCollection<>(
                        r -> Optional.ofNullable(ForgeRegistries.ITEMS.getValue(r)),
                        "tags/items",
                        false,
                        "item");

                return TagUtils.getBlockTagCollection(tags);
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
    private static <T> TagCollection<T> getBlockTagCollection(@Nonnull final TagCollection<T> tags) {
        final SimpleReloadableResourceManager resourceManager = new SimpleReloadableResourceManager(ResourcePackType.SERVER_DATA, Thread.currentThread());
        final List<IResourcePack> list = GameUtils.getMC().getResourcePackList().getEnabledPacks().stream().map(ResourcePackInfo::getResourcePack).collect(Collectors.toList());

        for (IResourcePack iresourcepack : list) {
            resourceManager.addResourcePack(iresourcepack);
        }

        try {
            final Map<ResourceLocation, Tag.Builder<T>> mapping = tags.reload(resourceManager, ForkJoinPool.commonPool()).get();
            tags.registerAll(mapping);
        } catch (@Nonnull final Throwable t) {
            Lib.LOGGER.error(t, "Unable to load tags!");
        }

        return tags;
    }

    @Nonnull
    public static Collection<Tag<Block>> getBlockStateTags(@Nonnull final BlockState state) {
        final List<Tag<Block>> tags = new ArrayList<>();
        for (final ResourceLocation res : state.getBlock().getTags()) {
            tags.add(getBlockTag(res));
        }
        return tags;
    }

    @Nullable
    public static Tag<Block> getBlockTag(@Nonnull final String name) {
        return getBlockTag(new ResourceLocation(name));
    }

    @Nullable
    public static Tag<Block> getBlockTag(@Nonnull final ResourceLocation res) {
        return blockTags.instance().get(res);
    }

    @Nullable
    public static Tag<Item> getItemTag(@Nonnull final String name) {
        return getItemTag(new ResourceLocation(name));
    }

    @Nullable
    public static Tag<Item> getItemTag(@Nonnull final ResourceLocation res) {
        return itemTags.instance().get(res);
    }
}

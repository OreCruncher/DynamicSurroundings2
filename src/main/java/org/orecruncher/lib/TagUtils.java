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
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;
import org.orecruncher.lib.collections.ObjectArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

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

    private TagUtils() {

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
        return TagCollectionManager.getManager().getBlockTags().get(res);
    }

    @Nullable
    public static ITag<Item> getItemTag(@Nonnull final String name) {
        return getItemTag(new ResourceLocation(name));
    }

    @Nullable
    public static ITag<Item> getItemTag(@Nonnull final ResourceLocation res) {
        return TagCollectionManager.getManager().getItemTags().get(res);
    }
}

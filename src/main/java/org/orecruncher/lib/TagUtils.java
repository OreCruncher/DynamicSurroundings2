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
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public final class TagUtils {
    private TagUtils() {

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
        return BlockTags.getCollection().get(res);
    }

    @Nullable
    public static Tag<Item> getItemTag(@Nonnull final String name) {
        return getItemTag(new ResourceLocation(name));
    }

    @Nullable
    public static Tag<Item> getItemTag(@Nonnull final ResourceLocation res) {
        return ItemTags.getCollection().get(res);
    }
}

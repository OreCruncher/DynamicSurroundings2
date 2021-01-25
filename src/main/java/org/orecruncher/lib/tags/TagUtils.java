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

package org.orecruncher.lib.tags;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.DynamicSurroundings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = DynamicSurroundings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TagUtils {

    private TagUtils() {

    }

    private static ITagCollectionSupplier supplier;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tagsUpdated(@Nonnull final TagsUpdatedEvent event) {
        supplier = event.getTagManager();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerLoggedOut(@Nonnull final ClientPlayerNetworkEvent.LoggedOutEvent event) {
        supplier = null;
    }

    @Nullable
    public static ITag<Block> getBlockTag(@Nonnull final String name) {
        return getBlockTag(new ResourceLocation(name));
    }

    @Nullable
    public static ITag<Block> getBlockTag(@Nonnull final ResourceLocation res) {
        if (supplier == null)
            return null;
        return supplier.getBlockTags().get(res);
    }

    public static Stream<String> dumpBlockTags() {
        if (supplier == null)
            return ImmutableList.<String>of().stream();

        final ITagCollection<Block> collection = supplier.getBlockTags();

        return collection.getRegisteredTags().stream().map(loc -> {
            final StringBuilder builder = new StringBuilder();
            builder.append(loc.toString()).append(" -> ");
            final ITag<Block> tag = collection.get(loc);
            final String text = tag.getAllElements().stream().map(l -> l.getRegistryName().toString()).collect(Collectors.joining(","));
            builder.append(text);
            return builder.toString();
        }).sorted();
    }
}

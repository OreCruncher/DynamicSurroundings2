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

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.sndctrl.SoundControl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientBlockUpdateHandler {
    private ClientBlockUpdateHandler(){

    }

    private static final Set<BlockPos> updates = new ObjectOpenHashSet<>(64);
    private static final List<Consumer<BlockPos>> callbackHandlers = new ArrayList<>();

    public static void blockUpdateCallback(@Nonnull final ClientWorld world, @Nonnull final BlockPos pos, @Nonnull final BlockState state) {
        if (callbackHandlers.size() > 0) {
            updates.add(pos);
        }
    }

    public static void registerCallback(@Nonnull final Consumer<BlockPos> callback) {
        callbackHandlers.add(callback);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {

        if(callbackHandlers.size() > 0) {
            for (final BlockPos pos : updates)
                callbackHandlers.forEach(h -> h.accept(pos));
        }

        updates.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onWorldLoad(@Nonnull final WorldEvent.Load event) {
        updates.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onWorldUnload(@Nonnull final WorldEvent.Unload event) {
        updates.clear();
    }
}

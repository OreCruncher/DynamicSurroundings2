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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.lib.events.BlockUpdateEvent;
import org.orecruncher.sndctrl.SoundControl;

import javax.annotation.Nonnull;
import java.util.Set;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientBlockUpdateHandler {
    private ClientBlockUpdateHandler(){

    }

    private static final Set<BlockPos> updates = new ObjectOpenHashSet<>(64);

    // Callback that is inserted into ClientWorld processing via ASM
    public static void blockUpdateCallback(@Nonnull final ClientWorld world, @Nonnull final BlockPos pos, @Nonnull final BlockState state) {
        updates.add(pos);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {
        if (updates.size() > 0) {
            final BlockUpdateEvent evt = new BlockUpdateEvent(updates);
            MinecraftForge.EVENT_BUS.post(evt);
            updates.clear();
        }
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

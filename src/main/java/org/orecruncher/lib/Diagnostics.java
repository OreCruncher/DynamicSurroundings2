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

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.sndctrl.SoundControl;

import javax.annotation.Nonnull;

/**
 * Diagnostic helper that fires an event every tick to collect data that will be rendered on the diagnostics
 * screen (F3).
 */
@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Diagnostics {

    private static MinecraftClock clock;
    @Nonnull
    private static DiagnosticEvent lastEvent = new DiagnosticEvent();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {
        if (GameUtils.displayDebug()) {

            if (clock == null)
                clock = new MinecraftClock();

            if (GameUtils.isInGame())
                clock.update(GameUtils.getWorld());

            final DiagnosticEvent evt = new DiagnosticEvent();
            evt.addLeft(TextFormatting.YELLOW + clock.getFormattedTime());

            MinecraftForge.EVENT_BUS.post(evt);
            lastEvent = evt;
        }
    }

    @SubscribeEvent
    public static void onGatherText(@Nonnull final RenderGameOverlayEvent.Text event) {
        if (GameUtils.displayDebug()) {
            if (!lastEvent.getLeft().isEmpty())
                event.getLeft().addAll(lastEvent.getLeft());

            if (!lastEvent.getRight().isEmpty())
                event.getRight().addAll(lastEvent.getRight());
        }
    }
}

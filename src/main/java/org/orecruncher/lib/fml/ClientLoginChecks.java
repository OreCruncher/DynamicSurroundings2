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

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.lib.Lib;
import org.orecruncher.lib.collections.ObjectArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Simple framework to register callbacks for whenever a player logs into a session.  These checks can be a
 * check for updated versions of a mod, or other checks about runtime that the player may need to know about (like
 * mod conflicts).
 */
@Mod.EventBusSubscriber(modid = DynamicSurroundings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientLoginChecks {

    private static final ObjectArray<ICallbackHandler> handlers = new ObjectArray<>();

    public static void register(@Nonnull final ICallbackHandler handler) {
        handlers.add(handler);
    }

    @SubscribeEvent
    public static void onLogin(@Nonnull final ClientPlayerNetworkEvent.LoggedInEvent event) {
        final ClientPlayerEntity player = event.getPlayer();
        if (player != null) {
            Lib.LOGGER.debug("Player login: %s", event.getPlayer().getName().getString());

            for (final ICallbackHandler callback : handlers) {
                final ITextComponent msg = callback.onLogin(event.getPlayer());
                if (msg != null)
                    event.getPlayer().sendMessage(msg, Util.DUMMY_UUID);
            }
        }
    }

    @FunctionalInterface
    public interface ICallbackHandler {
        /**
         * Invoked by the login process.  The result, if provided, will be sent to the local chat window to
         * inform the player.
         *
         * @param player The instance of the player that logged in
         * @return Text message to display to the player, if any
         */
        @Nullable
        ITextComponent onLogin(@Nonnull final ClientPlayerEntity player);
    }
}

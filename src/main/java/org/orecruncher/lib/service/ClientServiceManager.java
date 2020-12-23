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

package org.orecruncher.lib.service;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.orecruncher.lib.Lib;
import org.orecruncher.lib.Singleton;
import org.orecruncher.lib.collections.ObjectArray;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ClientServiceManager {

    private static final Singleton<ClientServiceManager> instance = new Singleton<>(ClientServiceManager::new);

    private final ObjectArray<IClientService> services = new ObjectArray<>();

    public static ClientServiceManager instance()
    {
        return instance.instance();
    }

    private ClientServiceManager()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Adds the service instance to the service manager.
     * @param svc Service to add
     */
    public void add(@Nonnull final IClientService svc)
    {
        services.add(svc);
    }

    /**
     * Instructs configured services to reload configuration
     */
    public void reload() {
        performAction("reload", IClientService::reload);
    }

    /**
     * Causes the service start phase to be invoked when a player logs in
     * @param event Event that is raised
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onStart(@Nonnull final ClientPlayerNetworkEvent.LoggedInEvent event) {
        performAction("start", IClientService::start);

        for (@Nonnull final IClientService svc : this.services)
            svc.log();
    }

    /**
     * Causes the service stop phase to be invoked when a player logs out
     * @param event Event that is raised
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onStop(@Nonnull final ClientPlayerNetworkEvent.LoggedOutEvent event) {
        performAction("stop", IClientService::stop);
    }

    private void performAction(@Nonnull final String actionName, @Nonnull final Consumer<IClientService> action) {

        Lib.LOGGER.info("Starting parallel action '%s'", actionName);

        long start = System.nanoTime();

        final List<String> results = services.parallelStream().map(svc -> {
            long begin = System.nanoTime();
            action.accept(svc);
            long duration = System.nanoTime() - begin;
            return String.format("Action '%s::%s' took %dmsecs", svc.name(), actionName, (long) (duration / 1000000D));
        }).collect(Collectors.toList());

        long duration = System.nanoTime() - start;
        for (final String r : results)
            Lib.LOGGER.info(r);

        Lib.LOGGER.info("Overall Action '%s' took %dmsecs", actionName, (long) (duration / 1000000D));
    }
}

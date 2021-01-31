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

import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.Lib;
import org.orecruncher.lib.Singleton;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.resource.ResourceUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ModuleServiceManager implements ISelectiveResourceReloadListener {

    private static final Singleton<ModuleServiceManager> instance = new Singleton<>(ModuleServiceManager::new);

    private final ObjectArray<IModuleService> services = new ObjectArray<>();

    public static ModuleServiceManager instance() {
        return instance.instance();
    }

    private ModuleServiceManager() {
        MinecraftForge.EVENT_BUS.register(this);
        final IResourceManager resourceManager = GameUtils.getMC().getResourceManager();
        ((IReloadableResourceManager) resourceManager).addReloadListener(this);
    }

    /**
     * Adds the service instance to the service manager.
     *
     * @param svc Service to add
     */
    public void add(@Nonnull final IModuleService svc) {
        services.add(svc);
    }

    /**
     * Instructs configured services to reload configuration
     */
    protected void reload() {
        ForgeUtils.getEnabledResourcePacks().forEach( p -> {
            Lib.LOGGER.info("Resource pack '%s'", p.getName());
            Lib.LOGGER.info("+  %s", p.getTitle().getString());
            Lib.LOGGER.info("+  %s", p.getDescription().getString());
        });
        performAction("reload", IModuleService::reload);
        this.services.forEach(IModuleService::log);
    }

    /**
     * Resource manager callback when resources change.  This can happen when a player alters the resource pack
     * list.
     *
     * @param resourceManager   Ignored
     * @param resourcePredicate Used to test which resource type is being reloaded
     */
    @Override
    public void onResourceManagerReload(@Nonnull final IResourceManager resourceManager, @Nonnull final Predicate<IResourceType> resourcePredicate) {
        // Reload based on sounds
        if (resourcePredicate.test(VanillaResourceType.SOUNDS)) {
            Lib.LOGGER.info("Received Resource reload callback");
            ResourceUtils.clearCache();
            reload();
        }
    }

    /**
     * This event is raised during login.  It triggers the process of configuring the various components after
     * tags and data have been synchronized between server and client.  It happens twice, so this logic will trigger
     * the reload process on the second or greater time the event is received.  (There has to be a better way...)
     *
     * @param event Ignored
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onStart(@Nonnull final TagsUpdatedEvent event) {
        // Cannot optimize.  Forge servers will send two of these events, Paper one.
        Lib.LOGGER.info("Received TagsUpdatedEvent - triggering reload");
        reload();
    }

    /**
     * Causes the service stop phase to be invoked when a player logs out
     *
     * @param event Event that is raised
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onStop(@Nonnull final ClientPlayerNetworkEvent.LoggedOutEvent event) {
        performAction("stop", IModuleService::stop);
    }

    private void performAction(@Nonnull final String actionName, @Nonnull final Consumer<IModuleService> action) {

        Lib.LOGGER.info("Starting action '%s'", actionName);

        long start = System.nanoTime();

        final List<String> results = this.services.stream().map(svc -> {
            long begin = System.nanoTime();
            action.accept(svc);
            long duration = System.nanoTime() - begin;
            return String.format("Action '%s::%s' took %dmsecs", svc.name(), actionName, (long) (duration / 1000000D));
        }).collect(Collectors.toList());

        long duration = System.nanoTime() - start;
        results.forEach(Lib.LOGGER::info);

        Lib.LOGGER.info("Overall Action '%s' took %dmsecs", actionName, (long) (duration / 1000000D));
    }
}

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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.Singleton;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.resource.ResourceUtils;
import org.orecruncher.lib.tags.TagUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = DynamicSurroundings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModuleServiceManager implements ISelectiveResourceReloadListener {

    private static final IModLog LOGGER = DynamicSurroundings.LOGGER.createChild(ModuleServiceManager.class);

    private static final Singleton<ModuleServiceManager> instance = new Singleton<>(ModuleServiceManager::new);

    private final ObjectArray<IModuleService> services = new ObjectArray<>();
    private boolean playerJoined = false;
    private boolean customTagsEventFired = false;
    private boolean vanillaTagsEventFired = false;
    private boolean reloadFired = false;

    private ModuleServiceManager() {
        final IResourceManager resourceManager = GameUtils.getMC().getResourceManager();
        ((IReloadableResourceManager) resourceManager).addReloadListener(this);
    }

    public static ModuleServiceManager instance() {
        return instance.get();
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
    private void reload() {
        ForgeUtils.getEnabledResourcePacks().forEach(p -> {
            LOGGER.debug("Resource pack '%s'", p.getName());
            LOGGER.debug("+  %s", p.getTitle().getString());
            LOGGER.debug("+  %s", p.getDescription().getString());
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
            reportStatus("Received Resource reload callback");
            ResourceUtils.clearCache();
            this.reload();
        }
    }

    private boolean readyForReload() {
        return this.playerJoined && (this.vanillaTagsEventFired || this.customTagsEventFired);
    }

    private void reportStatus(@Nonnull final String msg) {
        final String txt = String.format("%s (playerJoined: %b, reloadFired: %b, customTagsEventFired: %b, vanillaTagsEventFired: %b)",
                msg,
                this.playerJoined,
                this.reloadFired,
                this.customTagsEventFired,
                this.vanillaTagsEventFired);
        LOGGER.info(txt);
    }

    private void reloadIfReady() {
        if (!this.reloadFired && readyForReload()) {
            this.reloadFired = true;
            this.reload();
        }
    }

    private void clearReloadState() {
        reportStatus("Clearing reload state");
        this.playerJoined = false;
        this.reloadFired = false;
        this.customTagsEventFired = false;
        this.vanillaTagsEventFired = false;
    }

    @SubscribeEvent
    public static void entityJoinWorld(@Nonnull final EntityJoinWorldEvent event) {
        final PlayerEntity player = GameUtils.getPlayer();
        if (player != null && player.getEntityWorld().isRemote() && player.getEntityId() == event.getEntity().getEntityId()) {
            instance().joinWorld(event);
        }
    }

    private void joinWorld(@Nonnull final EntityJoinWorldEvent ignore) {
        this.playerJoined = true;
        reportStatus("EntityJoinWorldEvent fired");
        this.reloadIfReady();
    }

    @SubscribeEvent
    public static void onLoad(@Nonnull final TagsUpdatedEvent.VanillaTagTypes event) {
        instance().load(event);
    }

    private void load(@Nonnull final TagsUpdatedEvent.VanillaTagTypes event) {
        this.vanillaTagsEventFired = true;
        TagUtils.setTagManager(event.getTagManager());
        reportStatus("TagsUpdatedEvent.VanillaTagTypes fired");
        this.reloadIfReady();
    }

    @SubscribeEvent
    public static void load(@Nonnull final TagsUpdatedEvent.CustomTagTypes event) {
        instance().onLoad(event);
    }

    private void onLoad(@Nonnull final TagsUpdatedEvent.CustomTagTypes event) {
        this.customTagsEventFired = true;
        TagUtils.setTagManager(event.getTagManager());
        reportStatus("TagsUpdatedEvent.CustomTagTypes fired");
        this.reloadIfReady();
    }

    /**
     * Causes the service stop phase to be invoked when a player logs out
     *
     * @param event Event that is raised
     */
    @SubscribeEvent
    public static void onStop(@Nonnull final ClientPlayerNetworkEvent.LoggedOutEvent event) {
        instance().stop();
    }

    private void stop() {
        performAction("stop", IModuleService::stop);
        TagUtils.clearTagManager();
        this.clearReloadState();
    }

    private void performAction(@Nonnull final String actionName, @Nonnull final Consumer<IModuleService> action) {

        LOGGER.info("Starting action '%s'", actionName);

        long start = System.nanoTime();

        final List<String> results = this.services.stream().map(svc -> {
            long begin = System.nanoTime();
            action.accept(svc);
            long duration = System.nanoTime() - begin;
            return String.format("Action '%s::%s' took %dmsecs", svc.name(), actionName, (long) (duration / 1000000D));
        }).collect(Collectors.toList());

        long duration = System.nanoTime() - start;
        results.forEach(LOGGER::debug);

        LOGGER.info("Overall Action '%s' took %dmsecs", actionName, (long) (duration / 1000000D));
    }
}

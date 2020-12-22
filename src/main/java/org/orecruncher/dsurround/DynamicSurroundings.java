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

package org.orecruncher.dsurround;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.orecruncher.lib.fml.ConfigUtils;
import org.orecruncher.lib.fml.UpdateChecker;
import org.orecruncher.lib.logging.ModLog;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod(DynamicSurroundings.MOD_ID)
public final class DynamicSurroundings {

    /**
     * ID of the mod
     */
    public static final String MOD_ID = "dsurround";

    /**
     * Logging instance for trace
     */
    public static final ModLog LOGGER = new ModLog(DynamicSurroundings.class);

    /**
     * Path to the mod's configuration directory
     */
    public static final Path CONFIG_PATH = ConfigUtils.getConfigPath(MOD_ID);

    /**
     * Path to the external config data cache for user customization
     */
    public static final File DATA_PATH = Paths.get(CONFIG_PATH.toString(), "configs").toFile();

    public DynamicSurroundings() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            // Various event bus registrations
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupComplete);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
            MinecraftForge.EVENT_BUS.register(this);

            // Initialize our configuration
            Config.setup();

            if (!DATA_PATH.exists()) {
                try {
                    DATA_PATH.mkdirs();
                } catch (@Nonnull final Throwable t) {
                    LOGGER.error(t, "Unable to create configuration data path");
                }
            }
        }
    }

    private void commonSetup(@Nonnull final FMLCommonSetupEvent event) {
    }

    private void clientSetup(@Nonnull final FMLClientSetupEvent event) {
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void setupComplete(@Nonnull final FMLLoadCompleteEvent event) {
    }

    @SubscribeEvent
    public void onPlayerLogin(@Nonnull final PlayerLoggedInEvent event) {
        // TODO:  getString()?
        LOGGER.debug("Player login: %s", event.getPlayer().getDisplayName().getString());
        if (Config.CLIENT.logging.get_onlineVersionCheck())
            UpdateChecker.doCheck(event, MOD_ID);
    }
}

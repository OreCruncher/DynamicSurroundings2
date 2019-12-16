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

package org.orecruncher.sndctrl;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.orecruncher.lib.effects.EntityEffectHandler;
import org.orecruncher.lib.fml.ConfigUtils;
import org.orecruncher.lib.fml.UpdateChecker;
import org.orecruncher.lib.logging.ModLog;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.capabilities.CapabilityEntityFXData;
import org.orecruncher.sndctrl.library.AcousticLibrary;
import org.orecruncher.sndctrl.library.AudioEffectLibrary;
import org.orecruncher.sndctrl.library.EntityEffectLibrary;
import org.orecruncher.sndctrl.library.SoundLibrary;
import org.orecruncher.sndctrl.misc.ModEnvironment;

import javax.annotation.Nonnull;
import java.nio.file.Path;

@Mod(SoundControl.MOD_ID)
public final class SoundControl {

    /**
     * ID of the mod
     */
    public static final String MOD_ID = "sndctrl";
    /**
     * Logging instance for trace
     */
    public static final ModLog LOGGER = new ModLog(SoundControl.class);
    /**
     * Path to the mod's configuration directory
     */
    @Nonnull
    public static final Path CONFIG_PATH = ConfigUtils.getConfigPath(MOD_ID);

    public SoundControl() {

        // Various event bus registrations
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupComplete);
        MinecraftForge.EVENT_BUS.register(this);

        // Initialize our configuration
        Config.setup();
    }

    private void commonSetup(@Nonnull final FMLCommonSetupEvent event) {
        ModEnvironment.initialize();
        CapabilityEntityFXData.register();
    }

    private void clientSetup(@Nonnull final FMLClientSetupEvent event) {
        AudioEngine.initialize();
        SoundLibrary.initialize();
        AudioEffectLibrary.initialize();
        EntityEffectLibrary.initialize();
        EntityEffectHandler.initialize();
    }

    private void setupComplete(@Nonnull final FMLLoadCompleteEvent event) {
        // Mod initialization and IMC processing should have completed by now.  Do any further baking.
        AcousticLibrary.INSTANCE.initialize();
        EntityEffectLibrary.complete();
    }

    @SubscribeEvent
    public void onPlayerLogin(@Nonnull final PlayerLoggedInEvent event) {
        LOGGER.debug("Player login: %s", event.getPlayer().getDisplayName().getFormattedText());
        if (Config.CLIENT.logging.onlineVersionCheck.get())
            UpdateChecker.doCheck(event, MOD_ID);
    }
}

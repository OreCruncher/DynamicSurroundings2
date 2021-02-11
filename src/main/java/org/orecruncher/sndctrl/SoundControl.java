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

package org.orecruncher.sndctrl;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.effects.EntityEffectHandler;
import org.orecruncher.lib.logging.ModLog;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.sndctrl.api.IMC;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.lib.effects.entity.CapabilityEntityFXData;
import org.orecruncher.sndctrl.config.Config;
import org.orecruncher.sndctrl.gui.Keys;
import org.orecruncher.sndctrl.library.AcousticLibrary;
import org.orecruncher.sndctrl.library.AudioEffectLibrary;
import org.orecruncher.sndctrl.library.EntityEffectLibrary;
import org.orecruncher.sndctrl.library.SoundLibrary;

import javax.annotation.Nonnull;

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

    public SoundControl() {

        // Since we are 100% client side
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            // Various event bus registrations
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupComplete);
            MinecraftForge.EVENT_BUS.register(this);

            // Initialize our configuration
            Config.setup();

            DynamicSurroundings.doConfigMenuSetup();
        }
    }

    private void commonSetup(@Nonnull final FMLCommonSetupEvent event) {
        CapabilityEntityFXData.register();
    }

    private void clientSetup(@Nonnull final FMLClientSetupEvent event) {
        Keys.register();

        if (Config.CLIENT.effects.fixupRandoms.get()) {
            GameUtils.getMC().gameRenderer.random = new XorShiftRandom();
        }

        AudioEngine.initialize();
        EntityEffectLibrary.initialize();
        EntityEffectHandler.initialize();
    }

    private void setupComplete(@Nonnull final FMLLoadCompleteEvent event) {
        // Mod initialization and IMC processing should have completed by now.  Do any further baking.
        AudioEffectLibrary.initialize();
        EntityEffectLibrary.complete();

        // Callback initialization where the acoustic library is concerned.  Only way to serialize access because
        // of the new Forge parallel loading.
        IMC.processCompletions();

        // Initialize after.  Reason is that a mod could override a regular sound with a complex
        // acoustic, so we only want to create a SimpleAcoustic if it does not exist in the map.
        SoundLibrary.initialize();
        AcousticLibrary.initialize();
    }

}

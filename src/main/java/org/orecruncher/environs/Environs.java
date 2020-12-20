/*
 * Dynamic Surroundings: Environs
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

package org.orecruncher.environs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.orecruncher.environs.handlers.Manager;
import org.orecruncher.environs.library.Constants;
import org.orecruncher.environs.library.Libraries;
import org.orecruncher.lib.fml.ConfigUtils;
import org.orecruncher.lib.fml.UpdateChecker;
import org.orecruncher.lib.logging.ModLog;
import org.orecruncher.sndctrl.api.IMC;

import javax.annotation.Nonnull;
import java.nio.file.Path;

@Mod(Environs.MOD_ID)
public final class Environs {

    /**
     * ID of the mod
     */
    public static final String MOD_ID = "environs";
    /**
     * Logging instance for trace
     */
    public static final ModLog LOGGER = new ModLog(Environs.class);
    /**
     * Path to the mod's configuration directory
     */
    @Nonnull
    public static final Path CONFIG_PATH = ConfigUtils.getConfigPath(MOD_ID);

    public Environs() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            // Various event bus registrations
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupComplete);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
            MinecraftForge.EVENT_BUS.register(this);

            // Initialize our configuration
            Config.setup();
        }
    }

    private void commonSetup(@Nonnull final FMLCommonSetupEvent event) {

    }

    private void clientSetup(@Nonnull final FMLClientSetupEvent event) {
        // Disable Particles if configured to do so
        if (Config.CLIENT.effects.get_disableUnderwaterParticles())
            Minecraft.getInstance().particles.registerFactory(ParticleTypes.UNDERWATER, (IParticleFactory<BasicParticleType>) null);
    }

    private void setupComplete(@Nonnull final FMLLoadCompleteEvent event) {

    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        IMC.registerSoundCategory(Constants.BIOMES);
        IMC.registerSoundCategory(Constants.SPOT_SOUNDS);

        IMC.registerSoundFile(new ResourceLocation(MOD_ID, "sounds.json"));
        IMC.registerAcousticFile(new ResourceLocation(MOD_ID, "acoustics.json"));

        IMC.registerCompletionCallback(Libraries::initialize);
        IMC.registerCompletionCallback(Libraries::complete);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void clientConnect(@Nonnull final ClientPlayerNetworkEvent.LoggedInEvent event) {
        Manager.connect();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void clientDisconnect(@Nonnull final ClientPlayerNetworkEvent.LoggedOutEvent event) {
        Manager.disconnect();
    }

    @SubscribeEvent
    public void onPlayerLogin(@Nonnull final PlayerLoggedInEvent event) {
        LOGGER.debug("Player login: %s", event.getPlayer().getDisplayName().getFormattedText());
        if (Config.CLIENT.logging.get_onlineVersionCheck())
            UpdateChecker.doCheck(event, MOD_ID);
    }
}

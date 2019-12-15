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

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.sndctrl.audio.Category;
import org.orecruncher.sndctrl.audio.ISoundCategory;
import org.orecruncher.sndctrl.audio.acoustic.AcousticEvent;
import org.orecruncher.sndctrl.library.AcousticLibrary;
import org.orecruncher.sndctrl.library.SoundLibrary;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Helper interface used to register items with Sound Control using IMC.
 */
@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class IMC {

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(IMC.class);
    private static final Map<String, Consumer<InterModComms.IMCMessage>> dispatchTable = new HashMap<>(4);

    static {
        dispatchTable.put(Constants.REGISTER_ACOUSTIC_EVENT, IMC::registerAcousticEventHandler);
        dispatchTable.put(Constants.REGISTER_SOUND_CATEGORY, IMC::registerSoundCategoryHandler);
        dispatchTable.put(Constants.REGISTER_ACOUSTIC_FILE, IMC::registerAcousticFileHandler);
        dispatchTable.put(Constants.REGISTER_SOUND_META, IMC::registerSoundMetaHandler);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(IMC::processIMC);
    }

    private IMC() {

    }

    private static void processIMC(@Nonnull final InterModProcessEvent event) {

        final List<InterModComms.IMCMessage> msgs = event.getIMCStream()
                .collect(Collectors.toList());

        for (final InterModComms.IMCMessage msg : msgs) {
            LOGGER.debug("Processing IMC message '%s' from '%s'", msg.getMethod(), msg.getSenderModId());
            final Consumer<InterModComms.IMCMessage> disp = dispatchTable.get(msg.getMethod());
            if (disp != null) {
                disp.accept(msg);
            } else {
                LOGGER.debug("Did not understand request '%s'", msg.getMethod());
            }
        }
    }

    private static void registerAcousticEventHandler(@Nonnull final InterModComms.IMCMessage msg) {
        final Optional<AcousticEvent> event = Utilities.safeCast(msg.getMessageSupplier().get(), AcousticEvent.class);
        event.ifPresent(AcousticEvent::register);
    }

    private static void registerSoundCategoryHandler(@Nonnull final InterModComms.IMCMessage msg) {
        final Optional<ISoundCategory> event = Utilities.safeCast(msg.getMessageSupplier().get(), ISoundCategory.class);
        event.ifPresent(Category::register);
    }

    private static void registerSoundMetaHandler(@Nonnull final InterModComms.IMCMessage msg) {
        final Optional<ResourceLocation> event = Utilities.safeCast(msg.getMessageSupplier().get(), ResourceLocation.class);
        event.ifPresent(SoundLibrary::registerSoundMeta);
    }

    private static void registerAcousticFileHandler(@Nonnull final InterModComms.IMCMessage msg) {
        final Optional<ResourceLocation> event = Utilities.safeCast(msg.getMessageSupplier().get(), ResourceLocation.class);
        event.ifPresent(AcousticLibrary.INSTANCE::processFile);
    }

    /**
     * Adds an AcousticEvent to the system so that it is recognized by the compiler
     *
     * @param event The Acoustic Event to register
     */
    public static void registerAcousticEvent(@Nonnull final AcousticEvent event) {
        InterModComms.sendTo(SoundControl.MOD_ID, Constants.REGISTER_ACOUSTIC_EVENT, () -> event);
    }

    /**
     * Adds a Sound Category to the system so that it is recognized by the compiler
     *
     * @param category Sound Category to register
     */
    public static void registerSoundCategory(@Nonnull final ISoundCategory category) {
        InterModComms.sendTo(SoundControl.MOD_ID, Constants.REGISTER_SOUND_CATEGORY, () -> category);
    }

    /**
     * Have Sound Control scan the specified sound file looking for meta data as well as additional sounds
     * that can be played.
     *
     * @param soundFile Sound file to process for meta data and additional sounds
     */
    public static void regigisterSoundMeta(@Nonnull final ResourceLocation soundFile) {
        InterModComms.sendTo(SoundControl.MOD_ID, Constants.REGISTER_SOUND_META, () -> soundFile);
    }

    /**
     * Have Sound Control scan the specified Json file to configure acoustics for the acoustic library.
     *
     * @param acousticFile Acoustic file to process
     */
    public static void registerAcousticFile(@Nonnull final ResourceLocation acousticFile) {
        InterModComms.sendTo(SoundControl.MOD_ID, Constants.REGISTER_ACOUSTIC_FILE, () -> acousticFile);
    }

    private static class Constants {
        public static final String REGISTER_ACOUSTIC_EVENT = "rae";
        public static final String REGISTER_SOUND_CATEGORY = "rsc";
        public static final String REGISTER_ACOUSTIC_FILE = "raf";
        public static final String REGISTER_SOUND_META = "rsm";
    }

}

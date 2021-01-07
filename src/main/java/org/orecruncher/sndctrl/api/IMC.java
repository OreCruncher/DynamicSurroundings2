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

package org.orecruncher.sndctrl.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.api.acoustics.AcousticEvent;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.api.effects.IEntityEffectFactoryHandler;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.library.EntityEffectLibrary;
import org.orecruncher.sndctrl.library.SoundLibrary;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Helper interface used to register items with Sound Control using IMC.  Because of the parallel loading of Forge
 * intermod communication outside of IMC can cause all types of difficulties.
 */
@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class IMC {

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(IMC.class);
    private static final ObjectArray<Runnable> callbacks = new ObjectArray<>(4);

    static {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(IMC::processIMC);
    }

    private IMC() {

    }

    private static void processIMC(@Nonnull final InterModProcessEvent event) {
        event.getIMCStream().forEach(msg -> {
            try {
                Methods method = Methods.valueOf(msg.getMethod());
                method.handle(msg);
            } catch(@Nonnull final Throwable t) {
                SoundControl.LOGGER.warn("Unable to process IMC message '%s' - unrecognized?", msg.getMethod());
            }
        });
    }

    private static void registerAcousticEventHandler(@Nonnull final InterModComms.IMCMessage msg) {
        handle(msg, AcousticEvent.class, AcousticEvent::register);
    }

    private static void registerSoundCategoryHandler(@Nonnull final InterModComms.IMCMessage msg) {
        handle(msg, ISoundCategory.class, Category::register);
    }

    private static void registerSoundFileHandler(@Nonnull final InterModComms.IMCMessage msg) {
        handle(msg, ResourceLocation.class, SoundLibrary::registerSoundFile);
    }

    private static void registerEffectFactoryHandlerHandler(@Nonnull final InterModComms.IMCMessage msg) {
        handle(msg, IEntityEffectFactoryHandler.class, EntityEffectLibrary::register);
    }

    private static void registerCompletionCallbackHandler(@Nonnull final InterModComms.IMCMessage msg) {
        Utilities.safeCast(msg.getMessageSupplier().get(), Runnable.class).ifPresent(callbacks::add);
    }

    private static <T> void handle(@Nonnull final InterModComms.IMCMessage msg, @Nonnull final Class<T> clazz, @Nonnull final Consumer<T> handler) {
        Utilities.safeCast(msg.getMessageSupplier().get(), clazz).ifPresent(handler);
    }

    /**
     * Adds an AcousticEvent to the system so that it is recognized by the compiler
     *
     * @param event The Acoustic Event to register
     */
    public static void registerAcousticEvent(@Nonnull final AcousticEvent... event) {
        for (final AcousticEvent e : event)
            Methods.REGISTER_ACOUSTIC_EVENT.send(() -> e);
    }

    /**
     * Adds a Sound Category to the system so that it is recognized by the compiler
     *
     * @param category Sound Category to register
     */
    public static void registerSoundCategory(@Nonnull final ISoundCategory... category) {
        for (final ISoundCategory c : category)
            Methods.REGISTER_SOUND_CATEGORY.send(() -> c);
    }

    /**
     * Have Sound Control scan the specified sound file looking for meta data as well as additional sounds
     * that can be played.
     *
     * @param soundFile Sound file to process for meta data and additional sounds
     */
    public static void registerSoundFile(@Nonnull final ResourceLocation... soundFile) {
        for (final ResourceLocation r : soundFile)
            Methods.REGISTER_SOUND_FILE.send(() -> r);
    }

    /**
     * Register an EffectFactoryHandler for the entity effect system.
     *
     * @param handler Effect handler to register
     */
    public static void registerEffectFactoryHandler(@Nonnull final IEntityEffectFactoryHandler... handler) {
        for (final IEntityEffectFactoryHandler h : handler)
            Methods.REGISTER_EFFECT_FACTORY_HANDLER.send(() -> h);
    }

    /**
     * Register a callback method to be invoked during completion processing.  Call may come back on a separate thread.
     * This method is useful to prevent concurrent access by mods during setup when they need to access the acoustic
     * library and such.
     *
     * @param callback  Callback to invoke on completion
     */
    public static void registerCompletionCallback(@Nonnull final Runnable... callback) {
        for (final Runnable r : callback)
            Methods.REGISTER_COMPLETION_CALLBACK.send(() -> r);
    }

    /**
     * Called by the startup routine to process any callbacks that were posted.  Not to be called by other mods!
     */
    public static void processCompletions() {
        for (final Runnable r : callbacks) {
            try {
                r.run();
            } catch (@Nonnull final Throwable t) {
                LOGGER.error(t, "Error executing completion processing routine");
            }
        }
        callbacks.clear();
    }

    private enum Methods {
        REGISTER_ACOUSTIC_EVENT(IMC::registerAcousticEventHandler),
        REGISTER_SOUND_CATEGORY(IMC::registerSoundCategoryHandler),
        REGISTER_SOUND_FILE(IMC::registerSoundFileHandler),
        REGISTER_EFFECT_FACTORY_HANDLER(IMC::registerEffectFactoryHandlerHandler),
        REGISTER_COMPLETION_CALLBACK(IMC::registerCompletionCallbackHandler);

        private final Consumer<InterModComms.IMCMessage> handler;

        Methods(@Nonnull final Consumer<InterModComms.IMCMessage> handler) {
            this.handler = handler;
        }

        public void handle(@Nonnull final InterModComms.IMCMessage msg) {
            LOGGER.debug("Processing IMC message '%s' from '%s'", msg.getMethod(), msg.getSenderModId());
            try {
                this.handler.accept(msg);
            } catch (@Nonnull final Throwable t) {
                LOGGER.error(t, "Error processing IMC message");
            }
        }

        public void send(@Nonnull final Supplier<?> sup) {
            InterModComms.sendTo(SoundControl.MOD_ID, this.name(), sup);
        }
    }

}

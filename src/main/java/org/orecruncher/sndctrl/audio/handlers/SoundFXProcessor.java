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

package org.orecruncher.sndctrl.audio.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.threading.Worker;
import org.orecruncher.sndctrl.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.handlers.effects.LowPassFilter;
import org.orecruncher.sndctrl.audio.handlers.effects.ReverbData;
import org.orecruncher.sndctrl.audio.handlers.effects.ReverbEffect;
import org.orecruncher.sndctrl.events.AudioEvent;
import org.orecruncher.sndctrl.xface.ISoundSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListMap;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoundFXProcessor {

    // Settings used by the system
    static final ReverbEffect reverb = new ReverbEffect();
    static final LowPassFilter lowPass = new LowPassFilter();
    static final ReverbData roomReverb = new ReverbData();
    private static final int SOUND_PROCESS_ITERATION = 1000 / 30;
    private static final int ROOM_PROCESS_ITERATION = 1000 / 20;
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundFXProcessor.class);
    private static final ConcurrentSkipListMap<Integer, SourceContext> sources = new ConcurrentSkipListMap<>();
    static boolean isAvailable;
    @Nullable
    private static Worker soundProcessor;
    @Nonnull
    private static Worker roomProcessor;
    private static WorldContext worldContext = new WorldContext();

    static {
        MinecraftForge.EVENT_BUS.register(SoundFXProcessor.class);
    }

    private SoundFXProcessor() {
    }

    @Nullable
    public static WorldContext getWorldContext() {
        return worldContext;
    }

    /**
     * Indicates if the SoundFX feature is available.
     *
     * @return true if the feature is available, false otherwise.
     */
    public static boolean isAvailable() {
        return isAvailable;
    }

    /**
     * This method is invoked via the MixinSoundSystem injection.  It will be called when the sound system
     * is intialized, and it gives an opportunity to setup special effects processing.
     *
     * @param device Device context created by the Minecraft sound system
     */
    public static void initialize(final long device) {

        if (!Config.CLIENT.sound.enableEnhancedSounds.get())
            return;

        final ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        if (!deviceCaps.ALC_EXT_EFX) {
            LOGGER.warn("EFX audio extensions not found on the current device.  Sound FX features will be disabled.");
            return;
        }

        try {
            reverb.initialize();
            lowPass.initialize();
            soundProcessor = new Worker(
                    "SoundControl Sound Processor",
                    SoundFXProcessor::processSounds,
                    SOUND_PROCESS_ITERATION,
                    LOGGER
            );
            soundProcessor.start();

            roomProcessor = new Worker(
                    "SoundControl Room Processor",
                    SoundFXProcessor::processRoom,
                    ROOM_PROCESS_ITERATION,
                    LOGGER
            );
            roomProcessor.start();

            isAvailable = true;
        } catch (@Nonnull final Throwable t) {
            LOGGER.warn(t.getMessage());
            LOGGER.warn("OpenAL special effects for sounds will not be available");
        }

    }

    /**
     * Event raised by the sound system when a sound source is created.  The event handler will initialize the
     * SourceHandler for the sound source if the sound matches the right criteria.
     *
     * @param event Event that was raised.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSoundPlay(@Nonnull final SoundEvent.SoundSourceEvent event) {
        if (!isAvailable())
            return;

        Utilities.safeCast(event.getSource(), ISoundSource.class).ifPresent(src -> {
            final SourceContext ctx = src.getSourceContext();
            ctx.attachSound(event.getSound());
            final SoundCategory cat = event.getSound().getCategory();
            if (cat == SoundCategory.MUSIC || cat == SoundCategory.MASTER) {
                ctx.disableEffects();
            } else {
                sources.put(src.getSourceId(), ctx);
            }
        });
    }

    /**
     * Injected into SoundSource and will be invoked when a sound source is being terminated.
     *
     * @param soundId The ID of the sound source that is being removed.
     */
    public static void stopSoundPlay(final int soundId) {
        if (isAvailable())
            sources.remove(soundId);
    }

    /**
     * Invoked on a client tick.  Precalculate overall environmental impacts for effects.
     *
     * @param event Event trigger in question.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {

        if (isAvailable() && event.side == LogicalSide.CLIENT && event.phase == TickEvent.Phase.START) {
            worldContext = new WorldContext();
        }
    }

    /**
     * Separate thread for evaluating the environment for the sound play.  These routines can get a little heavy
     * so offloading to a separate thread to keep it out of either the client tick or sound engine makes sense.
     */
    private static void processSounds() {
        final Collection<SourceContext> srcs = sources.values();
        srcs.forEach(SourceContext::update);
    }

    /**
     * Separate thread for processing the room around the player to get an accoustic layout.  The goal is to establish
     * area geometry and have that affect reverb of a sound that is playing.
     */
    private static void processRoom() {
        SoundFXUtils.calculateReverb(roomReverb, worldContext);
    }

    /**
     * Invoked when the client wants to know the current rain strength.
     *
     * @param event Event trigger in question.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public static void onPrecipitationStrength(@Nonnull final AudioEvent.PrecipitationStrengthEvent event) {
        event.setStrength(Minecraft.getInstance().world.rainingStrength);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onGatherText(@Nonnull final RenderGameOverlayEvent.Text event) {
        if (isAvailable() && Minecraft.getInstance().gameSettings.showDebugInfo) {
            String msg = soundProcessor.getDiagnosticString();
            if (!StringUtils.isEmpty(msg))
                event.getLeft().add(TextFormatting.GREEN + msg);
            msg = roomProcessor.getDiagnosticString();
            if (!StringUtils.isEmpty(msg))
                event.getLeft().add(TextFormatting.GREEN + msg);
        }
    }

    /**
     * Validates that the current OpenAL state is not in error.  If in an error state an exception will be thrown.
     */
    public static void validate() {
        validate(null);
    }

    /**
     * Validates that the current OpenAL state is not in error.  If in an error state an exception will be thrown.
     *
     * @param msg Message to add to the exception information if it is thrown.
     */
    public static void validate(@Nullable String msg) {
        final int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            String errorName = AL10.alGetString(error);
            if (StringUtils.isEmpty(errorName))
                errorName = Integer.toString(error);
            if (StringUtils.isEmpty(msg))
                msg = "NONE";
            throw new IllegalStateException(String.format("OpenAL Error: %s [%s]", errorName, msg));
        }
    }
}

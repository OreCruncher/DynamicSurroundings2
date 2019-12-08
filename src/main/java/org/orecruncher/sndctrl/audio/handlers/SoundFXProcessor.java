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

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ChannelManager;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.lwjgl.openal.AL10;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.threading.Worker;
import org.orecruncher.sndctrl.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.SoundUtils;
import org.orecruncher.sndctrl.events.AudioEvent;
import org.orecruncher.sndctrl.mixins.IChannelManagerEntry;
import org.orecruncher.sndctrl.xface.ISoundSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoundFXProcessor {

    /**
     * Sound categories that are ignored when determing special effects.  Things like MASTER, RECORDS, and MUSIC.
     */
    private static final Set<SoundCategory> IGNORE_CATEGORIES = new ReferenceOpenHashSet<>();

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundFXProcessor.class);
    static boolean isAvailable;
    private static final int SOUND_PROCESS_ITERATION = 1000 / 20;   // Match MC client tick rate
    private static final int SOUND_PROCESS_THREADS;
    // Sparse array to hold references to the SoundContexts of playing sounds
    private static SourceContext[] sources;
    @Nullable
    private static Worker soundProcessor;
    // Use our own ForkJoinPool avoiding the common pool.  Thread allocation is better controlled, and we won't run
    // into/cause any problems with other tasks in the common pool.
    @Nonnull
    private static final LazyInitializer<ForkJoinPool> threadPool = new LazyInitializer<ForkJoinPool>() {
        @Override
        protected ForkJoinPool initialize() {
            LOGGER.info("Threads allocated to SoundControl sound processor: %d", SOUND_PROCESS_THREADS);
            return new ForkJoinPool(SOUND_PROCESS_THREADS);
        }
    };

    @Nonnull
    private static WorldContext worldContext = new WorldContext();

    static {

        int threads = Config.CLIENT.sound.backgroundThreadWorkers.get();
        if (threads == 0)
            threads = 1;
        SOUND_PROCESS_THREADS = threads;

        IGNORE_CATEGORIES.add(SoundCategory.RECORDS);   // Jukebox
        IGNORE_CATEGORIES.add(SoundCategory.MUSIC);     // Background music
        IGNORE_CATEGORIES.add(SoundCategory.MASTER);    // Anything slotted to master, like menu buttons

        // Don't process weather sounds if configured
        if (!Config.CLIENT.sound.enhancedWeather.get())
            IGNORE_CATEGORIES.add(SoundCategory.WEATHER);

        MinecraftForge.EVENT_BUS.register(SoundFXProcessor.class);
    }

    private SoundFXProcessor() {
    }

    public static boolean isCategoryIgnored(@Nonnull final SoundCategory cat) {
        return IGNORE_CATEGORIES.contains(cat);
    }

    @Nonnull
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

    public static void initialize() {

        Effects.initialize();

        sources = new SourceContext[SoundUtils.getMaxSounds()];

        soundProcessor = new Worker(
                "SoundControl Sound Processor",
                SoundFXProcessor::processSounds,
                SOUND_PROCESS_ITERATION,
                LOGGER
        );
        soundProcessor.start();

        isAvailable = true;
    }

    /**
     * Callback hook from a mixin injection.  This callback is made on the client thread after the sound source
     * is created, but before it is configured.
     *
     * @param sound The sound that is going to play
     * @param entry The ChannelManager.Entry instance for the sound play
     */
    public static void onSoundPlay(@Nonnull final ISound sound, @Nonnull final ChannelManager.Entry entry) {

        if (!isAvailable())
            return;

        try {

            final Optional<IChannelManagerEntry> optionalCme = Utilities.safeCast(entry, IChannelManagerEntry.class);
            if (!optionalCme.isPresent())
                return;

            final IChannelManagerEntry cme = optionalCme.get();

            // Because of where this is hooked SoundSource may not have been created.  Have to wait for creation.
            // Note that this yield can be a bit dangerous.  There is a limit as to the number of sound sources that
            // can play at a time, but the Minecraft sound handler will queue up more sounds that permitted.  When
            // this happens SoundSource will be null, and the while loop below will block the client thread from
            // processing.  There is a check in SoundProcessor that if more sounds that can be queued is performed it
            // will block that sound play to prevent overload.
            SoundSource src;
            while ((src = cme.getSource()) == null)
                Thread.yield();

            Utilities.safeCast(src, ISoundSource.class).ifPresent(ss -> {
                final SourceContext ctx = ss.getSourceContext();
                ctx.attachSound(sound);
                if (!isCategoryIgnored(sound.getCategory())) {
                    ctx.enable();
                    final int idx = sourceIdToIdx(ss.getSourceId());
                    // First update before first actual play.  This is occuring on the client thread.
                    ctx.exec();
                    sources[idx] = ctx;
                }
            });
        } catch (@Nonnull final Throwable t) {
            LOGGER.error(t, "Error obtaining SoundSource information in callback");
        }

    }

    /**
     * Injected into SoundSource and will be invoked when a sound source is being terminated.
     *
     * @param soundId The ID of the sound source that is being removed.
     */
    public static void stopSoundPlay(final int soundId) {
        if (isAvailable())
            sources[sourceIdToIdx(soundId)] = null;
    }

    /**
     * Invoked on a client tick. Establishes the current world context for further computation..
     *
     * @param event Event trigger in question.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
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
        try {
            final ForkJoinPool pool = threadPool.get();
            for (int i = 0; i < SoundUtils.getMaxSounds(); i++) {
                final SourceContext ctx = sources[i];
                if (ctx != null && ctx.shouldExecute())
                    pool.execute(ctx);
            }
            pool.awaitQuiescence(5, TimeUnit.MINUTES);
        } catch (@Nonnull final Throwable t) {
            LOGGER.error(t, "Error in SoundContext ForkJoinPool");
        }
    }

    /**
     * Invoked when the client wants to know the current rain strength.
     *
     * @param event Event trigger in question.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPrecipitationStrength(@Nonnull final AudioEvent.PrecipitationStrengthEvent event) {
        event.setStrength(Minecraft.getInstance().world.rainingStrength);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onGatherText(@Nonnull final RenderGameOverlayEvent.Text event) {
        if (isAvailable() && Minecraft.getInstance().gameSettings.showDebugInfo && soundProcessor != null) {
            final String msg = soundProcessor.getDiagnosticString();
            if (!StringUtils.isEmpty(msg))
                event.getLeft().add(TextFormatting.GREEN + msg);
        }
    }

    private static int sourceIdToIdx(final int soundId) {
        final int idx = soundId - 1;
        if (idx < 0 || idx >= SoundUtils.getMaxSounds()) {
            throw new IllegalStateException("Invalid source ID: " + idx);
        }
        return idx;
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

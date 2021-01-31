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

package org.orecruncher.sndctrl.audio.handlers;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.audio.AudioStreamBuffer;
import net.minecraft.client.audio.ChannelManager;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
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
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.threading.Worker;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.config.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.Conversion;
import org.orecruncher.sndctrl.audio.SoundUtils;
import org.orecruncher.sndctrl.misc.IMixinSoundContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoundFXProcessor {

    /**
     * Sound categories that are ignored when determining special effects.  Things like MASTER, and MUSIC.
     */
    private static final Set<SoundCategory> IGNORE_CATEGORIES = new ReferenceOpenHashSet<>();

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundFXProcessor.class);
    static boolean isAvailable;
    private static final int SOUND_PROCESS_ITERATION = 1000 / 20;   // Match MC client tick rate
    private static final int SOUND_PROCESS_THREADS;
    // Sparse array to hold references to the SoundContexts of playing sounds
    private static SourceContext[] sources;
    private static Worker soundProcessor;
    // Use our own ForkJoinPool avoiding the common pool.  Thread allocation is better controlled, and we won't run
    // into/cause any problems with other tasks in the common pool.
    private static final LazyInitializer<ForkJoinPool> threadPool = new LazyInitializer<ForkJoinPool>() {
        @Override
        protected ForkJoinPool initialize() {
            LOGGER.info("Threads allocated to SoundControl sound processor: %d", SOUND_PROCESS_THREADS);
            return new ForkJoinPool(SOUND_PROCESS_THREADS);
        }
    };

    private static WorldContext worldContext = new WorldContext();

    static {

        int threads = Config.CLIENT.sound.backgroundThreadWorkers.get();
        if (threads == 0)
            threads = 1;
        SOUND_PROCESS_THREADS = threads;

        IGNORE_CATEGORIES.add(SoundCategory.MUSIC);     // Background music
        IGNORE_CATEGORIES.add(SoundCategory.MASTER);    // Anything slotted to master, like menu buttons

        MinecraftForge.EVENT_BUS.register(SoundFXProcessor.class);
    }

    private SoundFXProcessor() {
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

        if (soundProcessor == null) {
            soundProcessor = new Worker(
                    "SoundControl Sound Processor",
                    SoundFXProcessor::processSounds,
                    SOUND_PROCESS_ITERATION,
                    LOGGER
            );
            soundProcessor.start();
        }

        isAvailable = true;
    }

    public static void deinitialize() {
        if (isAvailable()) {
            isAvailable = false;
            if (soundProcessor != null) {
                soundProcessor.stop();
                soundProcessor = null;
            }
            if (sources != null) {
                Arrays.fill(sources, null);
                sources = null;
            }
            Effects.deinitialize();
        }
    }

    /**
     * Callback hook from an injection.  This callback is made on the client thread after the sound source
     * is created, but before it is configured.
     *
     * @param sound The sound that is going to play
     * @param entry The ChannelManager.Entry instance for the sound play
     */
    public static void onSoundPlay(@Nonnull final ISound sound, @Nonnull final SoundCategory category, @Nonnull final ChannelManager.Entry entry) {

        if (!isAvailable())
            return;

        if (IGNORE_CATEGORIES.contains(category))
            return;

        // Double suplex!  Queue the operation on the sound executor to do the config work.  This should queue in
        // behind any attempt at getting a sound source.
        entry.runOnSoundExecutor(source -> {
            if (source.id > 0) {
                final SourceContext ctx = new SourceContext();
                ctx.attachSound(sound);
                ctx.enable();
                ctx.exec();
                ((IMixinSoundContext) source).setData(ctx);
                sources[source.id - 1] = ctx;
            }
        });
    }

    /**
     * Callback hook from an injection.  Will be invoked by the sound processing thread when checking status which
     * essentially is a "tick".
     *
     * @param source SoundSource being ticked
     */
    public static void tick(@Nonnull final SoundSource source) {
        final SourceContext ctx = ((IMixinSoundContext)source).getData();
        if (ctx != null)
            ctx.tick(source.id);
    }

    /**
     * Injected into SoundSource and will be invoked when a sound source is being terminated.
     *
     * @param source The sound source that is stopping
     */
    public static void stopSoundPlay(@Nonnull final SoundSource source) {
        final SourceContext ctx = ((IMixinSoundContext)source).getData();
        if (ctx != null)
            sources[source.id - 1] = null;
    }

    /**
     * Injected into SoundSource and will be invoked when a non-streaming sound data stream is attached to the
     * SoundSource.  Take the opportunity to convert the audio stream into mono format if needed.
     *
     * @param source SoundSource for which the audio buffer is being generated
     * @param buffer The buffer in question.
     */
    @Nonnull
    public static AudioStreamBuffer playBuffer(@Nonnull final SoundSource source, @Nonnull final AudioStreamBuffer buffer) {

        // If disabled return
        if (!Config.CLIENT.sound.enableMonoConversion.get())
            return buffer;

        final SourceContext ctx = ((IMixinSoundContext) source).getData();

        // If there is no context attached and conversion is enabled do it.  This can happen if enhanced sound
        // processing is turned off.  If there is a context, make sure that the sound is attenuated.
        boolean doConversion = ctx == null || (ctx.getSound() != null && ctx.getSound().getAttenuationType() != ISound.AttenuationType.NONE);

        if (doConversion)
            return Conversion.convert(buffer);

        return buffer;
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
                if (ctx != null && ctx.shouldExecute()) {
                    ctx.reinitialize();
                    pool.execute(ctx);
                }
            }
            pool.awaitQuiescence(5, TimeUnit.MINUTES);
        } catch (@Nonnull final Throwable t) {
            LOGGER.error(t, "Error in SoundContext ForkJoinPool");
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onGatherText(@Nonnull final DiagnosticEvent event) {
        if (isAvailable() && soundProcessor != null) {
            final String msg = soundProcessor.getDiagnosticString();
            if (!StringUtils.isEmpty(msg))
                event.getLeft().add(TextFormatting.GREEN + msg);
        }
    }

    /**
     * Validates that the current OpenAL state is not in error.  If in an error state an exception will be thrown.
     *
     * @param msg Optional message to be displayed along with error data
     */
    public static void validate(@Nonnull final String msg) {
        validate(() -> msg);
    }

    /**
     * Validates that the current OpenAL state is not in error.  If in an error state an exception will be thrown.
     *
     * @param err Supplier for the error message to post with exception info
     */
    public static void validate(@Nullable final Supplier<String> err) {
        final int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            String errorName = AL10.alGetString(error);
            if (StringUtils.isEmpty(errorName))
                errorName = Integer.toString(error);
            final String msg = Utilities.firstNonNull(err != null ? err.get() : null, "NONE");
            throw new IllegalStateException(String.format("OpenAL Error: %s [%s]", errorName, msg));
        }
    }
}

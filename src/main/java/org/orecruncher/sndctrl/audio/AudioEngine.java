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

package org.orecruncher.sndctrl.audio;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.audio.ChannelManager.Entry;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.sndctrl.Config;
import org.orecruncher.sndctrl.SoundControl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles the life cycle of sounds submitted to the Minecraft sound engine.
 */
@OnlyIn(Dist.CLIENT)
public final class AudioEngine {
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(AudioEngine.class);
    private static final String FMT_DBG_SOUND_SYSTEM = TextFormatting.AQUA + "SoundSystem: %d/%d";
    private static final String FMT_DBG_TRACKED = TextFormatting.AQUA + "AudioEngine: %d";
    private static final String FMT_DBG_SOUND = TextFormatting.GOLD + "%s: %d";
    private static final Reference2ObjectOpenHashMap<ISoundInstance, Consumer<ISoundInstance>> playingSounds = new Reference2ObjectOpenHashMap<>(256);
    private static final Consumer<ISoundInstance> DEFAULT_CALLBACK = s -> {
    };

    @Nonnull
    private static List<String> diagnostics = ImmutableList.of();
    @Nullable
    private static ISound playedSound = null;

    private AudioEngine() {
    }

    /**
     * Stops the specified sound if it is playing.  There may be latency between the request and when the sound play is
     * terminated.
     *
     * @param sound The sound to stop
     */
    public static void stop(@Nonnull final ISoundInstance sound) {
        Objects.requireNonNull(sound);

        final SoundState state = sound.getState();

        if (state != SoundState.STOPPING && !state.isTerminal()) {
            final ISound actualSound = getActualSound(sound);
            if (state == SoundState.DELAYED) {
                // Delayed sounds are held in a separate queue in the engine thus there is nothing to stop.
                sound.setState(SoundState.DONE);
                SoundUtils.getDelayedSounds().remove(actualSound);
            } else {
                // Tell Minecraft to stop the sound.  Termination will be detected in the client tick handler.
                sound.setState(SoundState.STOPPING);
                GameUtils.getSoundHander().stop(actualSound);
            }
        }
    }

    /**
     * Stops all playing and pending sounds. All lists and queues are dumped.
     */
    public static void stopAll() {
        LOGGER.debug("Stopping all sounds");
        GameUtils.getSoundHander().stop();
        playingSounds.keySet().forEach(s -> s.setState(SoundState.DONE));
        processTerminalSounds();
    }

    /**
     * Submits the sound to the sound system to be played.  It is possible that the sound instance returned is
     * different than the one that was passed in because another mod may have replaced the sound that is being played.
     * If this happens the actual sound that gets played will be wrapped with a proxy, and that proxy will be returned.
     * <p>
     * The status of the sound play can be obtained by checking the sound instance status.  If the sound instance is
     * in a non-terminal state the instance is being handled.
     *
     * @param sound Sound to play
     * @return Sound instance that is being tracked
     */
    public static ISoundInstance play(@Nonnull final ISoundInstance sound) {
        return play(sound, DEFAULT_CALLBACK);
    }

    /**
     * Submits the sound to the sound system to be played.  It is possible that the sound instance returned is
     * different than the one that was passed in because another mod may have replaced the sound that is being played.
     * If this happens the actual sound that gets played will be wrapped with a proxy, and that proxy will be returned.
     * <p>
     * The status of the sound play can be obtained by checking the sound instance status.  If the sound instance is
     * in a non-terminal state the instance is being handled.
     * <p>
     * If supplied with a callback, the audio engine will invoke the callback when the sound is being removed from
     * it's internal tracking list.  This happens because the sound has transitioned to a terminal state.  The callback
     * can return a value instructing whether to discard the sound (true), or to resubmit the sound (false).
     *
     * @param sound    Sound to play
     * @param callback Callback to be invoked when the sound is getting removed from the tracking list.
     * @return Sound instance that is being tracked
     */
    @Nonnull
    public static ISoundInstance play(@Nonnull final ISoundInstance sound, @Nonnull final Consumer<ISoundInstance> callback) {
        Objects.requireNonNull(sound);
        Objects.requireNonNull(callback);

        // If the sound is already queued return it's current active state.
        if (playingSounds.containsKey(sound))
            return sound;

        return playSound0(sound, callback);
    }

    /**
     * Checks the sound instance to see if it is a proxy, and returns the actual sound instance that is playing.
     *
     * @param sound The sound instance to check
     * @return The actual sound if the sound instance is a proxy; otherwise it returns self.
     */
    @Nonnull
    private static ISound getActualSound(@Nonnull final ISoundInstance sound) {
        return Utilities.safeCast(sound, IProxySound.class).map(IProxySound::getTrueSound).orElse(sound);
    }

    /**
     * Checks the sound instance to see if it is a proxy, and returns the original sound instance that was
     * requested to play.
     *
     * @param sound The sound instance to check
     * @return The original sound if the sound instance is a proxy; otherwise it returns self.
     */
    @Nonnull
    private static ISoundInstance getOriginalSound(@Nonnull final ISoundInstance sound) {
        return Utilities.safeCast(sound, IProxySound.class).map(IProxySound::getOriginalSound).orElse(sound);
    }

    @Nonnull
    private static ISoundInstance playSound0(@Nonnull ISoundInstance sound, @Nonnull final Consumer<ISoundInstance> callback) {
        // Set the initial state
        sound.setState(SoundState.QUEUING);

        if (SoundUtils.isSoundVolumeBlocked(sound)) {
            // Check if the sound even has a chance at playing.  If the calculated volume is too low the sound engine
            // will just drop it.  This check isn't perfect, but will get the majority of the cases.
            sound.setState(SoundState.BLOCKED);
        } else if (sound.isDelayed()) {
            // The sound has delay play so stick it in Minecraft's delay queue and set state for tracking
            GameUtils.getSoundHander().playDelayed(sound, sound.getPlayDelay());
            sound.setState(SoundState.DELAYED);
            playingSounds.put(sound, callback);
        } else if (SoundUtils.hasRoom()) {
            // Play the sound now
            try {
                /*
                 If the incoming sound is an IProxySound, try submitting the original sound.  It may be able to play
                 now.  It is possible that on a previous execution attempt that the sound was substituted with a
                 different sound.
                */
                final ISoundInstance original = getOriginalSound(sound);
                playedSound = null;
                GameUtils.getSoundHander().play(original);
                if (playedSound != null) {
                    // Did the sound get replaced?  If so, need to wrap in a proxy for tracking
                    if (playedSound != original) {
                        LOGGER.debug("Sound '%s' was replaced with '%s'", sound.toString(), SoundUtils.debugString(playedSound));
                        sound = new ProxySound(original, playedSound);
                    } else {
                        // It played!  Save the reference to the original sound play instead of any proxy.
                        sound = original;
                    }
                    playingSounds.put(sound, callback);
                    sound.setState(SoundState.PLAYING);
                } else {
                    /*
                     If the played sound is null it means that something decided not to have the sound play.  A mod
                     could have decided this, or for some reason the SoundEngine decided not to play because of
                     some other factors like volume, or error.
                    */
                    sound.setState(SoundState.BLOCKED);
                }
            } catch (@Nonnull final Throwable t) {
                sound.setState(SoundState.ERROR);
                LOGGER.error(t, "Unable to play sound '%s'", sound);
            } finally {
                playedSound = null;
            }
        }

        final ISoundInstance s = sound;
        LOGGER.debug(Config.Trace.SOUND_PLAY, () -> String.format("%sQUEUED: [%s]", s.getState().isActive() ? StringUtils.EMPTY : "NOT ", s));

        return sound;
    }

    /**
     * Run down our active sound list checking that they are still active. If they
     * aren't update the state accordingly.
     *
     * @param event Event that was raised
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {
        if (event.side != LogicalSide.CLIENT || event.phase != Phase.START)
            return;

        final Map<ISound, Integer> delayedSounds = SoundUtils.getDelayedSounds();
        final Map<ISound, Entry> playing = SoundUtils.getPlayingSounds();

        /*
         Process our queued sounds to make sure the state is appropriate. A sound can move between the playing sound
         list and the delayed sound list based on its attributes so we need to make sure we detect that.

         We cannot rely on isSoundPlaying(). It can return FALSE even though the sound is in the internal playing
         lists. We only want to transition if the sound is in the playing lists or not.
        */
        for (final ISoundInstance sound : playingSounds.keySet()) {
            // If an IProxySound we need to get the reference to the sound that is actually playing otherwise the
            // Minecraft sound engine will not know what we are talking about.
            final ISound aggregate = getActualSound(sound);
            final SoundState currentState = sound.getState();
            final boolean isPlaying = playing.containsKey(aggregate);

            switch (currentState) {
                case DELAYED:
                    // The sound play is delayed. Check to see if Minecraft transitioned it's state.
                    if (!delayedSounds.containsKey(aggregate)) {
                        sound.setState(isPlaying ? SoundState.PLAYING : SoundState.DONE);
                    }
                    break;
                case STOPPING:
                    if (!isPlaying) {
                        sound.setState(SoundState.DONE);
                    }
                    break;
                case PLAYING:
                    // The sound is playing. Check to see if the Minecraft sound engine transitioned to a
                    // different state.
                    if (!isPlaying) {
                        sound.setState(delayedSounds.containsKey(aggregate) ? SoundState.DELAYED : SoundState.DONE);
                    }
                    break;
                default:
                    // This should not happen, but to be safe set to a terminal state
                    LOGGER.debug(Config.Trace.SOUND_PLAY, () -> String.format("Incorrect sound state [%s]", sound));
                    sound.setState(SoundState.ERROR);
                    break;
            }
        }

        // Process any sounds in a terminal state.
        processTerminalSounds();

        // Generate diagnostics if needed.
        if (Minecraft.getInstance().gameSettings.showDebugInfo) {
            diagnostics = new ArrayList<>(16);
            diagnostics.add(String.format(FMT_DBG_SOUND_SYSTEM, SoundUtils.getTotalPlaying(), SoundUtils.getMaxSounds()));
            diagnostics.add(String.format(FMT_DBG_TRACKED, playingSounds.size()));

            playing.keySet().stream()
                    .map(s -> s.getSound().getSoundLocation())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet().stream()
                    .map(e -> String.format(FMT_DBG_SOUND, e.getKey().toString(), e.getValue()))
                    .sorted()
                    .forEach(diagnostics::add);
        } else if (diagnostics.size() > 0) {
            diagnostics = ImmutableList.of();
        }
    }

    private static void processTerminalSounds() {
        playingSounds.reference2ObjectEntrySet().removeIf(kvp -> {
            final boolean remove = kvp.getKey().getState().isTerminal();
            if (remove) {
                kvp.getValue().accept(kvp.getKey());
            }
            return remove;
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGatherText(@Nonnull final DiagnosticEvent event) {
        if (!diagnostics.isEmpty()) {
            event.getLeft().addAll(diagnostics);
        }
    }

    public static void initialize() {
        MinecraftForge.EVENT_BUS.register(AudioEngine.class);
        GameUtils.getSoundHander().addListener(new SoundListener());
    }

    /**
     * Tap into the sound play process to figure out which sound actually gets played.  Use the SoundListener rather
     * than the event method because this callback will happen later in the process when things are more diffinitive.
     * The sound engine could decide to not play a sound after the Forge event fires.
     */
    private static class SoundListener implements ISoundEventListener {

        @Override
        public void onPlaySound(@Nonnull final ISound soundIn, @Nonnull final SoundEventAccessor accessor) {
            playedSound = soundIn;

            if (!(playedSound instanceof ISoundInstance)) {
                LOGGER.debug(Config.Trace.BASIC_SOUND_PLAY, () -> String.format("PLAYING: [%s]", SoundUtils.debugString(playedSound)));
            }
        }
    }
}

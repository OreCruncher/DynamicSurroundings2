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

package org.orecruncher.sndctrl.audio;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.audio.ChannelManager.Entry;
import net.minecraft.util.math.vector.Vector3d;
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
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.sndctrl.config.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private static final ReferenceOpenHashSet<ISoundInstance> playingSounds = new ReferenceOpenHashSet<>(256);

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
            if (state == SoundState.DELAYED) {
                // Delayed sounds are held in a separate queue in the engine thus there is nothing to stop.
                sound.setState(SoundState.DONE);
                SoundUtils.getDelayedSounds().remove(sound);
            } else {
                // Tell Minecraft to stop the sound.  Termination will be detected in the client tick handler.
                sound.setState(SoundState.STOPPING);
                GameUtils.getSoundHander().stop(sound);
            }
        }
    }

    /**
     * Stops all playing and pending sounds. All lists and queues are dumped.
     */
    public static void stopAll() {
        LOGGER.debug("Stopping all sounds");
        GameUtils.getSoundHander().stop();
        playingSounds.forEach(s -> s.setState(SoundState.DONE));
        processTerminalSounds();
    }

    /**
     * Submits the sound to the sound system to be played.
     * <p>
     * The status of the sound play can be obtained by checking the sound instance status.  If the sound instance is
     * in a non-terminal state the instance is being handled.
     *
     * @param sound Sound to play
     */
    public static void play(@Nonnull final ISoundInstance sound) {
        Objects.requireNonNull(sound);
        // If the sound is already queued return it's current active state.
        if (!playingSounds.contains(sound)) {
            playSound0(sound);
        }
    }

    private static void playSound0(@Nonnull ISoundInstance sound) {
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
            playingSounds.add(sound);
        } else if (SoundUtils.hasRoom()) {
            // Play the sound now
            try {
                playedSound = null;
                GameUtils.getSoundHander().play(sound);
                if (playedSound != null) {
                    // Did the sound get replaced?
                    if (playedSound != sound) {
                        // It got replaced.  Nothing that can be done other than reporting back up to the caller.
                        LOGGER.debug("Sound '%s' was replaced with '%s'", sound.toString(), SoundUtils.debugString(playedSound));
                        sound.setState(SoundState.REPLACED);
                    } else {
                        // It played!  Save the reference to the sound play for tracking.
                        playingSounds.add(sound);
                        sound.setState(SoundState.PLAYING);
                    }
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

        LOGGER.debug(Config.Trace.SOUND_PLAY, () -> {
            final double distance;
            if (GameUtils.getPlayer() != null) {
                final Vector3d location = new Vector3d(sound.getX(), sound.getY(), sound.getZ());
                distance = Math.sqrt(GameUtils.getPlayer().getDistanceSq(location));
            } else {
                distance = 0;
            }
            return String.format("%sQUEUED: [%s] (distance: %f)", sound.getState().isActive() ? StringUtils.EMPTY : "NOT ", sound, distance);
        });
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
        for (final ISoundInstance sound : playingSounds) {
            final SoundState currentState = sound.getState();
            final boolean isPlaying = playing.containsKey(sound);

            switch (currentState) {
                case DELAYED:
                    // The sound play is delayed. Check to see if Minecraft transitioned it's state.
                    if (!delayedSounds.containsKey(sound)) {
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
                        sound.setState(delayedSounds.containsKey(sound) ? SoundState.DELAYED : SoundState.DONE);
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
        if (processDiagnostics() && Minecraft.getInstance().gameSettings.showDebugInfo) {
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
        playingSounds.removeIf(s -> s.getState().isTerminal());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGatherText(@Nonnull final DiagnosticEvent event) {
        if (processDiagnostics() && !diagnostics.isEmpty()) {
            event.getLeft().addAll(diagnostics);
        }
    }

    private static boolean processDiagnostics() {
        return Config.CLIENT.logging.get_enableLogging();
    }

    public static void initialize() {
        MinecraftForge.EVENT_BUS.register(AudioEngine.class);
    }

    /**
     * Hook that is called when the sound is actually being queued down into the engine.  Use this to determine
     * what actually got played and to perform logging.  The standard sound listener will not receive callbacks if
     * the sound is too far away (based on the sound instance distance value).
     * @param sound Sound that is being queued into the audio engine
     */
    public static void onPlaySound(@Nonnull final ISound sound) {
        playedSound = sound;
        if (!(playedSound instanceof ISoundInstance)) {
            LOGGER.debug(Config.Trace.BASIC_SOUND_PLAY, () -> String.format("PLAYING: [%s]", SoundUtils.debugString(playedSound)));
        }
    }
}

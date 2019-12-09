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

import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.audio.ISoundInstance;
import org.orecruncher.sndctrl.events.AudioEvent.MusicFadeAudioEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This guy is responsible for manipulating the volume state of the Music
 * SoundCategory. This is so normal playing music can fade in/out whenever
 * BattleMusic is being played, or when a player hits the "play" button in the
 * sound configuration.
 */
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MusicFader {
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(MusicFader.class);
    private static final float MIN_VOLUME_SCALE = 0.001F;
    private static final float FADE_AMOUNT = 0.02F;
    private static float currentScale = 1.0F;
    @Nullable
    private static ISoundInstance playingConfigSound;

    private MusicFader() {
    }

    public static float getMusicScaling() {
        return currentScale;
    }

    @SubscribeEvent
    public static void onTick(@Nonnull final TickEvent.ClientTickEvent event) {

        if (event.side != LogicalSide.CLIENT || event.phase == Phase.END)
            return;

        final float oldScale = currentScale;

        if (playingConfigSound != null) {
            if (playingConfigSound.getState().isTerminal())
                stopConfigSound(playingConfigSound);
        }

        if (playingConfigSound == null) {
            final MusicFadeAudioEvent doFade = new MusicFadeAudioEvent();
            MinecraftForge.EVENT_BUS.post(doFade);
            if (doFade.isCanceled())
                currentScale -= FADE_AMOUNT;
            else
                currentScale += FADE_AMOUNT;
        }

        // Make sure it is properly bounded
        currentScale = MathStuff.clamp(currentScale, MIN_VOLUME_SCALE, 1.0F);

        // If there is a change in scale tell the sound handler. Just by tickling the
        // the value it triggers all sounds that are currently playing that are in the
        // MUSIC category to be re-evaluated, and the getClampedVolume() override will
        // scale accordingly.
        if (Float.compare(oldScale, currentScale) != 0) {
            final float mcScale = GameUtils.getGameSettings().getSoundLevel(SoundCategory.MUSIC);
            GameUtils.getSoundHander().setSoundLevel(SoundCategory.MUSIC, mcScale);
        }
    }

    /**
     * Used by the configuration system to short circuit the playing music in order
     * to play a sound sample.
     *
     * @param sound The configuration sound instance to play
     */
    public static void playConfigSound(@Nonnull final ISoundInstance sound) {
        playingConfigSound = Objects.requireNonNull(sound);
        currentScale = MIN_VOLUME_SCALE;
        AudioEngine.stopAll();
        AudioEngine.play(sound);
    }

    /**
     * Used by the configuration system to stop playing a configure sound. Sounds
     * that were muted prior will fade back in.
     *
     * @param sound The configuration sound instance to stop
     */
    public static void stopConfigSound(@Nonnull final ISoundInstance sound) {
        if (playingConfigSound != null) {
            if (playingConfigSound != Objects.requireNonNull(sound))
                LOGGER.warn("Inconsistent sound in MusicFader");
            AudioEngine.stop(playingConfigSound);
            playingConfigSound = null;
        }
    }

    /**
     * Does an identity compare of the specified sound instance to the playing sound
     * to see if they are the same instance.
     *
     * @param sound Sound instance to check
     * @return true if the sound is the currently play configuration sound, false otherwise
     */
    public static boolean isConfigSoundInstance(@Nonnull final ISound sound) {
        return playingConfigSound == Objects.requireNonNull(sound);
    }

}

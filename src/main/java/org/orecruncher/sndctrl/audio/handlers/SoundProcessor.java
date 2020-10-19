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

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.sndctrl.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.SoundUtils;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SoundProcessor {
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundProcessor.class);
    private static final float MIN_SOUNDFACTOR = 0F;
    private static final float MAX_SOUNDFACTOR = 4F;
    private static final float DEFAULT_SOUNDFACTOR = 1F;
    private static final Set<ResourceLocation> blockedSounds = new ObjectOpenHashSet<>(32);
    private static final Object2LongOpenHashMap<ResourceLocation> soundCull = new Object2LongOpenHashMap<>(32);
    private static final Object2FloatOpenHashMap<ResourceLocation> volumeControl = new Object2FloatOpenHashMap<>(32);
    private static int cullInterval = 20;

    static {
        volumeControl.defaultReturnValue(DEFAULT_SOUNDFACTOR);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, SoundProcessor::soundPlay);
    }

    private SoundProcessor() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onConfigLoad(@Nonnull final ModConfig.Loading configEvent) {
        applyConfig();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onConfigChange(@Nonnull final ModConfig.Reloading configEvent) {
        applyConfig();
    }

    private static void applyConfig() {
        soundCull.clear();
        blockedSounds.clear();
        volumeControl.clear();

        cullInterval = Config.CLIENT.sound.get_cullInterval();

        for (final String line : Config.CLIENT.sound.get_individualSounds()) {
            final String[] parts = line.split(" ");
            if (parts.length < 2) {
                LOGGER.warn("Missing tokens in sound settings? (%s)", line);
            } else {
                final ResourceLocation res = new ResourceLocation(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    if ("cull".compareToIgnoreCase(parts[i]) == 0) {
                        soundCull.put(res, -cullInterval);
                    } else if ("block".compareToIgnoreCase(parts[i]) == 0) {
                        blockedSounds.add(res);
                    } else {
                        try {
                            final int volume = Integer.parseInt(parts[i]);
                            volumeControl.put(res, MathStuff.clamp(volume / 100F, MIN_SOUNDFACTOR, MAX_SOUNDFACTOR));
                        } catch (final Throwable t) {
                            LOGGER.warn("Unrecognized token '%s' (%s)", parts[i], line);
                        }
                    }
                }
            }
        }
    }

    public static boolean isSoundBlocked(@Nonnull final ResourceLocation sound) {
        return blockedSounds.contains(Objects.requireNonNull(sound));
    }

    public static boolean isSoundCulled(@Nonnull final ResourceLocation sound) {
        return soundCull.containsKey(Objects.requireNonNull(sound));
    }

    public static float getVolumeScale(@Nonnull final ResourceLocation sound) {
        return volumeControl.getFloat(Objects.requireNonNull(sound));
    }

    public static float getVolumeScale(@Nonnull final ISound sound) {
        return getVolumeScale(Objects.requireNonNull(sound).getSoundLocation());
    }

    private static boolean isSoundCulledLogical(@Nonnull final ResourceLocation sound) {
        if (cullInterval > 0) {
            // Get the last time the sound was seen
            final long lastOccurance = soundCull.getLong(Objects.requireNonNull(sound));
            if (lastOccurance != 0) {
                final long currentTick = TickCounter.getTickCount();
                if ((currentTick - lastOccurance) < cullInterval) {
                    return true;
                } else {
                    // Set when it happened and fall through for remapping and stuff
                    soundCull.put(sound, currentTick);
                }
            }
        }
        return false;
    }

    private static boolean blockSoundProcess(@Nonnull final ResourceLocation res) {
        return isSoundBlocked(res) || isSoundCulledLogical(res);
    }

    // Event handler for sound plays - hooked in static class initializer
    private static void soundPlay(@Nonnull final PlaySoundEvent e) {
        // If there is no sound assigned, or if there is no more room in the play lists kill it
        final ISound theSound = e.getSound();
        if (theSound == null || !SoundUtils.hasRoom()) {
            e.setResultSound(null);
            return;
        }

        // Don't mess with our config sound instances from the config menu
        if (MusicFader.isConfigSoundInstance(theSound))
            return;

        // Check to see if we need to block sound processing
        final ResourceLocation soundResource = theSound.getSoundLocation();
        if (blockSoundProcess(soundResource)) {
            e.setResultSound(null);
        }
    }

}

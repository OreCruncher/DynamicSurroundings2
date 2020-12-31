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

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
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
import org.orecruncher.sndctrl.config.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.SoundUtils;
import org.orecruncher.sndctrl.library.SoundLibrary;

import javax.annotation.Nonnull;
import java.util.*;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SoundProcessor {
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundProcessor.class);

    private static final float MIN_SOUNDFACTOR = 0F;
    private static final float MAX_SOUNDFACTOR = 4F;
    private static final float DEFAULT_SOUNDFACTOR = 1F;
    private static final String BLOCK_TOKEN = "block";
    private static final String CULL_TOKEN = "cull";

    private static final Set<ResourceLocation> blockedSounds = new ObjectOpenHashSet<>(32);
    private static final Object2LongOpenHashMap<ResourceLocation> soundCull = new Object2LongOpenHashMap<>(32);
    private static final Object2FloatOpenHashMap<ResourceLocation> volumeControl = new Object2FloatOpenHashMap<>(32);
    private static int cullInterval = 20;

    public static final class IndividualSoundConfig {
        private final ResourceLocation location;
        private boolean isBocked;
        private boolean isCulled;
        private int volumeScale;

        public IndividualSoundConfig(@Nonnull final ResourceLocation location) {
            this(location, false, false, 100);
        }

        public IndividualSoundConfig(@Nonnull final ResourceLocation location, final boolean isBlocked, final boolean isCulled, final int volumeScale) {
            this.location = location;
            this.isBocked = isBlocked;
            this.isCulled = isCulled;
            this.volumeScale = MathStuff.clamp(volumeScale, 0, 400);
        }

        public IndividualSoundConfig(@Nonnull final IndividualSoundConfig source) {
            this.location = source.location;
            this.isBocked = source.isBocked;
            this.isCulled = source.isCulled;
            this.volumeScale = source.volumeScale;
        }

        @Nonnull
        public ResourceLocation getLocation() {
            return this.location;
        }

        public boolean isDefault() {
            return !(this.isBocked || this.isCulled || volumeScale != 100);
        }

        public boolean isBocked() {
            return this.isBocked;
        }

        public void setIsBlocked(final boolean flag) {
            this.isBocked = flag;
        }

        public boolean isCulled() {
            return this.isCulled;
        }

        public void setIsCulled(final boolean flag) {
            this.isCulled = flag;
        }

        public float getVolumeScale() {
            return this.volumeScale;
        }

        public void setVolumeScale(final int scale) {
            this.volumeScale = MathStuff.clamp(scale, 0, 400);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(this.location.toString());
            if (this.isBocked)
                builder.append(" ").append(BLOCK_TOKEN);
            if (this.isCulled)
                builder.append(" ").append(CULL_TOKEN);
            if (this.volumeScale != 100)
                builder.append(" ").append(this.volumeScale);
            return builder.toString();
        }
    }

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
                    if (CULL_TOKEN.compareToIgnoreCase(parts[i]) == 0) {
                        soundCull.put(res, -cullInterval);
                    } else if (BLOCK_TOKEN.compareToIgnoreCase(parts[i]) == 0) {
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

    @Nonnull
    public static Collection<IndividualSoundConfig> getSortedSoundConfigurations() {
        final SortedMap<ResourceLocation, IndividualSoundConfig> map = new TreeMap<>();

        // Get a list of all the sounds and synthesize entry data, filling in blanks
        for (final Map.Entry<ResourceLocation, SoundEvent> kvp : SoundLibrary.getRegisteredSounds().entrySet()) {
            final boolean isBlocked = blockedSounds.contains(kvp.getKey());
            final boolean isCulled = soundCull.containsKey(kvp.getKey());
            final int volumeScale = (int) (volumeControl.getFloat(kvp.getKey()) * 100);
            map.put(kvp.getKey(), new IndividualSoundConfig(kvp.getKey(),isBlocked, isCulled, volumeScale));
        }

        return map.values();
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

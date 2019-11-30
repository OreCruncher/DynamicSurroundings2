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

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ChannelManager;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Listener;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.Utilities;
import org.orecruncher.sndctrl.misc.ModEnvironment;
import org.orecruncher.sndctrl.mixins.ISoundEngineMixin;
import org.orecruncher.sndctrl.mixins.ISoundHandlerMixin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * Helper class that obtains information about the current sound processing environment
 * from sources that are not directly obtainable.
 */
@OnlyIn(Dist.CLIENT)
public final class SoundUtils {
    private static final int MAX_SOUNDS = 255;
    private static final Map<String, SoundCategory> categoryMapper;

    // Since the pieces of the Minecraft sound system are effectively singletons, cache these values for reuse.
    private static final Map<ISound, ChannelManager.Entry> playing;
    private static final Map<ISound, Integer> delayed;
    private static final Listener listener;

    static {
        categoryMapper = new Object2ObjectOpenHashMap<>();
        for (final SoundCategory sc : SoundCategory.values())
            categoryMapper.put(sc.getName(), sc);

        final SoundEngine engine = Utilities.safeCast(Minecraft.getInstance().getSoundHandler(), ISoundHandlerMixin.class).get().getSoundEngine();
        final ISoundEngineMixin mixinEngine = Utilities.safeCast(engine, ISoundEngineMixin.class).get();
        playing = mixinEngine.getPlayingSounds();
        delayed = mixinEngine.getDelayedSounds();
        listener = mixinEngine.getListener();
    }

    private SoundUtils() {
    }

    static boolean hasRoom() {
        return getTotalPlaying() < MAX_SOUNDS;
    }

    static int getMaxSounds() {
        return MAX_SOUNDS;
    }

    static int getTotalPlaying() {
        return playing.size() + delayed.size();
    }

    @Nonnull
    public static Listener getListener() {
        return listener;
    }

    @Nonnull
    static Map<ISound, ChannelManager.Entry> getPlayingSounds() {
        return playing;
    }

    @Nonnull
    static Map<ISound, Integer> getDelayedSounds() {
        return delayed;
    }

    @Nullable
    static SoundCategory getSoundCategory(@Nonnull final String name) {
        return categoryMapper.get(Objects.requireNonNull(name));
    }

    public static float getMasterGain() {
        return listener.getGain();
    }

    @Nonnull
    public static ISound.AttenuationType noAttenuation() {
        return ModEnvironment.SoundPhysics.isLoaded() ? ISound.AttenuationType.LINEAR : ISound.AttenuationType.NONE;
    }

    /**
     * Checks to see if a sound will be blocked from playing because the volume would be too low to hear.  This is
     * a best guess based on coarse information available.
     *
     * @param sound Sound instance to check for a volume block
     * @return true of the sound would be volume blocked; false otherwise
     */
    public static boolean isSoundVolumeBlocked(@Nonnull final ISound sound) {
        Objects.requireNonNull(sound);
        return getMasterGain() <= 0F || (!sound.canBeSilent() && sound.getVolume() <= 0F);
    }

    /**
     * Provides a debug string for the specified sound object.
     *
     * @param sound Sound instance to provide a debug string for
     * @return Debug string
     */
    @Nonnull
    public static String debugString(@Nonnull ISound sound) {
        Objects.requireNonNull(sound);

        if (sound instanceof ISoundInstance)
            return sound.toString();

        //@formatter:off
        MoreObjects.ToStringHelper t = MoreObjects.toStringHelper(sound)
                .addValue(sound.getSoundLocation().toString())
                .addValue(sound.getCategory().toString())
                .addValue(sound.getAttenuationType().toString());

        if (sound.getSound() != null) {
            t.add("v", sound.getVolume())
                    .add("p", sound.getPitch());
        }

        return t.add("x", sound.getX())
                .add("y", sound.getY())
                .add("z", sound.getZ())
                .toString();
        //@formatter:on
    }

}

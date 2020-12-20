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
import net.minecraft.client.audio.*;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.openal.*;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.sndctrl.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;
import org.orecruncher.sndctrl.audio.handlers.SoundFXProcessor;

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
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundUtils.class);

    private static int MAX_SOUNDS = 0;
    private static int SOUND_LIMIT = 255; // 0;
    private static final Map<String, SoundCategory> categoryMapper;

    // Since the pieces of the Minecraft sound system are effectively singletons, cache these values for reuse.
    private static final Map<ISound, ChannelManager.Entry> playing;
    private static final Map<ISound, Integer> delayed;
    private static final Listener listener;

    static {
        categoryMapper = new Object2ObjectOpenHashMap<>();
        for (final SoundCategory sc : SoundCategory.values())
            categoryMapper.put(sc.getName(), sc);

        final SoundEngine engine = GameUtils.getSoundHander().sndManager;
        playing = engine.playingSoundsChannel;
        delayed = engine.delayedSounds;
        listener = engine.listener;
    }

    private SoundUtils() {
    }

    public static int getMaxSounds() {
        return MAX_SOUNDS;
    }

    public static boolean hasRoom() {
        return getTotalPlaying() < SOUND_LIMIT;
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
    public static SoundCategory getSoundCategory(@Nonnull final String name) {
        return categoryMapper.get(Objects.requireNonNull(name));
    }

    public static float getMasterGain() {
        return listener.getGain();
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
     * Determines if a sound is in range of a listener based on the sounds attenuation distance.
     *
     * @param listener Location of the listener
     * @param sound The sound that is to be evaluated
     * @param pad Additional distance to add when evaluating
     * @return true if the sound is within the attenuation distance; false otherwise
     */
    public static boolean inRange(@Nonnull final Vector3d listener, @Nonnull final ISound sound, final int pad) {
        int distSq = sound.getSound().getAttenuationDistance() + pad;
        distSq *= distSq;
        return listener.squareDistanceTo(sound.getX(), sound.getY(), sound.getZ()) <= distSq;
    }

    public static boolean inRange(@Nonnull final Vector3d listener, @Nonnull final ISound sound) {
        return inRange(listener, sound, 0);
    }

    /**
     * Provides a debug string for the specified sound object.
     *
     * @param sound Sound instance to provide a debug string for
     * @return Debug string
     */
    @Nonnull
    public static String debugString(@Nullable final ISound sound) {

        if (sound == null)
            return "null";

        if (sound instanceof ISoundInstance)
            return sound.toString();

        //@formatter:off
        return MoreObjects.toStringHelper(sound)
                .addValue(sound.getSoundLocation().toString())
                .addValue(sound.getCategory().toString())
                .addValue(sound.getAttenuationType().toString())
                .add("v", sound.getVolume())
                .add("p", sound.getPitch())
                .add("x", sound.getX())
                .add("y", sound.getY())
                .add("z", sound.getZ())
                .add("distance", sound.getSound().getAttenuationDistance())
                .add("streaming", sound.getSound().isStreaming())
                .add("global", sound.isGlobal())
                .toString();
        //@formatter:on
    }

    /**
     * This method is invoked via the MixinSoundSystem injection.  It will be called when the sound system
     * is intialized, and it gives an opportunity to setup special effects processing.
     *
     * @param soundSystem The sound system instance being initialized
     */
    public static void initialize(@Nonnull final SoundSystem soundSystem) {

        try {

            final long device = soundSystem.device;

            boolean hasFX = false;
            if (Config.CLIENT.sound.get_enableEnhancedSounds()) {
                LOGGER.info("Enhanced sounds are enabled.  Will perform sound engine reconfiguration.");
                final ALCCapabilities deviceCaps = ALC.createCapabilities(device);
                hasFX = deviceCaps.ALC_EXT_EFX;

                if (!hasFX) {
                    LOGGER.warn("EFX audio extensions not available for the current sound device!");
                } else {
                    // Using 4 aux slots instead of the default 2
                    final int[] attribs = new int[]{EXTEfx.ALC_MAX_AUXILIARY_SENDS, 4, 0};
                    final long ctx = ALC10.alcCreateContext(device, attribs);
                    ALC10.alcMakeContextCurrent(ctx);

                    // Have to renable since we reset the context
                    AL10.alEnable(EXTSourceDistanceModel.AL_SOURCE_DISTANCE_MODEL);
                }
            } else {
                LOGGER.warn("Enhanced sounds are not enabled.  No fancy sounds for you!");
            }

            // Calculate the number of source slots available
            MAX_SOUNDS = ALC11.alcGetInteger(device, ALC11.ALC_MONO_SOURCES);
            SOUND_LIMIT = MAX_SOUNDS - 10;

            // Do this last because it is dependent on the sound calculations
            if (hasFX)
                SoundFXProcessor.initialize();

        } catch (@Nonnull final Throwable t) {
            LOGGER.warn(t.getMessage());
            LOGGER.warn("OpenAL special effects for sounds will not be available");
        }

    }

    public static void deinitialize(@Nonnull final SoundSystem soundSystem) {
        SoundFXProcessor.deinitialize();
    }

}

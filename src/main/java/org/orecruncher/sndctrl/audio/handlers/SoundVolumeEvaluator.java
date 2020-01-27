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

import com.google.common.base.Preconditions;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

/**
 * Handler that calculates an effective volume for a given sound based on
 * various factors, including any user configured sound scaling.
 */
@OnlyIn(Dist.CLIENT)
public final class SoundVolumeEvaluator {

    // Callbacks from other mods where volume can be scaled.  Goal is to get mods to use this callback rather than
    // replacing the sound during sound play.
    private static final ObjectArray<Function<ISound, Float>> volumeScaleCallbacks = new ObjectArray<>();

    private SoundVolumeEvaluator() {
    }

    public static void register(@Nonnull final Function<ISound, Float> callback) {
        volumeScaleCallbacks.add(callback);
    }

    private static float getVolumeScaleFromMods(@Nonnull final ISound sound) {
        float result = 1F; //SoundProcessor.getVolumeScale(sound);
        for (final Function<ISound, Float> callback : volumeScaleCallbacks) {
            try {
                result = MathStuff.min(result, callback.apply(sound));
                if (result == 0F)
                    break;
            } catch (@Nonnull final Throwable ignore) {
            }
        }

        return result;
    }


    private static float getCategoryVolumeScale(@Nonnull final ISound sound) {
        final Optional<ISoundInstance> si = Utilities.safeCast(sound, ISoundInstance.class);
        if (si.isPresent()) {
            final ISoundCategory sc = si.get().getSoundCategory();
            return sc == Category.MASTER ? 1F : sc.getVolumeScale();
        }

        // Master category already controlled by master gain so ignore
        final SoundCategory category = sound.getCategory();
        return category == SoundCategory.MASTER ? 1F : GameUtils.getGameSettings().getSoundLevel(category);
    }

    /**
     * This guy is hooked by a Mixin to replace getClampedVolume() in Minecraft code.
     */
    public static float getClampedVolume(@Nonnull final ISound sound) {
        Preconditions.checkNotNull(sound);
        float volume = getVolumeScaleFromMods(sound)
                * getCategoryVolumeScale(sound)
                * sound.getVolume();
        return MathStuff.clamp1(volume);
    }

}

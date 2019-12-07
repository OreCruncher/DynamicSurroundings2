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
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.sndctrl.audio.ISoundInstance;

import javax.annotation.Nonnull;

/**
 * Handler that calculates an effective volume for a given sound based on
 * various factors, including any user configured sound scaling.
 */
@OnlyIn(Dist.CLIENT)
public final class SoundVolumeEvaluator {
    private SoundVolumeEvaluator() {
    }

    private static float getCategoryVolumeScale(@Nonnull final SoundCategory category) {
        // Master category already controlled by master gain so ignore
        if (category != SoundCategory.MASTER)
            return Minecraft.getInstance().gameSettings.getSoundLevel(category);
        return 1F;
    }

    private static boolean canMute(@Nonnull final ISound sound) {
        return Utilities.safeCast(sound, ISoundInstance.class)
                .map(ISoundInstance::canMute)
                .orElse(sound.getCategory() == SoundCategory.MUSIC);
    }

    private static float getVolumeScale(@Nonnull final ISound sound) {
        final float vol = SoundProcessor.getVolumeScale(sound);
        return canMute(sound) ? vol * MusicFader.getMusicScaling() : vol;
    }

    /**
     * This guy is hooked by a Mixin to replace getClampedVolume() in Minecraft
     * code.
     */
    public static float getClampedVolume(@Nonnull final ISound sound) {
        Preconditions.checkNotNull(sound);
        final float volumeScale = getVolumeScale(sound);
        final float volume = sound.getVolume() * getCategoryVolumeScale(sound.getCategory()) * volumeScale;
        return MathStuff.clamp1(volume);
    }

}

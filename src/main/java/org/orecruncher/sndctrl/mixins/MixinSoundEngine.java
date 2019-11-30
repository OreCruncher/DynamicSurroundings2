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

package org.orecruncher.sndctrl.mixins;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEngine;
import org.orecruncher.sndctrl.audio.handlers.SoundVolumeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {

    /**
     * Calcluates the volume of sound based on the myriad of factors in the game as
     * well as configuration.
     *
     * @param sound The sound instance that is being evaluated
     * @param ci    The callback context used to return the result of the calculation
     */
    @Inject(method = "getClampedVolume(Lnet/minecraft/client/audio/ISound;)F", at = @At("HEAD"), cancellable = true)
    private void getClampedVolume(ISound sound, CallbackInfoReturnable<Float> ci) {
        try {
            ci.setReturnValue(SoundVolumeEvaluator.getClampedVolume(sound));
        } catch (final Throwable ignored) {
        }
    }
}

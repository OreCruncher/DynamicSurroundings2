/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2020 OreCruncher
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

import net.minecraft.client.audio.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import org.orecruncher.sndctrl.audio.SoundUtils;
import org.orecruncher.sndctrl.audio.handlers.SoundFXProcessor;
import org.orecruncher.sndctrl.audio.handlers.SoundVolumeEvaluator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {

    @Final
    @Shadow
    private SoundSystem sndSystem;

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

    /**
     * Callback hook to initialize sndcntrl when the SoundEngine initializes.
     * @param ci
     */
    @Inject(method = "load()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundSystem;init()V", shift = At.Shift.AFTER))
    public void initialize(CallbackInfo ci)
    {
        SoundUtils.initialize(this.sndSystem);
    }

    /**
     * Callback hook to deinitialize sndctrl when the SoundEngine is unloaded.
     * @param ci
     */
    @Inject(method = "unload()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundSystem;unload()V", shift = At.Shift.BEFORE))
    public void deinitialize(CallbackInfo ci)
    {
        SoundUtils.deinitialize(this.sndSystem);
    }

    /**
     * Callback will trigger creation of sound context information for the sound play once it has been queued to the
     * sound engine.  It will also perform the first calculations of sound effects based on the player environment.
     * @param p_sound The sound that is being played
     * @param ci Ignored
     * @param soundeventaccessor Ignored
     * @param resourcelocation Ignored
     * @param sound Ignored
     * @param f Ignored
     * @param f1 Ignored
     * @param soundcategory Category of the sound that is being played
     * @param f2 Ignored
     * @param f3 Ignored
     * @param attenuationtype Ignored
     * @param flag Ignored
     * @param vector3d Ignored
     * @param flag2 Ignored
     * @param flag3 Ignored
     * @param completablefuture Ignored
     * @param entry The ChannelManager entry that is being queued to the SoundEngine for off thread processing.
     */
    @Inject(method = "play(Lnet/minecraft/client/audio/ISound;)V", at = @At(value = "INVOKE", target ="Lnet/minecraft/client/audio/ChannelManager$Entry;runOnSoundExecutor(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void onSoundPlay(ISound p_sound, CallbackInfo ci, SoundEventAccessor soundeventaccessor, ResourceLocation resourcelocation, Sound sound, float f, float f1, SoundCategory soundcategory, float f2, float f3, ISound.AttenuationType attenuationtype, boolean flag, Vector3d vector3d, boolean flag2, boolean flag3, CompletableFuture completablefuture, ChannelManager.Entry entry)    {
        SoundFXProcessor.onSoundPlay(p_sound, soundcategory, entry);
    }
}

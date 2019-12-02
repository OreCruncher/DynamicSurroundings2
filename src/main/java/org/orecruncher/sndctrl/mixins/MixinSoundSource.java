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

import org.orecruncher.sndctrl.audio.handlers.SoundFXProcessor;
import org.orecruncher.sndctrl.audio.handlers.SourceContext;
import org.orecruncher.sndctrl.xface.ISoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(net.minecraft.client.audio.SoundSource.class)
public abstract class MixinSoundSource implements ISoundSource {

    @Nonnull
    private final SourceContext ctx = new SourceContext();
    // ID of the playing source
    @Shadow
    @Final
    private int field_216441_b;

    /**
     * Gets the source ID of the SoundSource
     *
     * @return ID of the SoundSource
     */
    @Override
    public int getSourceId() {
        return this.field_216441_b;
    }

    @Override
    @Nonnull
    public SourceContext getSourceContext() {
        return this.ctx;
    }

    /**
     * Invoked when the sound is actually triggered for playing.  We want to make sure out filters are applied before
     * the intial play.
     *
     * @param ignored Callback info object that is supplied by the Mixin framework; ignored
     */
    @Inject(method = "func_216438_c()V", at = @At("HEAD"))
    public void playTrigger(@Nonnull final CallbackInfo ignored) {
        if (SoundFXProcessor.isAvailable())
            this.ctx.tick(this.field_216441_b);
    }

    /**
     * The parent method is invoked by the channel manager during normal tick processing.  We hook that routine
     * and call the handler to do any special effect processing.
     *
     * @param ignored Callback info object that is supplied by the Mixin framework; ignored
     */
    @Inject(method = "func_216434_i()V", at = @At("TAIL"))
    public void tick(@Nonnull final CallbackInfo ignored) {
        if (SoundFXProcessor.isAvailable())
            this.ctx.tick(this.field_216441_b);
    }

    /**
     * Called by the SoundSystem when the source is going out of scope.
     *
     * @param ignored Callback info object that is supplied by the Mixin framework; ignored
     */
    @Inject(method = "func_216436_b()V", at = @At(value = "RETURN"))
    public void stop(@Nonnull final CallbackInfo ignored) {
        if (SoundFXProcessor.isAvailable())
            SoundFXProcessor.stopSoundPlay(this.field_216441_b);
    }
}

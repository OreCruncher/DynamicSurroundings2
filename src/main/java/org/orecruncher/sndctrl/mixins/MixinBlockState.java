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

import net.minecraft.block.BlockState;
import org.orecruncher.sndctrl.audio.EffectRegistry;
import org.orecruncher.sndctrl.xface.IBlockStateEffects;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public class MixinBlockState implements IBlockStateEffects {

    private float occlusion = -1;
    private float reflectivity = -1F;
    private float lowFreq = -1F;
    private float midFreq = -1F;
    private float highFreq = -1;

    @Override
    public float getOcclusion() {
        if (this.occlusion < 0)
            this.occlusion = EffectRegistry.getOcclusion((BlockState) ((Object) this));
        return this.occlusion;
    }

    @Override
    public float getReflectivity() {
        if (this.reflectivity < 0)
            this.reflectivity = EffectRegistry.getReflectivity((BlockState) ((Object) this));
        return this.reflectivity;
    }

    @Override
    public float getLowFrequencyReflect() {
        if (this.lowFreq < 0) {
            final float r = getReflectivity();
            this.lowFreq = r >= 1F ? 0 : (1F - r);
        }
        return lowFreq;
    }

    @Override
    public float getMidFrequencyReflect() {
        if (this.midFreq < 0) {
            final float r = getReflectivity();
            this.midFreq = r >= 2F ? 0 : (1F - Math.abs(r - 1F));
        }
        return this.midFreq;
    }

    @Override
    public float getHighFrequencyReflect() {
        if (this.highFreq < 0) {
            final float r = getReflectivity();
            this.highFreq = r <= 1F ? 0 : r - 1F;
        }
        return this.highFreq;
    }
}

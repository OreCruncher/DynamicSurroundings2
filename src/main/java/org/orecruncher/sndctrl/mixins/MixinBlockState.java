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
import org.orecruncher.sndctrl.library.AudioEffectLibrary;
import org.orecruncher.sndctrl.xface.IBlockStateEffects;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public class MixinBlockState implements IBlockStateEffects {

    private float occlusion = -1;
    private float reflectivity = -1F;

    @Override
    public float getOcclusion() {
        if (this.occlusion < 0)
            this.occlusion = AudioEffectLibrary.getOcclusion((BlockState) ((Object) this));
        return this.occlusion;
    }

    @Override
    public float getReflectivity() {
        if (this.reflectivity < 0)
            this.reflectivity = AudioEffectLibrary.getReflectivity((BlockState) ((Object) this));
        return this.reflectivity;
    }
}

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

import net.minecraft.block.BlockState;
import org.orecruncher.lib.IDataAccessor;
import org.orecruncher.sndctrl.library.AudioEffectLibrary;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

/**
 * Simple mixin that adds a context field to BlockState where effect information for the block can be stored.
 */
@Mixin(BlockState.class)
public class MixinBlockState implements IDataAccessor<AudioEffectLibrary.EffectData> {

    private AudioEffectLibrary.EffectData sndctrl_data = null;

    @Override
    @Nullable
    public AudioEffectLibrary.EffectData getData() {
        return this.sndctrl_data;
    }

    @Override
    public void setData(@Nullable AudioEffectLibrary.EffectData data) {
        this.sndctrl_data = data;
    }
}

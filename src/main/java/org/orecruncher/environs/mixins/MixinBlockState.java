/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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
package org.orecruncher.environs.mixins;

import net.minecraft.block.BlockState;
import org.orecruncher.environs.library.BlockStateData;
import org.orecruncher.environs.misc.IMixinBlockData;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(BlockState.class)
public class MixinBlockState implements IMixinBlockData {

    private BlockStateData environs_blockData;

    @Nullable
    @Override
    public BlockStateData getBlockData() {
        return this.environs_blockData;
    }

    @Override
    public void setBlockData(@Nullable final BlockStateData data) {
        this.environs_blockData = data;
    }
}

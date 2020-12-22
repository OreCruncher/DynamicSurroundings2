/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.library;

import net.minecraft.block.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.misc.IMixinBlockData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public final class BlockStateUtil {
    private BlockStateUtil() {
    }

    @Nonnull
    public static BlockStateData getData(@Nonnull final BlockState state) {
        BlockStateData profile = ((IMixinBlockData) state).getBlockData();
        if (profile == null) {
            profile = BlockStateLibrary.get(state);
            ((IMixinBlockData) state).setBlockData(profile);
        }
        return profile;
    }

    public static void setData(@Nonnull final BlockState state, @Nullable final BlockStateData data) {
        ((IMixinBlockData) state).setBlockData(data);
    }

}

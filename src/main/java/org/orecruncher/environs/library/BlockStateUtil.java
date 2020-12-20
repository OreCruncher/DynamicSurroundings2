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
import org.orecruncher.lib.reflection.ObjectField;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public final class BlockStateUtil {
    private BlockStateUtil() {

    }

    private static final ObjectField<BlockState, BlockStateData> environs_blockData =
            new ObjectField<>(
                    BlockState.class,
                    () -> BlockStateData.DEFAULT,
                    "environs_blockData"
            );

    @Nonnull
    public static BlockStateData getData(@Nonnull final BlockState state) {
        BlockStateData profile = environs_blockData.get(state);
        if (profile == null) {
            profile = BlockStateLibrary.get(state);
            environs_blockData.set(state, profile);
        }
        return profile;
    }

    public static void setData(@Nonnull final BlockState state, @Nullable final BlockStateData data) {
        //noinspection ConstantConditions
        environs_blockData.set(state, data);
    }

}

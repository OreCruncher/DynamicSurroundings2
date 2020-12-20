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

package org.orecruncher.environs.effects;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.effects.particles.Collections;

import javax.annotation.Nonnull;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class FireFlyEffect extends BlockEffect {

    public FireFlyEffect(final int chance) {
        super(chance);
    }

    @Nonnull
    @Override
    public BlockEffectType getEffectType() {
        return BlockEffectType.FIREFLY;
    }

    @Override
    public void doEffect(@Nonnull final IBlockReader provider, @Nonnull final BlockState state,
                         @Nonnull final BlockPos pos, @Nonnull final Random random) {
        Collections.addFireFly(provider, pos.getX() + 0.5F, pos.getY() + 0.5F,
                pos.getZ() + 0.5F);
    }
}

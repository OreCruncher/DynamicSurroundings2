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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.effects.emitters.DustJet;
import org.orecruncher.environs.effects.emitters.Jet;
import org.orecruncher.lib.WorldUtils;

import javax.annotation.Nonnull;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class DustJetEffect extends JetEffect {

    public DustJetEffect(final int chance) {
        super(chance);
    }

    @Nonnull
    @Override
    public BlockEffectType getEffectType() {
        return BlockEffectType.DUST_JET;
    }

    @Override
    public boolean canTrigger(@Nonnull final IBlockReader provider, @Nonnull final BlockState state,
                              @Nonnull final BlockPos pos, @Nonnull final Random random) {
        return WorldUtils.isAirBlock(provider, pos.down()) && super.canTrigger(provider, state, pos, random);
    }

    @Override
    public void doEffect(@Nonnull final IBlockReader provider, @Nonnull final BlockState state,
                         @Nonnull final BlockPos pos, @Nonnull final Random random) {
        final Jet effect = new DustJet(2, provider, pos.getX() + 0.5D, pos.getY() - 0.2D,
                pos.getZ() + 0.5D, state);
        addEffect(effect);
    }
}

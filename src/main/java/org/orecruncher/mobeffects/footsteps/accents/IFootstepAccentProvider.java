/*
 *  Dynamic Surroundings
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

package org.orecruncher.mobeffects.footsteps.accents;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.util.math.BlockPos;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;

/**
 * Interface for objects that provide additional accents to acoustics when
 * producing step sounds.
 */
@OnlyIn(Dist.CLIENT)
interface IFootstepAccentProvider {

	void provide(@Nonnull final LivingEntity entity, @Nonnull final BlockPos pos, @Nonnull final BlockState posState, @Nonnull final ObjectArray<IAcoustic> acoustics);

	boolean isEnabled();

}

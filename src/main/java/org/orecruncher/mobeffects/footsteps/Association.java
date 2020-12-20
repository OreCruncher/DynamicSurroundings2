/*
 * Dynamic Surroundings: Mob Effects
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

package org.orecruncher.mobeffects.footsteps;

import javax.annotation.Nonnull;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.orecruncher.sndctrl.api.acoustics.AcousticEvent;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.audio.acoustic.AcousticCompiler;

@OnlyIn(Dist.CLIENT)
public class Association {

	private final FootStrikeLocation location;
	private IAcoustic data;

	public Association(@Nonnull final LivingEntity entity, @Nonnull final IAcoustic association) {
		final Vec3d vec = entity.getPositionVector();
		this.location = new FootStrikeLocation(entity, vec.x, vec.y + 1, vec.z);
		this.data = association;
	}

	public Association(@Nonnull final FootStrikeLocation pos, @Nonnull final IAcoustic association) {
		this.location = pos;
		this.data = association;
	}

	public void merge(@Nonnull final IAcoustic... acoustics) {
		final IAcoustic[] t = new IAcoustic[1 + acoustics.length];
		t[0] = this.data;
		System.arraycopy(acoustics, 0, t, 1, acoustics.length);
		this.data = AcousticCompiler.combine(t);
	}

	public void play(@Nonnull final AcousticEvent event) {
		this.data.playAt(this.location.getStrikePosition(), event);
	}

	@Nonnull
	public FootStrikeLocation getStrikeLocation() {
		return this.location;
	}

	@Nonnull
	public BlockPos getStepPos() {
		return this.location.getStepPos();
	}

}
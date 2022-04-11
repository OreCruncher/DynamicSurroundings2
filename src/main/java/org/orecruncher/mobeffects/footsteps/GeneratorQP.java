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
import org.orecruncher.mobeffects.library.Constants;
import org.orecruncher.sndctrl.api.acoustics.AcousticEvent;

public class GeneratorQP extends Generator {

	private static final int USE_FUNCTION = 2;

	private int hoof = 0;
	private float nextWalkDistanceMultiplier = 0.05f;

	public GeneratorQP(@Nonnull final Variator var) {
		super(var);
	}

	@Override
	protected void stepped(@Nonnull final LivingEntity ply, @Nonnull final AcousticEvent event) {
		if (this.hoof == 0 || this.hoof == 2) {
			this.nextWalkDistanceMultiplier = RANDOM.nextFloat();
		}

		if (this.hoof >= 3) {
			this.hoof = 0;
		} else {
			this.hoof++;
		}

		if (this.hoof == 3 && event == Constants.RUN) {
			produceStep(ply, event);
			this.hoof = 0;
		}

		if (event == Constants.WALK) {
			produceStep(ply, event);
		}
	}

	protected float walkFunction2(final float distance) {
		final float overallMultiplier = this.VAR.QUADRUPED_MULTIPLIER;
		final float ndm = 0.2F;
		float pond = this.nextWalkDistanceMultiplier;
		pond *= pond;
		pond *= ndm;
		if (this.hoof == 1 || this.hoof == 3) {
			return distance * pond * overallMultiplier;
		}
		return distance * (1 - pond) * overallMultiplier;
	}

	protected float walkFunction1(final float distance) {
		final float overallMultiplier = 1.4f;
		final float ndm = 0.5f;

		if (this.hoof == 1 || this.hoof == 3) {
			return distance * (ndm + this.nextWalkDistanceMultiplier * ndm * 0.5f) * overallMultiplier;
		}
		return distance * (1 - ndm) * overallMultiplier;
	}

	protected float walkFunction0(final float distance) {
		final float overallMultiplier = 1.5f;
		final float ndm = 0.425f + this.nextWalkDistanceMultiplier * 0.15f;

		if (this.hoof == 1 || this.hoof == 3) {
			return distance * ndm * overallMultiplier;
		}
		return distance * (1 - ndm) * overallMultiplier;
	}

	@Override
	protected float reevaluateDistance(@Nonnull final AcousticEvent event, final float distance) {
		if (event == Constants.WALK)
			switch (USE_FUNCTION) {
			case 0:
				return walkFunction0(distance);
			case 1:
				return walkFunction1(distance);
			default:
				return walkFunction2(distance);
			}

		if (event == Constants.RUN && this.hoof == 0)
			return distance * 0.8f;

		if (event == Constants.RUN)
			return distance * 0.3f;

		return distance;
	}

}
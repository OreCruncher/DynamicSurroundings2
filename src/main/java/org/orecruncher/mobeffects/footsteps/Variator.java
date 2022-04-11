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

import org.orecruncher.mobeffects.library.config.VariatorConfig;

public class Variator {

	public final int IMMOBILE_DURATION;
	public final boolean EVENT_ON_JUMP;
	public final float LAND_HARD_DISTANCE_MIN;
	public final float SPEED_TO_JUMP_AS_MULTIFOOT;
	public final float SPEED_TO_RUN;
	public final float STRIDE;
	public final float STRIDE_STAIR;
	public final float STRIDE_LADDER;
	public final float QUADRUPED_MULTIPLIER;
	public final boolean PLAY_WANDER;
	public final boolean QUADRUPED;
	public final boolean PLAY_JUMP;
	public final float DISTANCE_TO_CENTER;
	public final boolean HAS_FOOTPRINT;
	public final FootprintStyle FOOTPRINT_STYLE;
	public final float FOOTPRINT_SCALE;
	public final float VOLUME_SCALE;

	public Variator() {
		this.IMMOBILE_DURATION = 4; // ticks
		this.EVENT_ON_JUMP = true;
		this.LAND_HARD_DISTANCE_MIN = 0.9F;
		this.SPEED_TO_JUMP_AS_MULTIFOOT = 0.005F;
		this.SPEED_TO_RUN = 0.22F; // 0.022F;
		this.STRIDE = 0.75F; // 0.95F;
		this.STRIDE_STAIR = this.STRIDE * 0.65F;
		this.STRIDE_LADDER = 0.5F;
		this.QUADRUPED_MULTIPLIER = 1.25F;
		this.PLAY_WANDER = true;
		this.QUADRUPED = false;
		this.PLAY_JUMP = false;
		this.DISTANCE_TO_CENTER = 0.2F;
		this.HAS_FOOTPRINT = true;
		this.FOOTPRINT_STYLE = FootprintStyle.LOWRES_SQUARE;
		this.FOOTPRINT_SCALE = 1.0F;
		this.VOLUME_SCALE = 1.0F;
	}

	public Variator(@Nonnull final VariatorConfig cfg) {
		this.IMMOBILE_DURATION = cfg.immobileDuration;
		this.EVENT_ON_JUMP = cfg.eventOnJump;
		this.LAND_HARD_DISTANCE_MIN = cfg.landHardDistanceMin;
		this.SPEED_TO_JUMP_AS_MULTIFOOT = cfg.speedToJumpAsMultifoot;
		this.SPEED_TO_RUN = cfg.speedToRun;
		this.STRIDE = cfg.stride;
		this.STRIDE_STAIR = cfg.strideStair;
		this.STRIDE_LADDER = cfg.strideLadder;
		this.QUADRUPED_MULTIPLIER = cfg.quadrupedMultiplier;
		this.PLAY_WANDER = cfg.playWander;
		this.QUADRUPED = cfg.quadruped;
		this.PLAY_JUMP = cfg.playJump;
		this.DISTANCE_TO_CENTER = cfg.distanceToCenter;
		this.HAS_FOOTPRINT = cfg.hasFootprint;
		this.FOOTPRINT_STYLE = FootprintStyle.getStyle(cfg.footprintStyle);
		this.FOOTPRINT_SCALE = cfg.footprintScale;
		this.VOLUME_SCALE = cfg.volumeScale;
	}

}

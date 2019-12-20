/*
 * Dynamic Surroundings: Sound Control
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

package org.orecruncher.lib;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.world.World;

@OnlyIn(Dist.CLIENT)
public class MinecraftClock {

	private static final String AM = Localization.load("sndctrl.format.AM");
	private static final String PM = Localization.load("sndctrl.format.PM");
	private static final String NO_SKY = Localization.load("sndctrl.format.NoSky");
	private static final String SUNRISE = Localization.load("sndctrl.format.Sunrise");
	private static final String SUNSET = Localization.load("sndctrl.format.Sunset");
	private static final String DAYTIME = Localization.load("sndctrl.format.Daytime");
	private static final String NIGHTTIME = Localization.load("sndctrl.format.Nighttime");
	private static final String TIME_FORMAT = Localization.load("sndctrl.format.TimeOfDay");

	protected int day;
	protected int hour;
	protected int minute;
	protected boolean isAM;
	protected DayCycle cycle = DayCycle.DAYTIME;

	public MinecraftClock() {

	}

	public MinecraftClock(@Nonnull final World world) {
		update(world);
	}

	public void update(@Nonnull final World world) {

		long time = world.getGameTime();
		this.day = (int) (time / 24000);
		time -= this.day * 24000;
		this.day++; // It's day 1, not 0 :)
		this.hour = (int) (time / 1000);
		time -= this.hour * 1000;
		this.minute = (int) (time / 16.666D);

		this.hour += 6;
		if (this.hour >= 24) {
			this.hour -= 24;
			this.day++;
		}

		this.isAM = this.hour < 12;

		this.cycle = DayCycle.getCycle(world);
	}

	public int getDay() {
		return this.day;
	}

	public int getHour() {
		return this.hour;
	}

	public int getMinute() {
		return this.minute;
	}

	public boolean isAM() {
		return this.isAM;
	}

	public String getTimeOfDay() {
		switch (this.cycle) {
		case NO_SKY:
			return MinecraftClock.NO_SKY;
		case SUNRISE:
			return MinecraftClock.SUNRISE;
		case SUNSET:
			return MinecraftClock.SUNSET;
		case DAYTIME:
			return MinecraftClock.DAYTIME;
		default:
			return MinecraftClock.NIGHTTIME;
		}
	}

	public String getFormattedTime() {
		return String.format(TIME_FORMAT, this.day, this.hour > 12 ? this.hour - 12 : this.hour, this.minute,
				this.isAM ? AM : PM);
	}

	@Override
	public String toString() {
		return '[' + getFormattedTime() + ' ' + getTimeOfDay() + ']';
	}
}

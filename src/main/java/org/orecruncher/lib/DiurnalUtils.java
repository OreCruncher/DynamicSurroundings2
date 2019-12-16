/* 
 * Licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.lib;

import javax.annotation.Nonnull;

import net.minecraft.world.IWorld;
import org.orecruncher.lib.math.MathStuff;

public final class DiurnalUtils {

	public enum DayCycle {
		NO_SKY,
		SUNRISE,
		SUNSET,
		DAYTIME,
		NIGHTTIME
	}

	private DiurnalUtils() {
	}

	public static boolean isDaytime(@Nonnull final IWorld world) {
		return getCycle(world) == DayCycle.DAYTIME;
	}

	public static boolean isNighttime(@Nonnull final IWorld world) {
		return getCycle(world) == DayCycle.NIGHTTIME;
	}

	public static boolean isSunrise(@Nonnull final IWorld world) {
		return getCycle(world) == DayCycle.SUNRISE;
	}

	public static boolean isSunset(@Nonnull final IWorld world) {
		return getCycle(world) == DayCycle.SUNSET;
	}

	public static DayCycle getCycle(@Nonnull final IWorld world) {
		if (world.getDimension().isNether() || !world.getDimension().hasSkyLight())
			return DayCycle.NO_SKY;

		final float brFactor = world.getDimension().getSunBrightness(1.0f);
		if (brFactor > 0.6f)
			return DayCycle.DAYTIME;
		if (brFactor < 0.1f)
			return DayCycle.NIGHTTIME;
		if (MathStuff.sin(world.getCelestialAngle(1.0f)) > 0.0)
			return DayCycle.SUNSET;
		return DayCycle.SUNRISE;
	}

	public static float getMoonPhaseFactor(@Nonnull final IWorld world) {
		return world.getCurrentMoonPhaseFactor();
	}

	public static boolean isAuroraVisible(@Nonnull final IWorld world) {
		return !isAuroraInvisible(world);
	}

	public static boolean isAuroraInvisible(@Nonnull final IWorld world) {
		final DayCycle cycle = getCycle(world);
		return cycle == DayCycle.SUNRISE || cycle == DayCycle.DAYTIME;
	}
}

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

import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.sndctrl.SoundControl;

import javax.annotation.Nonnull;

public enum DayCycle {

    NO_SKY(false, "NoSky"),
    SUNRISE(false, "Sunrise"),
    SUNSET(true, "Sunset"),
    DAYTIME(false, "Daytime"),
    NIGHTTIME(true, "Nighttime");

    private final boolean auroraVisible;
    private final String localizeString;

    DayCycle(final boolean auroraVisible, @Nonnull final String localName) {
        this.auroraVisible = auroraVisible;
        this.localizeString = SoundControl.MOD_ID + ".format." + localName;
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
        if (brFactor > 0.68f)   // 0.6F on 0-1 scale
            return DayCycle.DAYTIME;
        if (brFactor < 0.3f)    // 0 on 0-1 scale
            return DayCycle.NIGHTTIME;
        if (MathStuff.sin(world.getCelestialAngle(1.0f)) > 0.0)
            return DayCycle.SUNSET;
        return DayCycle.SUNRISE;
    }

    public static float getMoonPhaseFactor(@Nonnull final IWorld world) {
        return world.getCurrentMoonPhaseFactor();
    }

    public static boolean isAuroraVisible(@Nonnull final IWorld world) {
        return getCycle(world).isAuroraVisible();
    }

    public static boolean isAuroraInvisible(@Nonnull final IWorld world) {
        return !getCycle(world).isAuroraVisible();
    }

    public boolean isAuroraVisible() {
        return this.auroraVisible;
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public String getFormattedName() {
        return Localization.load(this.localizeString);
    }

}

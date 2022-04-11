/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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

package org.orecruncher.lib.scripting.sets;

import org.orecruncher.lib.DayCycle;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.scripting.VariableSet;

import javax.annotation.Nonnull;

public class DiurnalCycleVariables extends VariableSet<IDiurnalCycle> implements IDiurnalCycle {

    private final LazyVariable<Float> moonPhaseFactor = new LazyVariable<>(() -> GameUtils.isInGame() ? DayCycle.getMoonPhaseFactor(GameUtils.getWorld()) : 0F);
    private final LazyVariable<Float> celestialAngle = new LazyVariable<>(() -> GameUtils.isInGame() ? GameUtils.getWorld().func_242415_f(0F) : 0F);
    private boolean isAuroraVisible;
    private boolean isDay;
    private boolean isNight;
    private boolean isSunrise;
    private boolean isSunset;

    public DiurnalCycleVariables() {
        super("diurnal");
    }

    @Nonnull
    @Override
    public IDiurnalCycle getInterface() {
        return this;
    }

    public void update() {

        if (GameUtils.isInGame()) {
            DayCycle cycle = DayCycle.getCycle(GameUtils.getWorld());
            this.isAuroraVisible = cycle.isAuroraVisible();
            this.isDay = cycle == DayCycle.DAYTIME;
            this.isNight = cycle == DayCycle.NIGHTTIME;
            this.isSunrise = cycle == DayCycle.SUNRISE;
            this.isSunset = cycle == DayCycle.SUNSET;
        } else {
            this.isAuroraVisible = false;
            this.isDay = false;
            this.isNight = false;
            this.isSunrise = false;
            this.isSunset = false;
        }

        this.moonPhaseFactor.reset();
        this.celestialAngle.reset();
    }

    @Override
    public boolean isDay() {
        return this.isDay;
    }

    @Override
    public boolean isNight() {
        return this.isNight;
    }

    @Override
    public boolean isSunrise() {
        return this.isSunrise;
    }

    @Override
    public boolean isSunset() {
        return this.isSunset;
    }

    @Override
    public boolean isAuroraVisible() {
        return this.isAuroraVisible;
    }

    @Override
    public float getMoonPhaseFactor() {
        return this.moonPhaseFactor.get();
    }

    @Override
    public float getCelestialAngle() {
        return this.celestialAngle.get();
    }
}

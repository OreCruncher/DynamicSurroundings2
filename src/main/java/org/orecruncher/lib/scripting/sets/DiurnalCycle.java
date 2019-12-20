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

package org.orecruncher.lib.scripting.sets;

import org.orecruncher.lib.DiurnalUtils;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.scripting.VariableSet;

import javax.annotation.Nonnull;

public class DiurnalCycle extends VariableSet<IDiurnalCycle> implements IDiurnalCycle {

    private final LazyVariable<Boolean> isAuroraVisible = new LazyVariable<>(() -> GameUtils.isInGame() && DiurnalUtils.isAuroraVisible(GameUtils.getWorld()));
    private final LazyVariable<Float> moonPhaseFactor = new LazyVariable<>(() -> GameUtils.isInGame() ? DiurnalUtils.getMoonPhaseFactor(GameUtils.getWorld()) : 0F);
    private final LazyVariable<Float> celestialAngle = new LazyVariable<>(() -> GameUtils.isInGame() ? GameUtils.getWorld().getCelestialAngle(0F) : 0F);
    private DiurnalUtils.DayCycle cycle = DiurnalUtils.DayCycle.DAYTIME;
    private final LazyVariable<Boolean> isDay = new LazyVariable<>(() -> cycle == DiurnalUtils.DayCycle.DAYTIME);
    private final LazyVariable<Boolean> isNight = new LazyVariable<>(() -> cycle == DiurnalUtils.DayCycle.NIGHTTIME);
    private final LazyVariable<Boolean> isSunrise = new LazyVariable<>(() -> cycle == DiurnalUtils.DayCycle.SUNRISE);
    private final LazyVariable<Boolean> isSunset = new LazyVariable<>(() -> cycle == DiurnalUtils.DayCycle.SUNSET);

    public DiurnalCycle() {
        super("diurnal");
    }

    @Nonnull
    @Override
    public IDiurnalCycle getInterface() {
        return this;
    }

    public void update() {

        if (GameUtils.isInGame()) {
            this.cycle = DiurnalUtils.getCycle(GameUtils.getWorld());
        } else {
            this.cycle = DiurnalUtils.DayCycle.DAYTIME;
        }

        this.isDay.reset();
        this.isNight.reset();
        this.isSunset.reset();
        this.isSunset.reset();
        this.isAuroraVisible.reset();
        this.moonPhaseFactor.reset();
        this.celestialAngle.reset();
    }

    @Override
    public boolean isDay() {
        return this.isDay.get();
    }

    @Override
    public boolean isNight() {
        return this.isNight.get();
    }

    @Override
    public boolean isSunrise() {
        return this.isSunrise.get();
    }

    @Override
    public boolean isSunset() {
        return this.isSunset.get();
    }

    @Override
    public boolean isAuroraVisible() {
        return this.isAuroraVisible.get();
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

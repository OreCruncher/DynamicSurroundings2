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

package org.orecruncher.lib.math;

import javax.annotation.Nonnull;

/**
 * Simple EMA calculator.
 */
public class EMA {

    private final String name;
    private final double factor;
    private double ema;

    public EMA() {
        this("UNNAMED");
    }

    public EMA(@Nonnull final String name) {
        this(name, 100);
    }

    public EMA(@Nonnull final String name, final int periods) {
        this.name = name;
        this.factor = 2D / (periods + 1);
        this.ema = Double.NaN;
    }

    public double update(final double newValue) {
        if (Double.isNaN(this.ema)) {
            this.ema = newValue;
        } else {
            this.ema = (newValue - this.ema) * this.factor + this.ema;
        }
        return this.ema;
    }

    public String name() {
        return this.name;
    }

    public double get() {
        return this.ema;
    }

}

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

package org.orecruncher.lib.random;

/**
 * Simple Linear congruential generator for integer psuedo random numbers.
 * Intended to be fast. Limit is that it can only generate random numbers 0 -
 * 32K.
 */
@SuppressWarnings("unused")
public final class LCGRandom {

    private long v;

    /**
     * Creates and seeds an LCG using an integer from XorShiftRandom.
     */
    public LCGRandom() {
        this(SplitMax.current().next());
    }

    /**
     * Creates and initializes an LCG generator using a seed value.
     *
     * @param seed Seed to initialize the LCG generator with
     */
    public LCGRandom(final long seed) {
        this.v = seed;
    }

    /**
     * Generates a random number between 0 and the bound specified.
     *
     * @param bound upper bound of the random integer generated
     * @return Pseudo random integer between 0 and bound
     */
    public int nextInt(final int bound) {
        this.v = (2862933555777941757L * this.v + 3037000493L);
        return ((int) ((this.v >> 32) & 0x7FFFFFFF)) % bound;
    }
}

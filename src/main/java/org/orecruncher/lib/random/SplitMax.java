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

import java.time.Instant;

/**
 * RNG that is used to seed the other random implementations.
 */
final class SplitMax {

    private static final long PRIME = 402653189;

    private static final ThreadLocal<SplitMax> localRandom = ThreadLocal.withInitial(SplitMax::new);
    private long x;

    private SplitMax() {
        this(Instant.now().getNano() * PRIME + System.nanoTime());
    }

    private SplitMax(final long seed) {
        this.x = seed;
    }

    public static SplitMax current() {
        return localRandom.get();
    }

    public long next() {
        long z = (this.x += 0x9e3779b97f4a7c15L);
        z = (z ^ (z >> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >> 31);
    }
}

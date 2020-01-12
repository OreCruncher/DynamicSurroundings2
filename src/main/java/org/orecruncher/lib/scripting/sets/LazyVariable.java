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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * LazyVariable performs value caching to minimize the impact of initialization.  It has a reset mechanic so the value
 * can be cleared and requeried as needed.  (Essentially this is a lighter weight LazyOptional.)
 *
 * @param <T> Type of value that is cached
 */
@OnlyIn(Dist.CLIENT)
public final class LazyVariable<T> {

    @Nonnull
    private final Supplier<T> supplier;

    private T value;

    public LazyVariable(@Nonnull final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public void reset() {
        this.value = null;
    }

    public T get() {
        if (this.value == null)
            this.value = this.supplier.get();
        return this.value;
    }
}

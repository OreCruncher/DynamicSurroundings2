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

package org.orecruncher.lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public final class Singleton<T> {

    private static final Object NO_INIT = new Object();

    private final Supplier<T> factory;
    @SuppressWarnings("unchecked")
    private volatile T instance = (T) NO_INIT;

    public Singleton(@Nonnull final Supplier<T> factory) {
        this.factory = factory;
    }

    @Nullable
    public T get() {
        T result = this.instance;

        if (result == NO_INIT)
            synchronized (this) {
                result = this.instance;
                if (result == NO_INIT)
                    this.instance = result = this.factory.get();
            }
        return result;
    }
}

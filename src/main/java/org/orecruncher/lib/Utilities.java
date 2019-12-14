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
import javax.annotation.Nullable;
import java.util.Optional;

public final class Utilities {
    private Utilities() {
    }

    /**
     * Iterates through the parametesr to indentify the first non-null value and returns it.  If no parameters are
     * specified, or a non-null reference could not be found, an exception will be thrown.
     *
     * @param objs Object references to evaluate
     * @param <T> Type of object
     * @return Reference to the first non-null reference in the parameter list.
     */
    @SafeVarargs
    @Nonnull
    public static <T> T firstNonNull(@Nullable final T... objs) {
        if (objs != null && objs.length > 0)
            for (final T o : objs)
                if (o != null)
                    return o;
        throw new NullPointerException();
    }

    @Nonnull
    public static <S, T> Optional<T> safeCast(@Nonnull final S candidate, @Nonnull final Class<T> target) {
        return target.isInstance(candidate) ? Optional.of(target.cast(candidate)) : Optional.empty();
    }
}

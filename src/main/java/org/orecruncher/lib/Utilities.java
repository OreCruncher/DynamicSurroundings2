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
import java.util.Optional;

public final class Utilities {
    private Utilities() {
    }

    @Nonnull
    public static <S, T> Optional<T> safeCast(@Nonnull final S candidate, @Nonnull final Class<T> target) {
        return target.isInstance(candidate) ? Optional.of(target.cast(candidate)) : Optional.empty();
    }
}

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

package org.orecruncher.lib.scripting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Methods that are added to the JavaScript runtime environment for use by scripts.
 */
public final class LibraryFunctions {

    public Object iif(final boolean flag, @Nullable final Object trueResult, @Nullable final Object falseResult) {
        return flag ? trueResult : falseResult;
    }

    public boolean match(@Nonnull final String pattern, @Nonnull final String subject) {
        Objects.requireNonNull(pattern);
        Objects.requireNonNull(subject);
        return Pattern.matches(pattern, subject);
    }

    public boolean oneof(@Nonnull final Object testee, @Nonnull final Object... possibles) {
        Objects.requireNonNull(testee);
        Objects.requireNonNull(possibles);
        for (final Object obj : possibles)
            if (testee.equals(obj))
                return true;
        return false;
    }
}

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

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utilities {
    private Utilities() {
    }

    /**
     * Iterates through the parametesr to indentify the first non-null value and returns it.  If no parameters are
     * specified, or a non-null reference could not be found, an exception will be thrown.
     *
     * @param objs Object references to evaluate
     * @param <T>  Type of object
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
    public static <S, T> Optional<T> safeCast(@Nullable final S candidate, @Nonnull final Class<T> target) {
        return target.isInstance(candidate) ? Optional.of(target.cast(candidate)) : Optional.empty();
    }

    public static int[] splitToInts(@Nonnull final String str, final char splitChar) {

        final String[] tokens = StringUtils.split(str, splitChar);
        if (tokens == null || tokens.length == 0)
            return new int[0];

        final int[] result = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            result[i] = Integer.parseInt(tokens[i]);
        }

        return result;
    }

    @Nonnull
    public static <T> T[] append(@Nonnull final T[] a, @Nullable final T b) {

        if (b == null)
            return a;

        final int aLen = a.length;

        @SuppressWarnings("unchecked") final T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + 1);
        System.arraycopy(a, 0, c, 0, aLen);
        c[aLen] = b;

        return c;
    }

    public static String safeResourcePath(@Nonnull final String path) {
        return path.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._\\-]", ".");
    }
}

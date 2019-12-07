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

package org.orecruncher.lib.reflection;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class ReflectionHelper {
    private ReflectionHelper() {
    }

    @Nullable
    public static Field resolveField(@Nonnull final String className, @Nonnull final String... fieldName) {
        Preconditions.checkNotNull(className);
        Preconditions.checkArgument(fieldName.length > 0, "Field name cannot be empty");
        try {
            return resolveField(Class.forName(className), fieldName);
        } catch (@Nonnull final Throwable ignored) {
        }
        return null;
    }

    @Nullable
    public static Field resolveField(@Nonnull final Class<?> clazz, @Nonnull final String... fieldName) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkArgument(fieldName.length > 0, "Field name cannot be empty");
        for (final String name : fieldName) {
            try {
                final Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (final Throwable ignored) {
            }
        }
        return null;
    }

    @Nullable
    public static Method resolveMethod(@Nonnull final String className, @Nonnull final String[] names, Class<?>... parameters) {
        try {
            return resolveMethod(Class.forName(className), names, parameters);
        } catch (@Nonnull final Throwable ignored) {
        }
        return null;
    }

    @Nullable
    public static Method resolveMethod(@Nonnull final Class<?> clazz, @Nonnull final String[] names, Class<?>... parameters) {

        for (final String name : names) {
            try {
                final Method f = clazz.getDeclaredMethod(name, parameters);
                f.setAccessible(true);
                return f;
            } catch (@Nonnull final Throwable ignored) {
            }
        }
        return null;
    }

    @Nullable
    public static Class<?> resolveClass(@Nonnull final String className) {
        Preconditions.checkNotNull(className);
        try {
            return Class.forName(className);
        } catch (@Nonnull final Throwable ignored) {
        }
        return null;
    }

}

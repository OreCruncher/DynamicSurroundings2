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

import javax.annotation.Nonnull;

public class FloatField<T> extends ObjectField<T, Float> {

    public FloatField(@Nonnull final String className, @Nonnull final String... fieldName) {
        super(className, 0F, fieldName);
    }

    public FloatField(@Nonnull final String className, final float defaultValue, @Nonnull final String... fieldName) {
        super(className, defaultValue, fieldName);
    }

    public FloatField(@Nonnull final Class<T> clazz, @Nonnull final String... fieldName) {
        this(clazz, 0F, fieldName);
    }

    public FloatField(@Nonnull final Class<T> clazz, final float defaultValue, @Nonnull final String... fieldName) {
        super(clazz, defaultValue, fieldName);
    }
}
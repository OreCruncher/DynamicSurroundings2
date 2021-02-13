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

package org.orecruncher.lib.validation;

import it.unimi.dsi.fastutil.objects.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.function.Consumer;

public class Validators {

    private static final Reference2ObjectMap<Type, IValidator<?>> registeredValidators = new Reference2ObjectOpenHashMap<>();

    public static void registerValidator(@Nonnull final Type type, @Nonnull final IValidator<?> validator) {
        registeredValidators.put(type, validator);
    }

    public static <T> void validate(@Nonnull final T obj) throws ValidationException {
        validate(obj, (Consumer<String>) null);
    }

    public static <T> void validate(@Nonnull final T obj, @Nullable final Consumer<String> errorLogging) throws ValidationException {
        try {
            if (obj instanceof IValidator) {
                @SuppressWarnings("unchecked") final IValidator<T> v = (IValidator<T>) obj;
                v.validate(obj);
            }
        } catch (@Nonnull final ValidationException ex) {
            if (errorLogging != null) {
                errorLogging.accept(ex.getMessage());
            } else {
                throw ex;
            }
        }
    }

    public static <T> void validate(@Nonnull final T obj, @Nullable final Type type) throws ValidationException {
        validate(obj, type, null);
    }

    public static <T> void validate(@Nonnull final T obj, @Nullable final Type type, @Nullable final Consumer<String> errorLogging) throws ValidationException {
        try {
            if (obj instanceof IValidator) {
                @SuppressWarnings("unchecked") final IValidator<T> v = (IValidator<T>) obj;
                v.validate(obj);
            } else {
                @SuppressWarnings("unchecked") final IValidator<T> validator = (IValidator<T>) registeredValidators.get(type);
                if (validator != null)
                    validator.validate(obj);
            }
        } catch (@Nonnull final ValidationException ex) {
            if (errorLogging != null) {
                errorLogging.accept(ex.getMessage());
            } else {
                throw ex;
            }
        }
    }
}

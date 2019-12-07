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
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.Lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class ReflectedMethod<R> {

    protected final String className;
    protected final String methodName;
    protected final Method method;

    public ReflectedMethod(@Nonnull final String className, @Nonnull final String methodName,
                           @Nullable final String obfMethodName, Class<?>... parameters) {
        this.className = className;
        this.methodName = methodName;
        this.method = ReflectionHelper.resolveMethod(className, new String[]{methodName, obfMethodName}, parameters);

        if (isNotAvailable()) {
            final String msg = String.format("Unable to locate method [%s::%s]", this.className, methodName);
            Lib.LOGGER.warn(msg);
        }
    }

    public ReflectedMethod(@Nonnull final Class<?> clazz, @Nonnull final String methodName,
                           @Nullable final String obfMethodName, Class<?>... parameters) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkArgument(StringUtils.isNotEmpty(methodName), "Field name cannot be empty");
        this.className = clazz.getName();
        this.methodName = methodName;
        this.method = ReflectionHelper.resolveMethod(clazz, new String[]{methodName, obfMethodName}, parameters);

        if (isNotAvailable()) {
            final String msg = String.format("Unable to locate method [%s::%s]", this.className, methodName);
            Lib.LOGGER.warn(msg);
        }
    }

    public boolean isNotAvailable() {
        return this.method == null;
    }

    @SuppressWarnings("unchecked")
    public R invoke(Object ref, Object... parms) {
        check();
        try {
            return (R) this.method.invoke(ref, parms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void check() {
        if (isNotAvailable()) {
            final String msg = String.format("Uninitialized method [%s::%s]", this.className, this.methodName);
            throw new IllegalStateException(msg);
        }
    }

}

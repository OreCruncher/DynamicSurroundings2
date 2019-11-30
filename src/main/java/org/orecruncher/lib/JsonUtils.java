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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

public final class JsonUtils {
    private JsonUtils() {

    }

    /**
     * Process the specified config file extracting data into a map based on the templated parameter.  The format of
     * the file is assumed to be a map.
     *
     * @param location Location of the config file in the resource tree, typically modid:sounds.json.
     * @param clazz    Class type of the configuration entity to revieve data for each element encountered
     * @param <T>      Value type that is expected to be returned
     * @return Map containing the necessary information.  If there was an error processing a null is returned.
     */
    @Nonnull
    public static <T> Map<String, T> loadConfig(@Nonnull final ResourceLocation location, @Nonnull final Class<T> clazz) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(clazz);

        final String asset = String.format("/assets/%s/%s", location.getNamespace(), location.getPath());
        final ParameterizedType type = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class, clazz};
            }

            @Override
            public Type getRawType() {
                return Map.class;
            }

            @Override
            @Nullable
            public Type getOwnerType() {
                return null;
            }
        };

        final Map<String, T> result = load(type, asset);
        return result != null ? result : ImmutableMap.of();
    }

    @Nullable
    public static <T> T load(@Nonnull final Type type, @Nonnull final String path) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(path);

        try (final InputStream stream = JsonUtils.class.getResourceAsStream(path)) {
            if (stream != null)
                return load(stream, type);
        } catch (final Throwable t) {
            Lib.LOGGER.error(t, "Unable to load resource [%s] from JAR", path);
        }
        return null;
    }

    @Nonnull
    public static <T> T load(@Nonnull final ResourceLocation location, @Nonnull final Class<T> clazz) throws Exception {
        final String asset = String.format("/assets/%s/%s", location.getNamespace(), location.getPath());
        return load(asset, clazz);
    }

    @Nonnull
    public static <T> T load(@Nonnull final String path, @Nonnull final Class<T> clazz) throws Exception {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(path);

        try (final InputStream stream = clazz.getResourceAsStream(path)) {
            return load(stream, clazz);
        } catch (final Throwable t) {
            Lib.LOGGER.error(t, "Unable to load resource [%s] from JAR", path);
        }
        return clazz.newInstance();
    }

    @Nonnull
    public static <T> T load(@Nonnull final InputStream stream, @Nonnull final Class<T> clazz) throws Exception {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(clazz);

        try (final InputStreamReader reader = new InputStreamReader(stream)) {
            return new Gson().fromJson(reader, clazz);
        } catch (final Throwable t) {
            Lib.LOGGER.error(t, "Unable to process Json from stream");
        }
        return clazz.newInstance();
    }

    @Nullable
    public static <T> T load(@Nonnull final InputStream stream, @Nonnull final Type type) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(type);

        try (final InputStreamReader reader = new InputStreamReader(stream)) {
            return new Gson().fromJson(reader, type);
        } catch (final Throwable t) {
            Lib.LOGGER.error(t, "Unable to process Json from stream");
        }
        return null;
    }
}

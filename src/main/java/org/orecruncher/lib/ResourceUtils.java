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

import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.nio.charset.Charset;

public final class ResourceUtils {
    private ResourceUtils() {

    }

    public static String readResource(@Nonnull final ResourceLocation location) {
        final String asset = String.format("/assets/%s/%s", location.getNamespace(), location.getPath());
        try(InputStream stream = ResourceUtils.class.getResourceAsStream(asset)) {
            return IOUtils.toString(stream, Charset.defaultCharset());
        } catch(@Nonnull final Throwable ignore) {
        }

        return null;
    }
}

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

package org.orecruncher.lib.resource;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.InputStream;

final class ResourceAccessorJar extends ResourceAccessorBase {

    // Used to find assets within the current jar
    final String asset;

    public ResourceAccessorJar(@Nonnull final String rootContainer, @Nonnull ResourceLocation location) {
        this(location, String.format("/assets/%s/%s/%s", rootContainer, location.getNamespace(), location.getPath()));
    }

    public ResourceAccessorJar(@Nonnull final ResourceLocation location, @Nonnull final String asset) {
        super(location);
        this.asset = asset;
    }

    @Override
    protected byte[] getAsset() {
        try (InputStream stream = ResourceAccessorJar.class.getResourceAsStream(this.asset)) {
            return IOUtils.toByteArray(stream);
        } catch (@Nonnull Throwable ignore) {
        }
        return null;
    }
}

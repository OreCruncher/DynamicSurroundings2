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

import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.InputStream;

final class ResourceAccessorPack extends ResourceAccessorBase {

    private final IResourcePack pack;
    private final ResourceLocation actual;

    public ResourceAccessorPack(@Nonnull final ResourceLocation location, @Nonnull final IResourcePack pack, @Nonnull final ResourceLocation actual) {
        super(location);
        this.pack = pack;
        this.actual = actual;
    }

    @Override
    protected byte[] getAsset() {
        try {
            try (InputStream stream = this.pack.getResourceStream(ResourcePackType.CLIENT_RESOURCES, this.actual)) {
                return IOUtils.toByteArray(stream);
            }
        } catch (@Nonnull final Throwable ignore) {
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s (%s = %s)", super.toString(), this.pack.getName(), this.actual.toString());
    }
}

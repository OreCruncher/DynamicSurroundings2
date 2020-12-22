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

import javax.annotation.Nonnull;
import java.io.File;

final class ResourceAccessorCached extends ResourceAccessorBase {

    final ResourceAccessorExternal external;
    final ResourceAccessorJar internal;

    public ResourceAccessorCached(@Nonnull final String rootContainer, @Nonnull final File root, @Nonnull final ResourceLocation location) {
        super(location);
        this.external = new ResourceAccessorExternal(root, location);
        this.internal = new ResourceAccessorJar(rootContainer, location);
    }

    @Override
    protected byte[] getAsset() {
        byte[] result = this.external.asBytes();
        if (result == null)
            result = this.internal.asBytes();
        return result;
    }
}

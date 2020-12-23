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

abstract class ResourceAccessorBase implements IResourceAccessor {

    private final ResourceLocation location;
    private byte[] bytes;

    public ResourceAccessorBase(@Nonnull final ResourceLocation location) {
        this.location = location;
    }

    @Override
    public ResourceLocation location() {
        return this.location;
    }

    @Override
    public byte[] asBytes() {
        if (this.bytes == null)
            this.bytes = getAsset();
        return this.bytes;
    }

    abstract protected byte[] getAsset();

    @Override
    public String toString() {
        return this.location.toString();
    }
}

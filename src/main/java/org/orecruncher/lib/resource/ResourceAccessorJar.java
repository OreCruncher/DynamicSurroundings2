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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.InputStream;

@OnlyIn(Dist.CLIENT)
final class ResourceAccessorJar extends ResourceAccessorBase {

    // Used to find assets within the current jar
    final String asset;

    public ResourceAccessorJar(@Nonnull final String rootContainer, @Nonnull final ResourceLocation location) {
        this(location, String.format("/assets/%s/%s/%s", rootContainer, location.getNamespace(), location.getPath()));
    }

    public ResourceAccessorJar(@Nonnull final ResourceLocation location, @Nonnull final String asset) {
        super(location);
        this.asset = asset;
    }

    @Override
    protected byte[] getAsset() {
        try (InputStream stream = ResourceAccessorJar.class.getResourceAsStream(this.asset)) {
            // Will be null if resource not found
            if (stream != null)
                return IOUtils.toByteArray(stream);
        } catch (@Nonnull final Throwable t) {
            logError(t);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", super.toString(), this.asset);
    }
}

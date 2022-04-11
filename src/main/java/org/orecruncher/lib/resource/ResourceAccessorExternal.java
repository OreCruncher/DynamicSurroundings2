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
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class ResourceAccessorExternal extends ResourceAccessorBase {

    final Path filePath;

    public ResourceAccessorExternal(@Nonnull final File root, @Nonnull final ResourceLocation location) {
        super(location);
        this.filePath = Paths.get(root.getPath(), location.getNamespace(), location.getPath());
    }

    @Override
    public boolean exists() {
        return Files.exists(this.filePath);
    }

    @Override
    @Nullable
    protected byte[] getAsset() {
        try {
            return Files.readAllBytes(this.filePath);
        } catch (@Nonnull final Throwable t) {
            logError(t);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", super.toString(), this.filePath);
    }
}

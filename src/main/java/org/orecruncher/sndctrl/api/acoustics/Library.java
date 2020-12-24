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

package org.orecruncher.sndctrl.api.acoustics;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.library.AcousticLibrary;
import org.orecruncher.sndctrl.library.SoundLibrary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

/**
 * API Interface to the Acoustic Library that maintains a list of all acoustics.
 */
@OnlyIn(Dist.CLIENT)
public final class Library {
    private Library() {

    }

    /**
     * Resolves the provided name into a resource location.  The rules are:
     *
     * 1.  If the name is a fully qualified resource path it is used to create the ResourceLocation.
     * 2.  If there is no namespace specified in the name, the defaultDomain is used as the namespace.
     * 3.  If the name is prefixed with an @ it assumes the Minecraft domain.
     *
     * @param defaultDomain The namespace name to use if the name does not have that component.
     * @param name The resource path to convert into a ResourceLocation.
     * @return A ResourceLocation using the above rules.
     */

    @Nonnull
    public static ResourceLocation resolveResource(@Nonnull final String defaultDomain, @Nonnull final String name) {
        return AcousticLibrary.resolveResource(defaultDomain, name);
    }

    /**
     * Finds the acoustic matching the specified ResourceLocation.
     *
     * @param acoustic The resource path of the desired acoustic.
     * @return The acoustic corresponding the the resource path.
     */
    @Nonnull
    public static IAcoustic resolve(@Nonnull final ResourceLocation acoustic) {
        return AcousticLibrary.resolve(acoustic);
    }

    @Nonnull
    public static IAcoustic resolve(@Nonnull final ResourceLocation acousticName, @Nullable final String definition) {
        return resolve(acousticName, definition, false);
    }

    @Nonnull
    public static IAcoustic resolve(@Nonnull final ResourceLocation acousticName, @Nullable final String definition, final boolean overwrite) {
        return AcousticLibrary.resolve(acousticName, definition, overwrite);
    }

    @Nonnull
    public static IAcoustic resolve(@Nonnull final String namespace, @Nonnull String definition, @Nullable final Function<ResourceLocation, IAcoustic> specials) {
        return AcousticLibrary.resolve(namespace, definition, specials);
    }

    /**
     * Finds the SoundEvent that corresponds to the specified resource path.
     *
     * @param sound The resource path of the sound to locate.
     * @return SoundEvent instance that corresponds to the resource path
     */
    @Nonnull
    public static Optional<SoundEvent> getSound(@Nonnull final ResourceLocation sound) {
        return SoundLibrary.getSound(sound);
    }
}

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

package org.orecruncher.sndctrl.library;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.acoustic.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public final class AcousticLibrary {

    private static final Map<ResourceLocation, IAcoustic> compiled = new Object2ObjectAVLTreeMap<>();

    AcousticLibrary() {

    }

    /**
     * Adds/replaces a known acoustic in the library.
     *
     * @param name     Name of the acoustic
     * @param acoustic The acoustic mapped to that name
     */
    public static void addAcoustic(@Nonnull final ResourceLocation name, @Nonnull final IAcoustic acoustic) {
        compiled.put(name, acoustic);
    }

    public static void initialize() {
        // This should be invoked from the completion phase of loading.  Need to scan the entire sound event list
        // creating acoustic entries.
        final Map<ResourceLocation, SoundEvent> sounds = SoundLibrary.getRegisteredSounds();
        for (final Map.Entry<ResourceLocation, SoundEvent> kvp : sounds.entrySet()) {
            if (!compiled.containsKey(kvp.getKey())) {
                final Optional<SoundEvent> evt = SoundLibrary.getSound(kvp.getKey());
                evt.ifPresent(e -> addAcoustic(e.getName(), new SimpleAcoustic(e)));
            }
        }
    }

    public static void processFile(@Nonnull final ResourceLocation acousticFile) {
        final AcousticCompiler compiler = new AcousticCompiler(acousticFile.getNamespace());
        final List<IAcoustic> acoustics = compiler.compile(acousticFile);
        for (final IAcoustic a : acoustics) {
            addAcoustic(a.getName(), a);
        }
    }

    @Nonnull
    public static IAcoustic resolve(@Nonnull final ResourceLocation acousticName) {
        IAcoustic result = compiled.get(acousticName);
        if (result == null) {
            final IAcoustic[] parsed = Arrays.stream(acousticName.getPath().split(",")).map(fragment -> {
                // See if we have an acoustic for this fragment
                final ResourceLocation fragLoc = new ResourceLocation(fragment);
                final IAcoustic a = generateAcoustic(fragLoc);
                if (a == null)
                    SoundControl.LOGGER.warn("Acoustic '%s' not found!", fragment);
                return a;
            }).filter(Objects::nonNull).toArray(IAcoustic[]::new);

            if (parsed.length == 0) {
                result = NullAcoustic.INSTANCE;
            } else if (parsed.length == 1) {
                result = parsed[0];
            } else {
                final SimultaneousAcoustic s = new SimultaneousAcoustic(acousticName);
                for (final IAcoustic t : parsed)
                    s.add(t);
                result = s;
            }
            addAcoustic(acousticName, result);
        }

        return result;
    }

    @Nonnull
    public static ResourceLocation resolveResource(@Nonnull final String defaultDomain, @Nonnull final String name) {
        if (StringUtils.isNullOrEmpty(name))
            throw new IllegalArgumentException("Sound name is null or empty");
        if (StringUtils.isNullOrEmpty(defaultDomain))
            throw new IllegalArgumentException("Default domain is null or empty");

        ResourceLocation res;
        if (name.charAt(0) == '@') {
            // Sound is in the Minecraft namespace
            res = new ResourceLocation("minecraft", name.substring(1));
        } else if (!name.contains(":")) {
            // It's just a path so assume the specified namespace
            res = new ResourceLocation(defaultDomain, name);
        } else {
            // It's a fully qualified location
            res = new ResourceLocation(name);
        }
        return res;
    }

    @Nullable
    private static IAcoustic generateAcoustic(@Nonnull final ResourceLocation name) {
        IAcoustic a = compiled.get(name);
        if (a == null) {
            // Nope. Doesn't exist yet. It could be a sound name based on location.
            final Optional<SoundEvent> evt = SoundLibrary.getSound(name);
            if (evt.isPresent())
                a = generateAcoustic(evt.get());
        }

        return a;
    }

    @Nonnull
    private static IAcoustic generateAcoustic(@Nonnull final SoundEvent evt) {
        IAcoustic result = compiled.get(evt.getName());
        if (result == null) {
            result = new SimpleAcoustic(evt);
            addAcoustic(result.getName(), result);
        }
        return result;
    }

}

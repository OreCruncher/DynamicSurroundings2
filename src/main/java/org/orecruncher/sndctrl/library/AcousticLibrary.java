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

package org.orecruncher.sndctrl.library;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.resource.IResourceAccessor;
import org.orecruncher.lib.resource.ResourceUtils;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.audio.acoustic.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public final class AcousticLibrary {

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(AudioEffectLibrary.class);

    private static final ResourceLocation ADHOC = new ResourceLocation(SoundControl.MOD_ID, "ad_hoc");
    private static final Map<String, IAcoustic> compiled = new Object2ObjectAVLTreeMap<>();

    AcousticLibrary() {

    }

    public static Stream<String> dump() {
        return compiled.entrySet().stream().map(kvp -> String.format("%s -> %s", kvp.getKey(), kvp.getValue().toString())).sorted();
    }

    /**
     * Adds/replaces a known acoustic in the library.
     *
     * @param name     Name of the acoustic
     * @param acoustic The acoustic mapped to that name
     */
    public static void addAcoustic(@Nonnull final ResourceLocation name, @Nonnull final IAcoustic acoustic) {
        compiled.put(name.toString(), acoustic);
    }

    public static void initialize() {
        // This should be invoked from the completion phase of loading.  Need to scan the entire sound event list
        // creating acoustic entries.
        final Map<ResourceLocation, SoundEvent> sounds = SoundLibrary.getRegisteredSounds();
        for (final Map.Entry<ResourceLocation, SoundEvent> kvp : sounds.entrySet()) {
            if (!compiled.containsKey(kvp.getKey().toString())) {
                final Optional<SoundEvent> evt = SoundLibrary.getSound(kvp.getKey());
                evt.ifPresent(e -> addAcoustic(e.getName(), new SimpleAcoustic(e)));
            }
        }

        final Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "acoustics.json");

        IResourceAccessor.process(configs, accessor -> {
            final AcousticCompiler compiler = new AcousticCompiler(accessor.location().getNamespace());
            final List<IAcoustic> acoustics = compiler.compile(accessor.asString());
            for (final IAcoustic a : acoustics) {
                addAcoustic(a.getName(), a);
            }
        });
    }

    @Nonnull
    public static IAcoustic resolve(@Nonnull final String namespace, @Nonnull String definition, @Nullable final Function<ResourceLocation, IAcoustic> specials) {

        // Reformat the definition to ensure proper sequencing, etc.
        definition = Arrays.stream(definition.toLowerCase().split(","))
                .map(frag -> AcousticLibrary.resolveResource(namespace, frag).toString())
                .sorted()
                .collect(Collectors.joining(","));

        IAcoustic result = compiled.get(definition);
        if (result == null) {
            result = parseDefinition(null, definition, specials);
        }
        compiled.put(definition, result);
        return result;
    }

    @Nonnull
    public static IAcoustic resolve(@Nonnull final ResourceLocation acousticName) {
        return resolve(acousticName, null);
    }

    @Nonnull
    private static IAcoustic parseDefinition(@Nullable ResourceLocation acousticName, @Nullable String definition, @Nullable final Function<ResourceLocation, IAcoustic> specials) {
        IAcoustic result;
        if (!StringUtils.isNullOrEmpty(definition)) {
            if (acousticName == null)
                acousticName = ADHOC;
            final String nameSpace = acousticName.getNamespace();
            final IAcoustic[] acoustics = Arrays.stream(definition.split(",")).map(fragment -> {
                // See if we have an acoustic for this fragment
                final ResourceLocation fragLoc = AcousticLibrary.resolveResource(nameSpace, fragment);
                IAcoustic a = null;
                if (specials != null)
                    a = specials.apply(fragLoc);
                if (a == null)
                    a = generateAcoustic(fragLoc);
                if (a == null)
                    SoundControl.LOGGER.warn("Acoustic '%s' not found!", fragment);
                return a;
            }).filter(Objects::nonNull).toArray(IAcoustic[]::new);

            if (acoustics.length == 0) {
                result = NullAcoustic.INSTANCE;
            } else if (acoustics.length == 1) {
                result = acoustics[0];
            } else {
                final SimultaneousAcoustic s = new SimultaneousAcoustic(acousticName);
                for (final IAcoustic t : acoustics)
                    s.add(t);
                s.trim();
                result = s;
            }
        } else {
            result = NullAcoustic.INSTANCE;
        }
        return result;
    }

    @Nonnull
    public static IAcoustic resolve(@Nonnull final ResourceLocation acousticName, @Nullable final String definition) {
        IAcoustic result = compiled.get(acousticName.toString());
        if (result == null) {
            result = parseDefinition(acousticName, definition, null);
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
        IAcoustic a = compiled.get(name.toString());
        if (a == null) {
            // Nope. Doesn't exist yet. It could be a sound name based on location.
            if (name.getPath().equals("not_emitter")) {
                a = NullAcoustic.INSTANCE;
            } else {
                final Optional<SoundEvent> evt = SoundLibrary.getSound(name);
                if (evt.isPresent())
                    a = generateAcoustic(evt.get());
            }
        }

        return a;
    }

    @Nonnull
    private static IAcoustic generateAcoustic(@Nonnull final SoundEvent evt) {
        IAcoustic result = compiled.get(evt.getName().toString());
        if (result == null) {
            result = new SimpleAcoustic(evt);
            addAcoustic(result.getName(), result);
        }
        return result;
    }
}

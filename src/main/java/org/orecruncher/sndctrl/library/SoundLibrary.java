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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.lib.JsonUtils;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.audio.SoundMetadata;
import org.orecruncher.sndctrl.library.config.SoundMetadataConfig;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles all things related to registered sounds.  This registry is kept in parallel with the Forge registeries.
 * The reason is that the Forge registries will change when sync'd with a server which pretty much hoses pure client
 * side mods that add their own sounds.  Also, extended data about sounds is maintained, like ownership and
 * attribution, that can be used when rendering tooltips and the like in a GUI.
 */
@OnlyIn(Dist.CLIENT)
public final class SoundLibrary {
    public static final SoundEvent MISSING = new SoundEvent(new ResourceLocation(SoundControl.MOD_ID, "missing_sound"));
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundLibrary.class);
    private static final Object2ObjectOpenHashMap<ResourceLocation, SoundEvent> myRegistry = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<ResourceLocation, SoundMetadata> soundMetadata = new Object2ObjectOpenHashMap<>();

    static {
        myRegistry.defaultReturnValue(SoundLibrary.MISSING);
        soundMetadata.defaultReturnValue(new SoundMetadata());
    }

    private SoundLibrary() {
    }

    public static void initialize() {

        // Initializes the internal sound registry once all the other mods have
        // registered their sounds.
        ForgeRegistries.SOUND_EVENTS.forEach(se -> myRegistry.put(se.getName(), se));

        // Gather up resource pack sound files and process them to ensure meta data is collected
        // and we become aware of configured sounds.  Resource pack sounds generally replace existing
        // registration, but this allows for new sounds to be added client side.
        final List<ResourceLocation> packs = ForgeUtils.getResourcePackIdList().stream().map(p -> new ResourceLocation(p, "sounds.json")).collect(Collectors.toList());;

        for (final ResourceLocation packId : packs) {
            registerSoundFile(packId);
        }
    }

    // Package internal!
    static Map<ResourceLocation, SoundEvent> getRegisteredSounds() {
        return myRegistry;
    }

    public static void registerSoundFile(@Nonnull final ResourceLocation soundFile) {
        final Map<String, SoundMetadataConfig> result = JsonUtils.loadConfig(soundFile, SoundMetadataConfig.class);
        if (result.size() > 0) {
            LOGGER.debug("Processing %s", soundFile);
            result.forEach((key, value) -> {
                if (!value.isDefault()) {
                    final SoundMetadata data = new SoundMetadata(value);
                    final ResourceLocation resource = new ResourceLocation(soundFile.getNamespace(), key);
                    soundMetadata.put(resource, data);
                    if (!myRegistry.containsKey(resource)) {
                        myRegistry.put(resource, new SoundEvent(resource));
                    }
                }
            });
        }
    }

    @Nonnull
    public static Optional<SoundEvent> getSound(@Nonnull final ResourceLocation sound) {
        Objects.requireNonNull(sound);
        return Optional.of(myRegistry.get(sound));
    }

    @Nonnull
    public static SoundMetadata getSoundMetadata(@Nonnull final ResourceLocation sound) {
        return soundMetadata.get(Objects.requireNonNull(sound));
    }

    @Nonnull
    public static ISoundCategory getSoundCategory(@Nonnull final ResourceLocation sound, @Nonnull final ISoundCategory defaultCategory) {
        return Utilities.firstNonNull(soundMetadata.get(Objects.requireNonNull(sound)).getCategory(), defaultCategory);
    }

}

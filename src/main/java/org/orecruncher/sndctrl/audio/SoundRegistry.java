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

package org.orecruncher.sndctrl.audio;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.lib.JsonUtils;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.config.SoundMetadataConfig;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles all things related to registered sounds.  This registry is kept in parallel with the Forge registeries.
 * The reason is that the Forge registries will change when sync'd with a server which pretty much hoses pure client
 * side mods that add their own sounds.  Also, extended data about sounds is maintained, like ownership and
 * attribution, that can be used when rendering tooltips and the like in a GUI.
 */
@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoundRegistry {
    public static final SoundEvent MISSING = new SoundEvent(new ResourceLocation(SoundControl.MOD_ID, "missing_sound"));
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundRegistry.class);
    private static final Object2ObjectOpenHashMap<ResourceLocation, SoundEvent> myRegistry = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<ResourceLocation, SoundMetadata> soundMetadata = new Object2ObjectOpenHashMap<>();

    static {
        myRegistry.defaultReturnValue(SoundRegistry.MISSING);
        soundMetadata.defaultReturnValue(new SoundMetadata());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.LOWEST, SoundRegistry::onSetup);
    }

    private SoundRegistry() {
    }

    private static void onSetup(@Nonnull final FMLClientSetupEvent event) {

        // Initializes the internal sound registry once all the other mods have
        // registered their sounds.
        ForgeRegistries.SOUND_EVENTS.forEach(se -> myRegistry.put(se.getName(), se));

        // Crawl through all the sound json files for all the resource packs gather metadata that will be used in the
        // tooltip display in the config GUI. A pack could add extra fields in the Json for things like author and
        // attribution.
        final List<ResourceLocation> packs =
                Stream.concat(
                        ForgeUtils.getModIdList().stream(),
                        ForgeUtils.getResourcePackIdList().stream()
                )
                .distinct()
                .map(e -> new ResourceLocation(e, "sounds.json"))
                .collect(Collectors.toList());

        for (final ResourceLocation packId : packs) {
            final Map<String, SoundMetadataConfig> result = JsonUtils.loadConfig(packId, SoundMetadataConfig.class);
            if (result.size() > 0) {
                LOGGER.debug("Processing %s", packId);
                result.forEach((key, value) -> {
                    if (!value.isDefault()) {
                        final SoundMetadata data = new SoundMetadata(value);
                        final ResourceLocation resource = new ResourceLocation(packId.getNamespace(), key);
                        soundMetadata.put(resource, data);
                    }
                });
            }
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

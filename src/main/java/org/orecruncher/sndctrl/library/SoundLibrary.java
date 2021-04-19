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

import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.resource.IResourceAccessor;
import org.orecruncher.lib.resource.ResourceUtils;
import org.orecruncher.lib.validation.MapValidator;
import org.orecruncher.lib.validation.Validators;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.audio.SoundMetadata;
import org.orecruncher.sndctrl.audio.handlers.SoundProcessor;
import org.orecruncher.sndctrl.config.Config;
import org.orecruncher.sndctrl.library.config.SoundMetadataConfig;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all things related to registered sounds.  This registry is kept in parallel with the Forge registeries.
 * The reason is that the Forge registries will change when sync'd with a server which pretty much hoses pure client
 * side mods that add their own sounds.  Also, extended data about sounds is maintained, like ownership and
 * attribution, that can be used when rendering tooltips and the like in a GUI.
 */
@OnlyIn(Dist.CLIENT)
public final class SoundLibrary {
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundLibrary.class);
    private static final ResourceLocation MISSING_RESOURCE = new ResourceLocation(SoundControl.MOD_ID, "missing_sound");
    private static final Object2ObjectOpenHashMap<ResourceLocation, SoundEvent> myRegistry = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<ResourceLocation, SoundMetadata> soundMetadata = new Object2ObjectOpenHashMap<>();
    private static final Type SOUND_FILE_TYPE = TypeToken.getParameterized(Map.class, String.class, SoundMetadataConfig.class).getType();

    public static final SoundEvent MISSING = new SoundEvent(MISSING_RESOURCE);

    static {
        myRegistry.defaultReturnValue(SoundLibrary.MISSING);
        soundMetadata.defaultReturnValue(new SoundMetadata());

        Validators.registerValidator(SOUND_FILE_TYPE, new MapValidator<String, SoundMetadataConfig>());
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
        final Collection<IResourceAccessor> soundFiles = ResourceUtils.findSounds();

        for (final IResourceAccessor file : soundFiles) {
            registerSoundFile(file);
        }

        LOGGER.info("Number of SoundEvents cached: %d", myRegistry.size());

        LOGGER.info("Individual Sound Configurations");
        LOGGER.info("===============================");
        getIndividualSoundConfig().forEach(cfg -> LOGGER.info(cfg.toString()));

        LOGGER.info("Category Occlusion");
        LOGGER.info("==================");
        Category.getCategories().forEach(c -> LOGGER.info("%s -> %s", c.getName(), c.doOcclusion()));
    }

    static List<IndividualSoundConfig> getIndividualSoundConfig() {
        return Config.CLIENT.sound.individualSounds.get()
                .stream()
                .map(IndividualSoundConfig::createFrom)
                .filter(Objects::nonNull)
                .filter(e -> !e.isDefault())
                .collect(Collectors.toList());
    }

    @Nonnull
    public static Collection<IndividualSoundConfig> getSortedSoundConfigurations() {

        final SortedMap<ResourceLocation, IndividualSoundConfig> map = new TreeMap<>();

        // Get a list of all the sounds and set to defaults.  Each gets it's own state in case caller
        // wants to manipulate.
        for (final Map.Entry<ResourceLocation, SoundEvent> kvp : myRegistry.entrySet()) {
            map.put(kvp.getKey(), new IndividualSoundConfig(kvp.getValue()));
        }

        // Override with the defaults from configuration.  Make a copy of the original so it doesn't change.
        getIndividualSoundConfig().forEach(cfg -> map.put(cfg.getLocation(), new IndividualSoundConfig(cfg)));

        final Comparator<IndividualSoundConfig> iscComparator = Comparator.comparing(isc -> isc.getLocation().toString());
        return map.values().stream().sorted(iscComparator).collect(Collectors.toList());
    }

    public static void updateSoundConfigurations(@Nonnull final Collection<IndividualSoundConfig> configs) {
        final List<String> items = configs.stream().map(Object::toString).collect(Collectors.toList());
        Config.CLIENT.sound.individualSounds.set(items);

        // Update our sound processor
        // TODO: The flow here is a bit weird
        SoundProcessor.applyConfig();
    }

    private static void registerSoundFile(@Nonnull final IResourceAccessor soundFile) {
        final Map<String, SoundMetadataConfig> result = soundFile.as(SOUND_FILE_TYPE);
        if (result != null && result.size() > 0) {
            ResourceLocation resource = soundFile.location();
            LOGGER.debug("Processing %s", resource);
            result.forEach((key, value) -> {
                // We want to register the sound regardless of having metadata.
                final ResourceLocation loc = new ResourceLocation(resource.getNamespace(), key);
                if (!myRegistry.containsKey(loc)) {
                    myRegistry.put(loc, new SoundEvent(loc));
                }
                if (!value.isDefault()) {
                    final SoundMetadata data = new SoundMetadata(value);
                    soundMetadata.put(loc, data);
                }
            });
        } else {
            LOGGER.debug("Skipping %s - unable to parse sound file or there are no sounds declared", soundFile.location());
        }
    }

    @Nonnull
    public static Optional<SoundEvent> getSound(@Nonnull final ResourceLocation sound) {
        Objects.requireNonNull(sound);
        final SoundEvent se = myRegistry.get(sound);
        if (se == MISSING) {
            SoundControl.LOGGER.warn("Unable to locate sound '%s'", sound.toString());
        }
        return Optional.of(se);
    }

    @SuppressWarnings("unused")
    @Nonnull
    public static SoundMetadata getSoundMetadata(@Nonnull final ResourceLocation sound) {
        return soundMetadata.get(Objects.requireNonNull(sound));
    }

    @Nonnull
    public static ISoundCategory getSoundCategory(@Nonnull final ResourceLocation sound, @Nonnull final ISoundCategory defaultCategory) {
        return Utilities.firstNonNull(soundMetadata.get(Objects.requireNonNull(sound)).getCategory(), defaultCategory);
    }

}

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

package org.orecruncher.sndctrl.audio.acoustic;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.Utilities;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.SoundBuilder;
import org.orecruncher.sndctrl.audio.SoundRegistry;
import org.orecruncher.sndctrl.audio.SoundUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public final class AcousticCompiler {

    // Defaults for the SoundBuilders that are created.  There will be slight variation to avoid repeated sound
    // plays as being too similar.
    public static final float DEFAULT_MIN_VOLUME = 0.9F;
    public static final float DEFAULT_MAX_VOLUME = 1.0F;
    public static final float DEFAULT_MIN_PITCH = 0.95F;
    public static final float DEFAULT_MAX_PITCH = 1.05F;
    public static final int DEFAULT_MIN_DELAY = 0;
    public static final int DEFAULT_MAX_DELAY = 0;

    private final static Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    @Nonnull
    private final String nameSpace;
    private Map<String, IDispatchHandler> handlers = new HashMap<>();
    private float minVolume;
    private float maxVolume;
    private float minPitch;
    private float maxPitch;
    private int minDelay;
    private int maxDelay;

    public AcousticCompiler() {
        this(null);
    }

    public AcousticCompiler(@Nullable final String defaultNamespace) {
        this.nameSpace = Utilities.firstNonNull(defaultNamespace, SoundControl.MOD_ID);

        handlers.put("simple", this::simpleHandler);
        handlers.put("delayed", this::delayedHandler);
        handlers.put("simultaneous", this::simultaneousHandler);
        handlers.put("probability", this::probabilityHandler);
        handlers.put("event", this::eventSelectorHandler);

        this.minVolume = DEFAULT_MIN_VOLUME;
        this.maxVolume = DEFAULT_MAX_VOLUME;
        this.minPitch = DEFAULT_MIN_PITCH;
        this.maxPitch = DEFAULT_MAX_PITCH;
        this.minDelay = DEFAULT_MIN_DELAY;
        this.maxDelay = DEFAULT_MAX_DELAY;
    }

    private static float getFloatSetting(@Nonnull final String name, @Nonnull final JsonObject obj, final float defaultValue) {
        if (obj.has(name)) {
            return obj.get(name).getAsFloat() / 100F;
        }
        return defaultValue;
    }

    private static int getIntSetting(@Nonnull final String name, @Nonnull final JsonObject obj, final int defaultValue) {
        if (obj.has(name)) {
            return obj.get(name).getAsInt();
        }
        return defaultValue;
    }

    public void setVolumeRange(final float min, final float max) {
        this.minVolume = min;
        this.maxVolume = max;
    }

    public void setPitchRange(final float min, final float max) {
        this.minPitch = min;
        this.maxPitch = max;
    }

    public void setDelayRange(final int min, final int max) {
        this.minDelay = min;
        this.maxDelay = max;
    }

    @Nonnull
    public List<IAcoustic> compile(@Nonnull final String acousticJson) {
        try {
            final JsonObject obj = gson.fromJson(acousticJson, JsonObject.class);
            return generateMap(obj);
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.warn("Unable to parse acoustic: %s", t.getMessage());
        }

        return ImmutableList.of();
    }

    @Nonnull
    private List<IAcoustic> generateMap(@Nonnull final JsonObject obj) {
        final List<IAcoustic> result = new ArrayList<>();
        final Set<Map.Entry<String, JsonElement>> acousticList = obj.entrySet();
        for (final Map.Entry<String, JsonElement> kvp : acousticList) {
            try {
                dispatch(kvp).ifPresent(result::add);
            } catch (@Nonnull final Throwable t) {
                SoundControl.LOGGER.error(t, "Unable to parse map acoustic '%s'='%s'", kvp.getKey(), kvp.getValue().toString());
            }
        }
        return result;
    }

    @Nonnull
    private List<IAcoustic> generateFromList(@Nonnull final JsonObject obj) {
        final List<IAcoustic> result = new ArrayList<>();
        final JsonArray array = obj.getAsJsonArray(Constants.ARRAY);
        if (array != null) {
            for (final JsonElement e : array) {
                try {
                    dispatch(new AbstractMap.SimpleEntry<>("", e)).ifPresent(result::add);
                } catch (@Nonnull final Throwable t) {
                    SoundControl.LOGGER.error(t, "Unable to parse array acoustic '%s'", e.toString());
                }
            }
        }
        return result;
    }

    @Nonnull
    private Optional<IAcoustic> dispatch(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        // If the element is a primitive it is probably just a sound name
        if (entry.getValue().isJsonPrimitive()) {
            return inlineHandler(entry);
        }

        final JsonObject obj = entry.getValue().getAsJsonObject();
        String typeName = "simple";
        if (obj.has(Constants.TYPE)) {
            typeName = obj.get(Constants.TYPE).getAsString();
        }

        final IDispatchHandler func = this.handlers.get(typeName);
        if (func == null)
            throw new AcousticException("Unknown acoustic type '%s'", typeName);
        return func.apply(entry);
    }

    /**
     * Handles the case where only a string is provided to define the sound.  This is interpreted as being a
     * sound resource location, and other properties such as volume and pitch will be default.
     */
    @Nonnull
    private Optional<IAcoustic> inlineHandler(@Nonnull final Map.Entry<String, JsonElement> entry) {
        final String sound = entry.getValue().getAsString();
        final ResourceLocation res = new ResourceLocation(sound);
        final SoundEvent evt = SoundRegistry.getSound(res).orElseThrow(IllegalStateException::new);
        final SoundBuilder builder = SoundBuilder.builder(evt, SoundCategory.NEUTRAL);
        return Optional.of(new SimpleAcoustic(entry.getKey(), new AcousticFactory(builder)));
    }

    @Nonnull
    private Optional<IAcoustic> simpleHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final AcousticFactory factory = new AcousticFactory(create(entry.getValue().getAsJsonObject()));
        return Optional.of(new SimpleAcoustic(entry.getKey(), factory));
    }

    @Nonnull
    private Optional<IAcoustic> delayedHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final JsonObject obj = entry.getValue().getAsJsonObject();
        final SoundBuilder builder = create(obj);
        final AcousticFactory factory = new AcousticFactory(builder);
        final DelayedAcoustic acoustic = new DelayedAcoustic(entry.getKey(), factory);
        if (obj.has(Constants.DELAY)) {
            acoustic.setDelay(getIntSetting(Constants.DELAY, obj, 0));
        } else {
            acoustic.setDelay(getIntSetting(Constants.MIN_DELAY, obj, this.minDelay));
            acoustic.setDelayMax(getIntSetting(Constants.MAX_DELAY, obj, this.maxDelay));
        }
        return Optional.of(acoustic);
    }

    @Nonnull
    private Optional<IAcoustic> simultaneousHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final List<IAcoustic> list = generateFromList(entry.getValue().getAsJsonObject());
        final SimultaneousAcoustic acoustic = new SimultaneousAcoustic(entry.getKey());
        for (final IAcoustic a : list)
            acoustic.add(a);
        return Optional.of(acoustic);
    }

    @Nonnull
    private Optional<IAcoustic> probabilityHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final List<IAcoustic> results = extractMap(entry.getValue());
        final ProbabilityAcoustic acoustic = new ProbabilityAcoustic(entry.getKey());
        for (final IAcoustic a : results) {
            final int weight = Integer.parseInt(a.getName());
            acoustic.add(a, weight);
        }

        return Optional.of(acoustic);
    }

    @Nonnull
    private Optional<IAcoustic> eventSelectorHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final List<IAcoustic> results = extractMap(entry.getValue());
        final EventSelectorAcoustic acoustic = new EventSelectorAcoustic(entry.getKey());
        for (final IAcoustic a : results) {
            final AcousticEvent evt = AcousticEvent.getEvent(a.getName());
            if (evt == null)
                throw new AcousticException("Unknown acoustic event '%s'", a.getName());
            acoustic.add(evt, a);
        }

        return Optional.of(acoustic);
    }

    @Nonnull
    private SoundBuilder create(@Nonnull final JsonObject obj) throws AcousticException {
        if (!obj.has(Constants.NAME))
            throw new AcousticException("Sound name property not found");

        final String soundName = obj.get(Constants.NAME).getAsString();
        if (StringUtils.isNullOrEmpty(soundName))
            throw new AcousticException("Invalid sound name '%s'", soundName);

        ResourceLocation res = null;

        if (soundName.charAt(0) == '@') {
            // Sound is in the Minecraft namespace
            res = new ResourceLocation("minecraft", soundName.substring(1));
        } else if (!soundName.contains(":")) {
            // It's just a path so assume the specified namespace
            res = new ResourceLocation(this.nameSpace, soundName);
        } else {
            // It's a fully qualified location
            res = new ResourceLocation(soundName);
        }

        final SoundEvent evt = SoundRegistry.getSound(res).orElse(SoundRegistry.MISSING);

        SoundCategory cat = null;
        if (obj.has(Constants.CATEGORY)) {
            cat = SoundUtils.getSoundCategory(obj.get(Constants.CATEGORY).getAsString());
        }

        if (cat == null) {
            cat = SoundRegistry.getSoundCategory(res, SoundCategory.NEUTRAL);
        }

        final SoundBuilder builder = SoundBuilder.builder(evt, cat);

        if (obj.has(Constants.PITCH)) {
            builder.setPitch(getFloatSetting(Constants.PITCH, obj, 1F));
        } else {
            float pitchMin = getFloatSetting(Constants.MIN_PITCH, obj, this.minPitch);
            float pitchMax = getFloatSetting(Constants.MAX_PITCH, obj, this.maxPitch);
            builder.setPitchRange(pitchMin, pitchMax);
        }

        if (obj.has(Constants.VOLUME)) {
            builder.setVolume(getFloatSetting(Constants.VOLUME, obj, 1F));
        } else {
            float volMin = getFloatSetting(Constants.MIN_VOLUME, obj, this.minVolume);
            float volMax = getFloatSetting(Constants.MAX_VOLUME, obj, this.maxVolume);
            builder.setVolumeRange(volMin, volMax);
        }

        return builder;
    }

    @Nonnull
    private List<IAcoustic> extractMap(@Nonnull final JsonElement element) throws AcousticException {
        final JsonObject obj = element.getAsJsonObject();
        if (!obj.has(Constants.MAP))
            throw new AcousticException("Sound configuration does not have a 'map' property defined");

        return generateMap(obj.get(Constants.MAP).getAsJsonObject());
    }

    @FunctionalInterface
    private interface IDispatchHandler {
        Optional<IAcoustic> apply(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException;
    }

    private static class Constants {
        public static final String TYPE = "_type";
        public static final String NAME = "name";
        public static final String CATEGORY = "category";
        public static final String MIN_PITCH = "pitch_min";
        public static final String MAX_PITCH = "pitch_max";
        public static final String PITCH = "pitch";
        public static final String MIN_VOLUME = "vol_min";
        public static final String MAX_VOLUME = "vol_max";
        public static final String VOLUME = "volume";
        public static final String MIN_DELAY = "delay_min";
        public static final String MAX_DELAY = "delay_max";
        public static final String DELAY = "delay";
        public static final String ARRAY = "array";
        public static final String MAP = "map";
    }
}

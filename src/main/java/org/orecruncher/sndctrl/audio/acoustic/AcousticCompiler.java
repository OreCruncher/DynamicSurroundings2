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
import net.minecraft.util.SoundEvent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.JsonUtils;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.api.acoustics.AcousticEvent;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.library.AcousticLibrary;
import org.orecruncher.sndctrl.library.SoundLibrary;

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

    public AcousticCompiler(@Nonnull final String defaultNamespace) {
        this.nameSpace = defaultNamespace;

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

    private static boolean getBoolSetting(@Nonnull final String name, @Nonnull final JsonObject obj, final boolean defaultValue) {
        if (obj.has(name)) {
            return obj.get(name).getAsBoolean();
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
    public static IAcoustic combine(@Nullable final IAcoustic... acoustics) {
        if (acoustics == null || acoustics.length == 0)
            return NullAcoustic.INSTANCE;

        if (acoustics.length == 1)
            return acoustics[0];

        final SimultaneousAcoustic result = new SimultaneousAcoustic(new ResourceLocation(SoundControl.MOD_ID, "adhoc"));
        Arrays.stream(acoustics).forEach(result::add);
        result.trim();
        return result;
    }

    @Nonnull
    public List<IAcoustic> compile(@Nonnull final String acousticJson) {
        try {
            final JsonObject obj = gson.fromJson(acousticJson, JsonObject.class);
            return generate(obj.entrySet());
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.warn("Unable to parse acoustic: %s", t.getMessage());
        }
        return ImmutableList.of();
    }

    @Nonnull
    public List<IAcoustic> compile(@Nonnull final ResourceLocation acousticFile) {
        try {
            final Map<String, JsonElement> acousticList = JsonUtils.loadConfig(acousticFile, JsonElement.class);
            return generate(acousticList.entrySet());
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.warn("Unable to parse acoustic: %s", t.getMessage());
        }
        return ImmutableList.of();
    }

    @Nonnull
    private List<IAcoustic> generate(@Nonnull final Set<Map.Entry<String, JsonElement>> set) {
        final List<IAcoustic> result = new ArrayList<>();
        for (final Map.Entry<String, JsonElement> kvp : set) {
            try {
                dispatch(kvp).ifPresent(result::add);
            } catch (@Nonnull final Throwable t) {
                SoundControl.LOGGER.error(t, "Unable to parse map acoustic '%s'='%s'", kvp.getKey(), kvp.getValue().toString());
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
    private Optional<IAcoustic> inlineHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final String sound = entry.getValue().getAsString();

        if (StringUtils.isNullOrEmpty(sound)) {
            return Optional.of(NullAcoustic.INSTANCE);
        }

        final ResourceLocation res = resolveResource(sound, null);
        final SoundEvent evt = SoundLibrary.getSound(res).orElseThrow(IllegalStateException::new);
        final ISoundCategory cat = SoundLibrary.getSoundCategory(evt.getName(), Category.AMBIENT);
        return Optional.of(new SimpleAcoustic(res, new AcousticFactory(evt, cat)));
    }

    @Nonnull
    private Optional<IAcoustic> simpleHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final AcousticFactory factory = create(entry.getValue().getAsJsonObject());
        final ResourceLocation acousticId;

        if (StringUtils.isNullOrEmpty(entry.getKey())) {
            acousticId = factory.getResourceName();
        } else {
            acousticId = resolveResource(entry.getKey(), null);
        }

        return Optional.of(new SimpleAcoustic(acousticId, factory));
    }

    @Nonnull
    private Optional<IAcoustic> delayedHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final JsonObject obj = entry.getValue().getAsJsonObject();
        final AcousticFactory factory = create(obj);
        final ResourceLocation acousticId;

        if (StringUtils.isNullOrEmpty(entry.getKey())) {
            acousticId = factory.getResourceName();
        } else {
            acousticId = resolveResource(entry.getKey(), null);
        }

        final DelayedAcoustic da = new DelayedAcoustic(acousticId, factory);

        if (obj.has(Constants.DELAY)) {
            da.setDelay(getIntSetting(Constants.DELAY, obj, 0));
        } else {
            da.setDelayMin(getIntSetting(Constants.MIN_DELAY, obj, this.minDelay));
            da.setDelayMax(getIntSetting(Constants.MAX_DELAY, obj, this.maxDelay));
        }

        return Optional.of(da);
    }

    @Nonnull
    private Optional<IAcoustic> simultaneousHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final ResourceLocation acousticId = resolveResource(entry.getKey(), "simultaneous");
        final SimultaneousAcoustic acoustic = new SimultaneousAcoustic(acousticId);
        final JsonArray array = entry.getValue().getAsJsonObject().getAsJsonArray(Constants.ARRAY);

        if (array == null || array.size() == 0) {
            throw new AcousticException("Simultaneous acoustic list is null or empty '%s'", entry.toString());
        }

        for (final JsonElement e : array) {
            try {
                dispatch(new AbstractMap.SimpleEntry<>("", e)).ifPresent(acoustic::add);
            } catch (@Nonnull final Throwable t) {
                SoundControl.LOGGER.error(t, "Unable to parse array acoustic '%s'", e.toString());
            }
        }

        acoustic.trim();
        return Optional.of(acoustic);
    }

    @Nonnull
    private Optional<IAcoustic> probabilityHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final ResourceLocation acousticId = resolveResource(entry.getKey(), "probablility");
        final ProbabilityAcoustic acoustic = new ProbabilityAcoustic(acousticId);
        final JsonArray array = entry.getValue().getAsJsonObject().getAsJsonArray(Constants.ARRAY);

        if (array == null || array.size() == 0 || (array.size() & 1) != 0) {
            throw new AcousticException("Probability acoustic is invalid '%s'", entry.toString());
        }

        final Iterator<JsonElement> itr = array.iterator();
        while (itr.hasNext()) {
            try {
                final JsonElement weight = itr.next();
                if (!weight.isJsonPrimitive()) {
                    throw new AcousticException("Expected weight value '%s'", weight.toString());
                }

                final JsonElement e = itr.next();
                dispatch(new AbstractMap.SimpleEntry<>("", e)).ifPresent(a -> acoustic.add(a, weight.getAsInt()));
            } catch (@Nonnull final Throwable t) {
                SoundControl.LOGGER.error(t, "Unable to parse probability acoustic");
            }
        }

        acoustic.trim();
        return Optional.of(acoustic);
    }

    @Nonnull
    private Optional<IAcoustic> eventSelectorHandler(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException {
        final ResourceLocation acousticId = resolveResource(entry.getKey(), "eventSelector");
        final EventSelectorAcoustic acoustic = new EventSelectorAcoustic(acousticId);
        final Set<Map.Entry<String, JsonElement>> entries = entry.getValue().getAsJsonObject().entrySet();
        for(final Map.Entry<String, JsonElement> e : entries) {
            // Skip the type entry
            if (e.getKey().equalsIgnoreCase(Constants.TYPE))
                continue;
            try {
                final AcousticEvent ae = AcousticEvent.getEvent(AcousticLibrary.resolveResource(this.nameSpace, e.getKey()));
                dispatch(e).ifPresent(a -> acoustic.add(ae, a));
            } catch (@Nonnull final Throwable t) {
                SoundControl.LOGGER.error(t, "Unable to parse event selector acoustic entry '%s'", e.toString());
            }
        }
        return Optional.of(acoustic);
    }

    @Nonnull
    private AcousticFactory create(@Nonnull final JsonObject obj) throws AcousticException {
        if (!obj.has(Constants.NAME))
            throw new AcousticException("Sound name property not found");

        final String soundName = obj.get(Constants.NAME).getAsString();
        if (StringUtils.isNullOrEmpty(soundName))
            throw new AcousticException("Invalid sound name '%s'", soundName);

        final ResourceLocation res = resolveResource(soundName, null);
        final SoundEvent evt = SoundLibrary.getSound(res).orElse(SoundLibrary.MISSING);

        ISoundCategory cat = null;
        if (obj.has(Constants.CATEGORY)) {
            cat = Category.getCategory(obj.get(Constants.CATEGORY).getAsString()).orElseThrow(() -> new AcousticException("Unknown sound category"));
        }

        if (cat == null) {
            cat = SoundLibrary.getSoundCategory(res, Category.NEUTRAL);
        }

        final AcousticFactory builder = new AcousticFactory(evt);
        builder.setCategory(cat);

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

        final boolean repeat = getBoolSetting(Constants.REPEATABLE, obj, false);
        if (repeat) {
            if (obj.has(Constants.REPEAT_DELAY)) {
                builder.setRepeatDelay(getIntSetting(Constants.REPEAT_DELAY, obj, 0));
            } else {
                int delayMin = getIntSetting(Constants.MIN_REPEAT_DELAY, obj, 0);
                int delayMax = getIntSetting(Constants.MAX_REPEAT_DELAY, obj, 0);
                builder.setRepeateDelayRange(delayMin, delayMax);
            }
        }

        final boolean global = getBoolSetting(Constants.GLOBAL, obj, false);
        builder.setGlobal(global);

        return builder;
    }

    @Nonnull
    private ResourceLocation resolveResource(@Nonnull final String name, @Nullable final String defaultName) throws AcousticException {
        String n = name;
        if (StringUtils.isNullOrEmpty(n))
            n = defaultName;
        if (StringUtils.isNullOrEmpty(n))
            throw new AcousticException("Sound name is null or empty");
        return AcousticLibrary.resolveResource(this.nameSpace, n);
    }

    @FunctionalInterface
    private interface IDispatchHandler {
        Optional<IAcoustic> apply(@Nonnull final Map.Entry<String, JsonElement> entry) throws AcousticException;
    }

    private static class Constants {
        public static final String TYPE = "type";
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
        public static final String REPEATABLE = "repeatable";
        public static final String REPEAT_DELAY = "repeat_delay";
        public static final String MIN_REPEAT_DELAY = "repeat_delay_min";
        public static final String MAX_REPEAT_DELAY = "repeat_delay_max";
        public static final String GLOBAL = "global";
        public static final String ARRAY = "array";
    }
}

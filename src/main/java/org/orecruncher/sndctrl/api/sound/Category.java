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

package org.orecruncher.sndctrl.api.sound;

import com.google.common.base.MoreObjects;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.sndctrl.audio.handlers.MusicFader;
import org.orecruncher.sndctrl.config.Config;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class Category implements ISoundCategory {
    // Mappings for easy searching
    private static final Map<String, ISoundCategory> nameToCategory = new HashMap<>();
    private static final Map<SoundCategory, ISoundCategory> categoryToNew = new IdentityHashMap<>();

    // Sound categories of the base Minecraft game
    public static final ISoundCategory MASTER = new SoundCategoryWrapper(SoundCategory.MASTER, () -> false);
    public static final ISoundCategory MUSIC = new FaderSoundCategoryWrapper(SoundCategory.MUSIC, () -> false);
    public static final ISoundCategory RECORDS = new FaderSoundCategoryWrapper(SoundCategory.RECORDS, Config.CLIENT.sound.occludeRecords::get);
    public static final ISoundCategory WEATHER = new SoundCategoryWrapper(SoundCategory.WEATHER, Config.CLIENT.sound.occludeWeather::get);
    public static final ISoundCategory BLOCKS = new SoundCategoryWrapper(SoundCategory.BLOCKS);
    public static final ISoundCategory HOSTILE = new SoundCategoryWrapper(SoundCategory.HOSTILE);
    public static final ISoundCategory NEUTRAL = new SoundCategoryWrapper(SoundCategory.NEUTRAL);
    public static final ISoundCategory PLAYERS = new SoundCategoryWrapper(SoundCategory.PLAYERS);
    public static final ISoundCategory AMBIENT = new SoundCategoryWrapper(SoundCategory.AMBIENT);
    public static final ISoundCategory VOICE = new SoundCategoryWrapper(SoundCategory.VOICE);

    public static final ISoundCategory CONFIG = new Category(
            "CONFIG",
            "sndctrl.soundcategory.config",
            () -> 1F,
            (v) -> {
            },
            () -> false) {
        @Override
        public boolean doQuickMenu() {
            return false;
        }
    };

    static {
        categoryToNew.put(SoundCategory.MASTER, MASTER);
        categoryToNew.put(SoundCategory.MUSIC, MUSIC);
        categoryToNew.put(SoundCategory.RECORDS, RECORDS);
        categoryToNew.put(SoundCategory.WEATHER, WEATHER);
        categoryToNew.put(SoundCategory.BLOCKS, BLOCKS);
        categoryToNew.put(SoundCategory.HOSTILE, HOSTILE);
        categoryToNew.put(SoundCategory.NEUTRAL, NEUTRAL);
        categoryToNew.put(SoundCategory.PLAYERS, PLAYERS);
        categoryToNew.put(SoundCategory.AMBIENT, AMBIENT);
        categoryToNew.put(SoundCategory.VOICE, VOICE);

        register(CONFIG);
    }

    private final String name;
    private final Supplier<Float> getter;
    private final Consumer<Float> setter;
    private final Supplier<Boolean> occlusion;
    private final String translationKey;

    /**
     * Creates a sound category instance with the specified name, and Supplier that gives the scaling
     * factor to apply.  These categories can be occluded.
     *
     * @param name           Name of the sound category.  Needs to be unique.
     * @param translationKey Language translation key for GUI display
     * @param scale          The supplier that gives the scaling factor to apply for the sound category.
     * @param setter         The consumer that allows configuration of the scale factor
     */
    public Category(@Nonnull final String name, @Nonnull final String translationKey, @Nonnull final Supplier<Float> scale, @Nonnull final Consumer<Float> setter) {
        this(name, translationKey, scale, setter, () -> true);
    }

    /**
     * Creates a sound category instance with the specified name, and Supplier that gives the scaling
     * factor to apply.
     *
     * @param name           Name of the sound category.  Needs to be unique.
     * @param translationKey Language translation key for GUI display
     * @param scale          The supplier that gives the scaling factor to apply for the sound category.
     * @param setter         The consumer that allows configuration of the scale factor
     * @param occlusion      Supplier that indicates if the category can be occluded
     */
    public Category(@Nonnull final String name, @Nonnull final String translationKey, @Nonnull final Supplier<Float> scale, @Nonnull final Consumer<Float> setter, @Nonnull final Supplier<Boolean> occlusion) {
        this.name = name;
        this.getter = scale;
        this.setter = setter;
        this.occlusion = occlusion;
        this.translationKey = translationKey;
    }

    /**
     * Resolves an ISoundCategory instance based on the name supplied.
     *
     * @param name The name of the sound category to retrieve
     * @return Instance of the named sound category if found; null otherwise
     */
    @Nonnull
    public static Optional<ISoundCategory> getCategory(@Nonnull final String name) {
        return Optional.ofNullable(nameToCategory.get(name));
    }

    /**
     * Resolves an ISoundCategory based on the supplied Minecraft SoundCategory instance.
     *
     * @param cat The SoundCategory to retrieve
     * @return Instance of the ISoundCategory that corresponds to the provided SoundCategory
     */
    @Nonnull
    public static Optional<ISoundCategory> getCategory(@Nonnull final SoundCategory cat) {
        return Optional.of(categoryToNew.get(cat));
    }

    /**
     * Resolves an ISoundCategory for the provided ISound instance.
     *
     * @param sound The sound for which the category is needed
     * @return ISoundCategory for the given sound
     */
    @Nonnull
    public static Optional<ISoundCategory> getCategory(@Nonnull final ISound sound) {
        if (sound.getCategory() == SoundCategory.RECORDS) {
            int x = 0;
        }
        if (sound instanceof ISoundInstance) {
            return Optional.of(((ISoundInstance) sound).getSoundCategory());
        }
        return getCategory(sound.getCategory());
    }

    /**
     * Registers an ISoundCategory instance with the system.  DO NOT CALL THIS METHOD.  Use the IMC registration
     * method if you need to register a sound category.  It handles it in a thread safe way.
     *
     * @param category The ISoundCategory instance to register
     */
    public static void register(@Nonnull final ISoundCategory category) {
        nameToCategory.put(category.getName(), category);
    }

    /**
     * Provides a list of ISoundCategory instances that are tagged for being displayable in a config menu.
     *
     * @return Collection of ISoundCategory instances to present in a configuration GUI.
     */
    public static Collection<ISoundCategory> getCategoriesForMenu() {
        final List<ISoundCategory> categories = new ArrayList<>();

        for (final Map.Entry<String, ISoundCategory> kvp : nameToCategory.entrySet()) {
            if (kvp.getValue().doQuickMenu())
                categories.add(kvp.getValue());
        }

        return categories;
    }

    @Override
    @Nonnull
    public String getName() {
        return this.name;
    }

    @Override
    @Nonnull
    public ITextComponent getTextComponent() {
        return new TranslationTextComponent(this.translationKey);
    }

    @Override
    public boolean doQuickMenu() {
        return true;
    }

    @Override
    public float getVolumeScale() {
        return this.getter.get();
    }

    @Override
    public void setVolumeScale(final float scale) {
        this.setter.accept(scale);
    }

    @Override
    public boolean doOcclusion() {
        return this.occlusion.get();
    }

    @Override
    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(getName()).add("scale", getVolumeScale()).toString();
    }

    private static class SoundCategoryWrapper implements ISoundCategory {
        private final SoundCategory category;
        private final Supplier<Boolean> occlusion;

        public SoundCategoryWrapper(@Nonnull final SoundCategory cat) {
            this(cat, () -> true);
        }

        public SoundCategoryWrapper(@Nonnull final SoundCategory cat, @Nonnull final Supplier<Boolean> occlusion) {
            this.category = cat;
            this.occlusion = occlusion;
            register(this);
        }

        @Override
        public String getName() {
            return this.category.getName();
        }

        @Override
        public ITextComponent getTextComponent() {
            // From the SoundConfig slider logic for Vanilla categories
            return new TranslationTextComponent("soundCategory." + this.getName());
        }

        @Override
        public float getVolumeScale() {
            return GameUtils.getGameSettings().getSoundLevel(this.category);
        }

        @Override
        public void setVolumeScale(final float scale) {
            GameUtils.getGameSettings().setSoundLevel(this.category, scale);
        }

        @Nonnull
        public SoundCategory getRealCategory() {
            return this.category;
        }

        @Override
        public boolean doOcclusion() {
            return this.occlusion.get();
        }

        @Override
        @Nonnull
        public String toString() {
            return MoreObjects.toStringHelper(this).addValue(getName()).add("scale", getVolumeScale()).toString();
        }
    }

    private static class FaderSoundCategoryWrapper extends SoundCategoryWrapper {

        public FaderSoundCategoryWrapper(@Nonnull final SoundCategory cat, @Nonnull final Supplier<Boolean> occlusion) {
            super(cat, occlusion);
        }

        public FaderSoundCategoryWrapper(@Nonnull final SoundCategory cat) {
            super(cat);
        }

        @Override
        public float getVolumeScale() {
            return super.getVolumeScale() * MusicFader.getMusicScaling();
        }
    }
}

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

import com.google.common.base.MoreObjects;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.sndctrl.audio.handlers.MusicFader;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class Category implements ISoundCategory {
    // Mappings for easy searching
    private static final Map<String, ISoundCategory> nameToCategory = new HashMap<>();
    private static final Map<SoundCategory, ISoundCategory> categoryToNew = new IdentityHashMap<>();

    // Sound categories of the base Minecraft game
    public static final ISoundCategory MASTER = new SoundCategoryWrapper(SoundCategory.MASTER);
    public static final ISoundCategory MUSIC = new FaderSoundCategoryWrapper(SoundCategory.MUSIC);
    public static final ISoundCategory RECORDS = new FaderSoundCategoryWrapper(SoundCategory.RECORDS);
    public static final ISoundCategory WEATHER = new SoundCategoryWrapper(SoundCategory.WEATHER);
    public static final ISoundCategory BLOCKS = new SoundCategoryWrapper(SoundCategory.BLOCKS);
    public static final ISoundCategory HOSTILE = new SoundCategoryWrapper(SoundCategory.HOSTILE);
    public static final ISoundCategory NEUTRAL = new SoundCategoryWrapper(SoundCategory.NEUTRAL);
    public static final ISoundCategory PLAYERS = new SoundCategoryWrapper(SoundCategory.PLAYERS);
    public static final ISoundCategory AMBIENT = new SoundCategoryWrapper(SoundCategory.AMBIENT);
    public static final ISoundCategory VOICE = new SoundCategoryWrapper(SoundCategory.VOICE);

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
    }

    @Nonnull
    private final String name;
    @Nonnull
    private final Supplier<Float> scaling;

    /**
     * Creates a sound category instance with the specified name, and Supplier that gives the scaling
     * factor to apply.
     *
     * @param name  Name of the sound category.  Needs to be unique.
     * @param scale The supplier that gives the scaling factor to apply for the sound category.
     */
    public Category(@Nonnull final String name, @Nonnull final Supplier<Float> scale) {
        this.name = name;
        this.scaling = scale;
    }

    @Nonnull
    public static Optional<ISoundCategory> getCategory(@Nonnull final String name) {
        return Optional.ofNullable(nameToCategory.get(name));
    }

    @Nonnull
    public static Optional<ISoundCategory> getCategory(@Nonnull final SoundCategory cat) {
        return Optional.of(categoryToNew.get(cat));
    }

    public static void register(@Nonnull final ISoundCategory category) {
        nameToCategory.put(category.getName(), category);
    }

    @Override
    @Nonnull
    public String getName() {
        return this.name;
    }

    @Override
    public float getVolumeScale() {
        return this.scaling.get();
    }

    @Override
    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(getName()).add("scale", getVolumeScale()).toString();
    }

    private static class SoundCategoryWrapper implements ISoundCategory {
        @Nonnull
        private final SoundCategory category;

        public SoundCategoryWrapper(@Nonnull final SoundCategory cat) {
            this.category = cat;
            register(this);
        }

        @Override
        public String getName() {
            return this.category.getName();
        }

        @Override
        public float getVolumeScale() {
            return GameUtils.getGameSettings().getSoundLevel(this.category);
        }

        @Nonnull
        public SoundCategory getRealCategory() {
            return this.category;
        }

        @Override
        @Nonnull
        public String toString() {
            return MoreObjects.toStringHelper(this).addValue(getName()).add("scale", getVolumeScale()).toString();
        }
    }

    private static class FaderSoundCategoryWrapper extends SoundCategoryWrapper {

        public FaderSoundCategoryWrapper(@Nonnull SoundCategory cat) {
            super(cat);
        }

        @Override
        public float getVolumeScale() {
            return super.getVolumeScale() * MusicFader.getMusicScaling();
        }
    }
}

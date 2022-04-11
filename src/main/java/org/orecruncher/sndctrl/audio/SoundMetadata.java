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

package org.orecruncher.sndctrl.audio;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.library.config.SoundMetadataConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SoundMetadata {

    private static final ITextComponent NO_STRING = StringTextComponent.EMPTY;

    private final ITextComponent title;
    private final ITextComponent caption;
    private final ISoundCategory category;
    private final List<ITextComponent> credits;

    public SoundMetadata() {
        this.title = NO_STRING;
        this.caption = NO_STRING;
        this.category = Category.NEUTRAL;
        this.credits = ImmutableList.of();
    }

    public SoundMetadata(@Nonnull final SoundMetadataConfig cfg) {
        Objects.requireNonNull(cfg);

        this.title = StringUtils.isEmpty(cfg.title) ? NO_STRING : new TranslationTextComponent(cfg.title);
        this.caption = StringUtils.isEmpty(cfg.caption) ? NO_STRING : new TranslationTextComponent(cfg.caption);
        this.category = Category.getCategory(cfg.category).orElse(Category.NEUTRAL);

        if (cfg.credits == null || cfg.credits.size() == 0) {
            this.credits = ImmutableList.of();
        } else {
            this.credits = new ArrayList<>();
            for (final String s : cfg.credits) {
                if (StringUtils.isEmpty(s))
                    this.credits.add(NO_STRING);
                else
                    this.credits.add(new StringTextComponent(s));
            }
        }
    }

    /**
     * Gets the title configured in sounds.json, or EMPTY if not present.
     *
     * @return Configured title, or EMPTY if not present.
     */
    @Nonnull
    public ITextComponent getTitle() {
        return this.title;
    }

    /**
     * Gets the caption (subtitle) configured in sounds.json, or EMPTY if not present.
     *
     * @return Configured caption, or EMPTY if not present.
     */
    @Nonnull
    public ITextComponent getCaption() {
        return this.caption;
    }

    /**
     * Gets the credits configured for the sound event in sounds.json, or an empty list if not present.
     *
     * @return List containing 0 or more strings describing the sound credits.
     */
    @Nonnull
    public List<ITextComponent> getCredits() {
        return this.credits;
    }

    /**
     * Gets the SoundCategory configured in the underlying sounds.json.  Note that this property has been
     * deprecated by Mojang, but it is kept for configuring Dynamic Surroundings sounds.
     *
     * @return SoundCategory specified in the sound event metadata, or null if not present.
     */
    @Nonnull
    public ISoundCategory getCategory() {
        return this.category;
    }

}

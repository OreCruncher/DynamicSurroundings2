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

import com.google.common.collect.ImmutableList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.sndctrl.audio.config.SoundMetadataConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public final class SoundMetadata {

    private static final TextComponent NO_STRING = new StringTextComponent(StringUtils.EMPTY);

    @Nonnull
    private final TextComponent title;
    @Nonnull
    private final TextComponent caption;
    @Nullable
    private final SoundCategory category;
    @Nonnull
    private final List<TextComponent> credits;

    SoundMetadata() {
        this.title = NO_STRING;
        this.caption = NO_STRING;
        this.category = null;
        this.credits = ImmutableList.of();
    }

    SoundMetadata(@Nonnull final SoundMetadataConfig cfg) {
        Objects.requireNonNull(cfg);

        this.title = StringUtils.isEmpty(cfg.title) ? NO_STRING : new TranslationTextComponent(cfg.title);
        this.caption = StringUtils.isEmpty(cfg.caption) ? NO_STRING : new TranslationTextComponent(cfg.caption);
        this.category = SoundUtils.getSoundCategory(cfg.category);

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
    public TextComponent getTitle() {
        return this.title;
    }

    /**
     * Gets the caption (subtitle) configured in sounds.json, or EMPTY if not present.
     *
     * @return Configured caption, or EMPTY if not present.
     */
    @Nonnull
    public TextComponent getCaption() {
        return this.caption;
    }

    /**
     * Gets the credits configured for the sound evnt in sounds.json, or an empty list if not present.
     *
     * @return List containing 0 or more strings describing the sound credits.
     */
    @Nonnull
    public List<TextComponent> getCredits() {
        return this.credits;
    }

    /**
     * Gets the SoundCategory configured in the underlying sounds.json.  Note that this property has been
     * deprecated by Mojang, but it is kept for configuring Dynamic Surroundings sounds.
     *
     * @return SoundCategory specified in the sound event metadata, or null if not present.
     */
    @Nullable
    public SoundCategory getCategory() {
        return this.category;
    }

}

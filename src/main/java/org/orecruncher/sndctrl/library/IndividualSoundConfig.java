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

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.math.MathStuff;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public final class IndividualSoundConfig {

    private static final String BLOCK_TOKEN = "block";
    private static final String CULL_TOKEN = "cull";
    private static final String DELIMITER = " ";

    public static final int DEFAULT_VOLUME_SCALE = 100;
    public static final int VOLUME_SCALE_MIN = 0;
    public static final int VOLUME_SCALE_MAX = 400;

    private final ResourceLocation location;
    private boolean isBocked;
    private boolean isCulled;
    private int volumeScale;

    public IndividualSoundConfig(@Nonnull final ResourceLocation location) {
        this(location, false, false, DEFAULT_VOLUME_SCALE);
    }

    public IndividualSoundConfig(@Nonnull final ResourceLocation location, final boolean isBlocked, final boolean isCulled, final int volumeScale) {
        this.location = location;
        this.isBocked = isBlocked;
        this.isCulled = isCulled;
        this.volumeScale = MathStuff.clamp(volumeScale, VOLUME_SCALE_MIN, VOLUME_SCALE_MAX);
    }

    public IndividualSoundConfig(@Nonnull final IndividualSoundConfig source) {
        this.location = source.location;
        this.isBocked = source.isBocked;
        this.isCulled = source.isCulled;
        this.volumeScale = source.volumeScale;
    }

    public static boolean isValid(@Nonnull final String text) {
        // Just a simple see if it parses check
        return createFrom(text) != null;
    }

    // This check enforces that the text contains a : to avoid matching the "minecraft" namespace by default
    private static boolean isResourceNameValid(@Nonnull final String text) {
        if (!text.contains(":"))
            return false;
        final ResourceLocation loc = ResourceLocation.tryCreate(text);
        if (loc == null)
            return false;
        return loc.getPath().length() > 0;
    }

    @Nullable
    public static IndividualSoundConfig createFrom(@Nonnull final String entry) {
        IndividualSoundConfig result = null;
        final String[] parts = entry.split(DELIMITER);
        if (parts.length == 0 || parts.length > 4) {
            return null;
        } else {
            if (isResourceNameValid(parts[0])) {
                final ResourceLocation res = new ResourceLocation(parts[0]);
                boolean isCulled = false;
                boolean isBlocked = false;
                int volumeControl = DEFAULT_VOLUME_SCALE;

                for (int i = 1; i < parts.length; i++) {
                    if (CULL_TOKEN.compareToIgnoreCase(parts[i]) == 0) {
                        isCulled = true;
                    } else if (BLOCK_TOKEN.compareToIgnoreCase(parts[i]) == 0) {
                        isBlocked = true;
                    } else {
                        try {
                            volumeControl = MathStuff.clamp(Integer.parseInt(parts[i]), VOLUME_SCALE_MIN, VOLUME_SCALE_MAX);
                        } catch (final Throwable t) {
                            // Can't parse the token - bad entry
                            return null;
                        }
                    }
                }

                result = new IndividualSoundConfig(res, isBlocked, isCulled, volumeControl);
            }
        }

        return result;
    }

    @Nonnull
    public ResourceLocation getLocation() {
        return this.location;
    }

    public boolean isDefault() {
        return !(this.isBocked || this.isCulled || volumeScale != DEFAULT_VOLUME_SCALE);
    }

    public boolean isBlocked() {
        return this.isBocked;
    }

    public void setIsBlocked(final boolean flag) {
        this.isBocked = flag;
    }

    public boolean isCulled() {
        return this.isCulled;
    }

    public void setIsCulled(final boolean flag) {
        this.isCulled = flag;
    }

    public float getVolumeScale() {
        return this.volumeScale;
    }

    public int getVolumeScaleInt() {
        return this.volumeScale;
    }

    public void setVolumeScaleInt(final int scale) {
        this.volumeScale = MathStuff.clamp(scale, VOLUME_SCALE_MIN, VOLUME_SCALE_MAX);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.location.toString());
        if (this.isBocked)
            builder.append(DELIMITER).append(BLOCK_TOKEN);
        if (this.isCulled)
            builder.append(DELIMITER).append(CULL_TOKEN);
        if (this.volumeScale != DEFAULT_VOLUME_SCALE)
            builder.append(DELIMITER).append(this.volumeScale);
        return builder.toString();
    }
}

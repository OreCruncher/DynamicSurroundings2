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
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.sndctrl.SoundControl;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public final class IndividualSoundConfig {

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(IndividualSoundConfig.class);

    private static final String BLOCK_TOKEN = "block";
    private static final String CULL_TOKEN = "cull";

    private final ResourceLocation location;
    private boolean isBocked;
    private boolean isCulled;
    private int volumeScale;

    public IndividualSoundConfig(@Nonnull final ResourceLocation location) {
        this(location, false, false, 100);
    }

    public IndividualSoundConfig(@Nonnull final ResourceLocation location, final boolean isBlocked, final boolean isCulled, final int volumeScale) {
        this.location = location;
        this.isBocked = isBlocked;
        this.isCulled = isCulled;
        this.volumeScale = MathStuff.clamp(volumeScale, 0, 400);
    }

    public IndividualSoundConfig(@Nonnull final IndividualSoundConfig source) {
        this.location = source.location;
        this.isBocked = source.isBocked;
        this.isCulled = source.isCulled;
        this.volumeScale = source.volumeScale;
    }

    public static IndividualSoundConfig createFrom(@Nonnull final String entry) {
        IndividualSoundConfig result = null;
        final String[] parts = entry.split(" ");
        if (parts.length == 0) {
            return null;
        } else {
            if (ResourceLocation.isResouceNameValid(parts[0])) {
                final ResourceLocation res = new ResourceLocation(parts[0]);
                boolean isCulled = false;
                boolean isBlocked = false;
                int volumeControl = 100;

                for (int i = 1; i < parts.length; i++) {
                    if (CULL_TOKEN.compareToIgnoreCase(parts[i]) == 0) {
                        isCulled = true;
                    } else if (BLOCK_TOKEN.compareToIgnoreCase(parts[i]) == 0) {
                        isBlocked = true;
                    } else {
                        try {
                            volumeControl = Integer.parseInt(parts[i]);
                        } catch (final Throwable t) {
                            LOGGER.warn("Unrecognized token '%s' (%s)", parts[i], entry);
                            return null;
                        }
                    }
                }

                result = new IndividualSoundConfig(res, isBlocked, isCulled, volumeControl);

            } else {
                LOGGER.warn("Resource name '%s' is invalid (%s)", parts[0], entry);
            }
        }

        return result;
    }

    @Nonnull
    public ResourceLocation getLocation() {
        return this.location;
    }

    public boolean isDefault() {
        return !(this.isBocked || this.isCulled || volumeScale != 100);
    }

    public boolean isBocked() {
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

    public void setVolumeScale(final int scale) {
        this.volumeScale = MathStuff.clamp(scale, 0, 400);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.location.toString());
        if (this.isBocked)
            builder.append(" ").append(BLOCK_TOKEN);
        if (this.isCulled)
            builder.append(" ").append(CULL_TOKEN);
        if (this.volumeScale != 100)
            builder.append(" ").append(this.volumeScale);
        return builder.toString();
    }
}

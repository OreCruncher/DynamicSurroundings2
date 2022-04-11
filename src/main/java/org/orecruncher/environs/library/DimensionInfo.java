/*
 *  Dynamic Surroundings
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.library;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import org.orecruncher.environs.config.Config;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.library.config.DimensionConfig;
import org.orecruncher.lib.WorldUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DimensionInfo {

    private static final int SPACE_HEIGHT_OFFSET = 32;

    public static final  DimensionInfo NONE = new DimensionInfo();

    // Attributes about the dimension. This is information is loaded from local configs.
    protected ResourceLocation name;
    protected int seaLevel;
    protected int skyHeight;
    protected int cloudHeight;
    protected int spaceHeight;
    protected boolean hasHaze = false;
    protected boolean hasAuroras = false;
    protected boolean hasFog = false;
    protected boolean alwaysOutside = false;
    protected boolean playBiomeSounds = true;

    protected final boolean isFlatWorld;

    DimensionInfo() {
        this.name = new ResourceLocation(Environs.MOD_ID, "no_dimension");
        this.isFlatWorld = false;
    }

    public DimensionInfo(@Nonnull final World world, @Nullable final DimensionConfig dimConfig) {
        // Attributes that come from the world object itself. Set now because the config may override.
        DimensionType dt = world.getDimensionType();
        this.name = world.getDimensionKey().getLocation();
        this.seaLevel = world.getSeaLevel();
        this.skyHeight = world.getHeight();
        this.cloudHeight = this.skyHeight;
        this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;
        this.isFlatWorld = WorldUtils.isSuperFlat(world);

        if (dt.isNatural() && dt.hasSkyLight()) {
            this.hasAuroras = true;
            this.hasFog = true;
        }

        // Force sea level based on known world types that give heartburn
        if (this.isFlatWorld)
            this.seaLevel = 0;
        else if (dt.isNatural() && Config.CLIENT.biome.worldSealevelOverride.get() > 0)
            this.seaLevel = Config.CLIENT.biome.worldSealevelOverride.get();

        if (Config.CLIENT.biome.biomeSoundBlacklist.get().contains(this.name.toString()))
            this.playBiomeSounds = false;

        // Override based on player config settings
        if (dimConfig != null) {
            if (dimConfig.seaLevel != null)
                this.seaLevel = dimConfig.seaLevel;
            if (dimConfig.skyHeight != null)
                this.skyHeight = dimConfig.skyHeight;
            if (dimConfig.hasHaze != null)
                this.hasHaze = dimConfig.hasHaze;
            if (dimConfig.hasAurora != null)
                this.hasAuroras = dimConfig.hasAurora;
            if (dimConfig.cloudHeight != null)
                this.cloudHeight = dimConfig.cloudHeight;
            else
                this.cloudHeight = this.hasHaze ? this.skyHeight / 2 : this.skyHeight;
            if (dimConfig.hasFog != null)
                this.hasFog = dimConfig.hasFog;
            if (dimConfig.alwaysOutside != null)
                this.alwaysOutside = dimConfig.alwaysOutside;

            this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;
        }
    }

    @Nonnull
    public ResourceLocation getName() {
        return this.name;
    }

    public int getSeaLevel() {
        return this.seaLevel;
    }

    public int getSkyHeight() {
        return this.skyHeight;
    }

    public int getCloudHeight() {
        return this.cloudHeight;
    }

    public int getSpaceHeight() {
        return this.spaceHeight;
    }

    public boolean hasHaze() {
        return this.hasHaze;
    }

    public boolean hasAuroras() {
        return this.hasAuroras;
    }

    public boolean hasFog() {
        return this.hasFog;
    }

    public boolean playBiomeSounds() {
        return this.playBiomeSounds;
    }

    public boolean alwaysOutside() {
        return this.alwaysOutside;
    }

    public boolean isFlatWorld() {
        return this.isFlatWorld;
    }

}

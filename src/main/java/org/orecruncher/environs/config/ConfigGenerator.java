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

package org.orecruncher.environs.config;

import me.shedaniel.clothconfig2.forge.api.ConfigBuilder;
import me.shedaniel.clothconfig2.forge.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.forge.impl.builders.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.config.ClothAPIFactory;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class ConfigGenerator {

    @Nonnull
    public static SubCategoryBuilder generate(@Nonnull final ConfigBuilder builder, @Nonnull final ConfigEntryBuilder entryBuilder) {

        SubCategoryBuilder modCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.modname", false);

        SubCategoryBuilder subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.logging", false);
        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.logging.enableLogging));

        subCategory.add(
                ClothAPIFactory.createInteger(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.logging.flagMask,
                        0,
                        Integer.MAX_VALUE));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.biomes", false);
        subCategory.add(
                ClothAPIFactory.createInteger(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.biome.worldSealevelOverride,
                        0,
                        Integer.MAX_VALUE));

        subCategory.add(
                ClothAPIFactory.createStringList(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.biome.biomeSoundBlacklist,
                        null));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.effects", false);
        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.enableFireFlies));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.enableSteamJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.enableFireJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.enableBubbleJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.enableDustJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.enableFountainJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.enableWaterSplashJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.disableUnderwaterParticles));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.aurora", false);
        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.aurora.auroraEnabled));

        subCategory.add(
                ClothAPIFactory.createIntegerSlider(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.aurora.maxBands,
                        1,
                        3));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.fog", false);
        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.fog.enableFog));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.fog.enableBiomeFog));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.fog.enableElevationHaze));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.fog.enableMorningFog));

        subCategory.add(
                ClothAPIFactory.createInteger(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.fog.morningFogChance,
                        0,
                        Integer.MAX_VALUE));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.fog.enableBedrockFog));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.fog.enableWeatherFog));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.sound", false);
        subCategory.add(
                ClothAPIFactory.createIntegerSlider(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.sound.biomeSoundVolume,
                        0,
                        100));

        subCategory.add(
                ClothAPIFactory.createIntegerSlider(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.sound.spotSoundVolume,
                        0,
                        100));

        modCategory.add(subCategory.build());

        return modCategory;
    }
}

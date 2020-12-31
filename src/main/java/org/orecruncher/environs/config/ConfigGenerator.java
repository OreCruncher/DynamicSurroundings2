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

import com.google.common.collect.ImmutableList;
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
        BooleanToggleBuilder boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.logging.EnableDebug",
                false,
                Config.CLIENT.logging.enableLogging);
        subCategory.add(boolBuilder.build());

        IntFieldBuilder intBuilder = ClothAPIFactory.createInteger(
                builder,
                "environs.cfg.logging.FlagMask",
                0,
                Config.CLIENT.logging.flagMask,
                0,
                Integer.MAX_VALUE);
        subCategory.add(intBuilder.build());

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.biomes", false);
        intBuilder = ClothAPIFactory.createInteger(
                builder,
                "environs.cfg.biomes.Sealevel",
                0,
                Config.CLIENT.biome.worldSealevelOverride,
                0,
                Integer.MAX_VALUE);
        subCategory.add(intBuilder.build());

        StringListBuilder strListBuilder = ClothAPIFactory.createStringList(
                builder,
                "environs.cfg.biomes.DimBlacklist",
                ImmutableList.of(),
                Config.CLIENT.biome.biomeSoundBlacklist);
        subCategory.add(strListBuilder.build());

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.effects", false);
        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.effects.Fireflies",
                true,
                Config.CLIENT.effects.enableFireFlies);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.effects.Steam",
                true,
                Config.CLIENT.effects.enableSteamJets);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.effects.Fire",
                true,
                Config.CLIENT.effects.enableFireJets);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.effects.Bubble",
                true,
                Config.CLIENT.effects.enableBubbleJets);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.effects.Dust",
                true,
                Config.CLIENT.effects.enableDustJets);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.effects.Fountain",
                true,
                Config.CLIENT.effects.enableFountainJets);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.effects.Splash",
                true,
                Config.CLIENT.effects.enableWaterSplashJets);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.effects.Underwater",
                false,
                Config.CLIENT.effects.disableUnderwaterParticles);
        subCategory.add(boolBuilder.build());

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.aurora", false);
        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.aurora.Enable",
                false,
                Config.CLIENT.aurora.auroraEnabled);
        subCategory.add(boolBuilder.build());

        IntSliderBuilder intSliderBuilder = ClothAPIFactory.createIntegerSlider(
                builder,
                "environs.cfg.aurora.MaxBands",
                3,
                Config.CLIENT.aurora.maxBands,
                1,
                3);
        subCategory.add(intSliderBuilder.build());

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.fog", false);
        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.fog.Enable",
                true,
                Config.CLIENT.fog.enableFog);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.fog.Biome",
                true,
                Config.CLIENT.fog.enableBiomeFog);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.fog.Haze",
                true,
                Config.CLIENT.fog.enableElevationHaze);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.fog.Morning",
                true,
                Config.CLIENT.fog.enableMorningFog);
        subCategory.add(boolBuilder.build());

        intBuilder = ClothAPIFactory.createInteger(
                builder,
                "environs.cfg.fog.MorningChance",
                1,
                Config.CLIENT.fog.morningFogChance,
                0,
                Integer.MAX_VALUE);
        subCategory.add(intBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.fog.Bedrock",
                true,
                Config.CLIENT.fog.enableBedrockFog);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "environs.cfg.fog.Weather",
                true,
                Config.CLIENT.fog.enableWeatherFog);
        subCategory.add(boolBuilder.build());

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.sound", false);
        intSliderBuilder = ClothAPIFactory.createIntegerSlider(
                builder,
                "environs.cfg.sound.BiomeVolume",
                100,
                Config.CLIENT.sound.biomeSoundVolume,
                0,
                100);
        subCategory.add(intSliderBuilder.build());

        intSliderBuilder = ClothAPIFactory.createIntegerSlider(
                builder,
                "environs.cfg.sound.SpotVolume",
                100,
                Config.CLIENT.sound.spotSoundVolume,
                0,
                100);
        subCategory.add(intSliderBuilder.build());

        modCategory.add(subCategory.build());

        return modCategory;
    }
}

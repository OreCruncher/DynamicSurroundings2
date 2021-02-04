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
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.effects.particles.RippleStyle;
import org.orecruncher.lib.config.ClothAPIFactory;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class ConfigGenerator {

    @Nonnull
    public static SubCategoryBuilder generate(@Nonnull final ConfigBuilder builder, @Nonnull final ConfigEntryBuilder entryBuilder) {

        SubCategoryBuilder modCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.modname", TextFormatting.GOLD, false);

        SubCategoryBuilder subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.logging", TextFormatting.YELLOW, false);
        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.logging.enableLogging));

        subCategory.add(
                ClothAPIFactory.createInteger(
                        builder,
                        Config.CLIENT.logging.flagMask));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.biomes", TextFormatting.YELLOW, false);
        subCategory.add(
                ClothAPIFactory.createInteger(
                        builder,
                        Config.CLIENT.biome.worldSealevelOverride));

        subCategory.add(
                ClothAPIFactory.createStringList(
                        builder,
                        Config.CLIENT.biome.biomeSoundBlacklist,
                        null));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.effects", TextFormatting.YELLOW, false);
        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.effects.enableFireFlies));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.effects.enableSteamJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.effects.enableFireJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.effects.enableBubbleJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.effects.enableDustJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.effects.enableFountainJets));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.effects.enableWaterfalls));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.effects.disableUnderwaterParticles));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.effects.enableWaterRipples));

        subCategory.add(
                ClothAPIFactory.createEnumList(
                        builder,
                        RippleStyle.class,
                        Config.CLIENT.effects.waterRippleStyle));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.aurora", TextFormatting.YELLOW, false);
        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.aurora.auroraEnabled));

        subCategory.add(
                ClothAPIFactory.createIntegerSlider(
                        builder,
                        Config.CLIENT.aurora.maxBands));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.fog", TextFormatting.YELLOW, false);
        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.fog.enableFog));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.fog.enableBiomeFog));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.fog.enableElevationHaze));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.fog.enableMorningFog));

        subCategory.add(
                ClothAPIFactory.createInteger(
                        builder,
                        Config.CLIENT.fog.morningFogChance));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.fog.enableBedrockFog));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.fog.enableWeatherFog));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "environs.cfg.sound", TextFormatting.YELLOW, false);
        subCategory.add(
                ClothAPIFactory.createIntegerSlider(
                        builder,
                        Config.CLIENT.sound.biomeSoundVolume));

        subCategory.add(
                ClothAPIFactory.createIntegerSlider(
                        builder,
                        Config.CLIENT.sound.spotSoundVolume));

        subCategory.add(
                ClothAPIFactory.createIntegerSlider(
                        builder,
                        Config.CLIENT.sound.waterfallSoundVolume));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.CLIENT.sound.occludeWaterfall));

        modCategory.add(subCategory.build());

        return modCategory;
    }
}

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

package org.orecruncher.sndctrl;

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

        SubCategoryBuilder modCategory = ClothAPIFactory.createSubCategory(entryBuilder, "sndctrl.modname", false);

        SubCategoryBuilder subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "sndctrl.cfg.logging", false);
        BooleanToggleBuilder boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "sndctrl.cfg.logging.EnableDebug",
                false,
                Config.CLIENT.logging.enableLogging);
        subCategory.add(boolBuilder.build());

        IntFieldBuilder intBuilder = ClothAPIFactory.createInteger(
                builder,
                "sndctrl.cfg.logging.FlagMask",
                0,
                Config.CLIENT.logging.flagMask,
                0,
                Integer.MAX_VALUE);
        subCategory.add(intBuilder.build());

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "sndctrl.cfg.sound", false);
        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "sndctrl.cfg.sound.EnhancedSounds",
                true,
                Config.CLIENT.sound.enableEnhancedSounds).requireRestart();
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "sndctrl.cfg.sound.Occlusion",
                true,
                Config.CLIENT.sound.enableOcclusionCalcs).requireRestart();
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "sndctrl.cfg.sound.MonoConversion",
                true,
                Config.CLIENT.sound.enableMonoConversion);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "sndctrl.cfg.sound.EnhancedWeather",
                true,
                Config.CLIENT.sound.enhancedWeather);
        subCategory.add(boolBuilder.build());

        intBuilder = ClothAPIFactory.createInteger(
                builder,
                "sndctrl.cfg.sound.CullInterval",
                20,
                Config.CLIENT.sound.cullInterval,
                0,
                Integer.MAX_VALUE);
        subCategory.add(intBuilder.build());

        IntSliderBuilder intSliderBuilder = ClothAPIFactory.createIntegerSlider(
                builder,
                "sndctrl.cfg.sound.Threads",
                0,
                Config.CLIENT.sound.backgroundThreadWorkers,
                0,
                8).requireRestart();
        subCategory.add(intSliderBuilder.build());

        StringListBuilder strListBuilder = ClothAPIFactory.createStringList(
                builder,
                "sndctrl.cfg.sound.Individual",
                Config.Client.defaultSoundConfig,
                Config.CLIENT.sound.individualSounds);
        subCategory.add(strListBuilder.build());

        strListBuilder = ClothAPIFactory.createStringList(
                builder,
                "sndctrl.cfg.sound.StartupSounds",
                Config.Client.defaultStartupSounds,
                Config.CLIENT.sound.startupSoundList);
        subCategory.add(strListBuilder.build());

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "sndctrl.cfg.effects", false);
        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "sndctrl.cfg.effects.Randoms",
                true,
                Config.CLIENT.effects.fixupRandoms).requireRestart();
        subCategory.add(boolBuilder.build());

        intSliderBuilder = ClothAPIFactory.createIntegerSlider(
                builder,
                "sndctrl.cfg.effects.BlockRange",
                24,
                Config.CLIENT.effects.effectRange,
                16,
                64);
        subCategory.add(intSliderBuilder.build());

        modCategory.add(subCategory.build());

        return modCategory;
    }
}

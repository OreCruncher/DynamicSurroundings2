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

package org.orecruncher.mobeffects.config;

import me.shedaniel.clothconfig2.forge.api.ConfigBuilder;
import me.shedaniel.clothconfig2.forge.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.forge.impl.builders.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.config.ClothAPIFactory;
import org.orecruncher.mobeffects.footsteps.FootprintStyle;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class ConfigGenerator {

    @Nonnull
    public static SubCategoryBuilder generate(@Nonnull final ConfigBuilder builder, @Nonnull final ConfigEntryBuilder entryBuilder) {

        SubCategoryBuilder modCategory = ClothAPIFactory.createSubCategory(entryBuilder, "mobeffects.modname", false);

        SubCategoryBuilder subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "mobeffects.cfg.logging", false);
        BooleanToggleBuilder boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.logging.EnableDebug",
                Config.CLIENT.logging.enableLogging);
        subCategory.add(boolBuilder.build());

        IntFieldBuilder intBuilder = ClothAPIFactory.createInteger(
                builder,
                "mobeffects.cfg.logging.FlagMask",
                Config.CLIENT.logging.flagMask,
                0,
                Integer.MAX_VALUE);
        subCategory.add(intBuilder.build());

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "mobeffects.cfg.footsteps", false);
        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.footsteps.Enable",
                Config.CLIENT.footsteps.enableFootprintParticles);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.footsteps.Accents",
                Config.CLIENT.footsteps.enableFootstepAccents);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.footsteps.Cadence",
                Config.CLIENT.footsteps.firstPersonFootstepCadence);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.footsteps.Quadruped",
                Config.CLIENT.footsteps.footstepsAsQuadruped);
        subCategory.add(boolBuilder.build());

        EnumSelectorBuilder<FootprintStyle> footprintStyle = ClothAPIFactory.createEnumList(
                builder,
                "mobeffects.cfg.footsteps.PlayerStyle",
                FootprintStyle.class,
                Config.CLIENT.footsteps.playerFootprintStyle);
        subCategory.add(footprintStyle.build());

        IntSliderBuilder intSliderBuilder = ClothAPIFactory.createIntegerSlider(
                builder,
                "mobeffects.cfg.footsteps.Volume",
                Config.CLIENT.footsteps.footstepVolume,
                0,
                100);
        subCategory.add(intSliderBuilder.build());

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "mobeffects.cfg.effects", false);
        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.effects.PotionParticles",
                Config.CLIENT.effects.hidePlayerPotionParticles);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.effects.Breath",
                Config.CLIENT.effects.showBreath);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.effects.Arrow",
                Config.CLIENT.effects.showArrowTrail);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.effects.Toolbar",
                Config.CLIENT.effects.enableToolbarEffect);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.effects.Bow",
                Config.CLIENT.effects.enableBowEffect);
        subCategory.add(boolBuilder.build());

        boolBuilder = ClothAPIFactory.createBoolean(
                builder,
                "mobeffects.cfg.effects.Swing",
                Config.CLIENT.effects.enableSwingEffect);
        subCategory.add(boolBuilder.build());

        intSliderBuilder = ClothAPIFactory.createIntegerSlider(
                builder,
                "mobeffects.cfg.effects.ToolbarVolume",
                Config.CLIENT.effects.toolbarVolume,
                0,
                100);
        subCategory.add(intSliderBuilder.build());

        modCategory.add(subCategory.build());

        // Add other sections...

        return modCategory;
    }
}

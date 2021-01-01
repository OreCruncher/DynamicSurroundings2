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

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "mobeffects.cfg.footsteps", false);
        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.footsteps.enableFootprintParticles));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.footsteps.enableFootstepAccents));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.footsteps.firstPersonFootstepCadence));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.footsteps.footstepsAsQuadruped));

        subCategory.add(
                ClothAPIFactory.createEnumList(
                        builder,
                        Config.SPEC,
                        FootprintStyle.class,
                        Config.CLIENT.footsteps.playerFootprintStyle));

        subCategory.add(
                ClothAPIFactory.createIntegerSlider(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.footsteps.footstepVolume,
                        0,
                        100));

        modCategory.add(subCategory.build());

        subCategory = ClothAPIFactory.createSubCategory(entryBuilder, "mobeffects.cfg.effects", false);
        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.hidePlayerPotionParticles));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.showBreath));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.showArrowTrail));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.enableToolbarEffect));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.enableBowEffect));

        subCategory.add(
                ClothAPIFactory.createBoolean(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.enableSwingEffect));

        subCategory.add(
                ClothAPIFactory.createIntegerSlider(
                        builder,
                        Config.SPEC,
                        Config.CLIENT.effects.toolbarVolume,
                        0,
                        100));

        modCategory.add(subCategory.build());

        return modCategory;
    }
}

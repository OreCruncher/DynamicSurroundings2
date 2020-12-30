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

package org.orecruncher.dsurround;

import me.shedaniel.clothconfig2.forge.api.ConfigBuilder;
import me.shedaniel.clothconfig2.forge.api.ConfigCategory;
import me.shedaniel.clothconfig2.forge.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.forge.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.forge.impl.builders.IntFieldBuilder;
import me.shedaniel.clothconfig2.forge.impl.builders.SubCategoryBuilder;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.config.ClothAPIFactory;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class ConfigMenuBuilder extends ClothAPIFactory {

    public ConfigMenuBuilder() {
        super(new TranslationTextComponent("dsurround.modname"), () -> {
            org.orecruncher.dsurround.Config.SPEC.save();
            org.orecruncher.sndctrl.Config.SPEC.save();
            org.orecruncher.environs.Config.SPEC.save();
            org.orecruncher.mobeffects.Config.SPEC.save();
        });
    }

    @Override
    protected void generate(@Nonnull final ConfigBuilder builder) {
        final ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory category = createRootCategory(builder);
        SubCategoryBuilder modRoot = createSubCategory(entryBuilder, "dsurround.modname", false);
        SubCategoryBuilder subCategory = createSubCategory(entryBuilder, "dsurround.cfg.logging", true);

        BooleanToggleBuilder boolBuilder = createBoolean(
                builder,
                "dsurround.cfg.logging.VersionCheck",
                true,
                Config.CLIENT.logging.onlineVersionCheck);
        subCategory.add(boolBuilder.build());
        boolBuilder = createBoolean(
                builder,
                "dsurround.cfg.logging.EnableDebug",
                false,
                Config.CLIENT.logging.enableLogging);
        subCategory.add(boolBuilder.build());
        IntFieldBuilder intBuilder = createInteger(
                builder,
                "dsurround.cfg.logging.FlagMask",
                0,
                Config.CLIENT.logging.flagMask,
                0,
                Integer.MAX_VALUE);
        subCategory.add(intBuilder.build());
        modRoot.add(subCategory.build());

        category.addEntry(modRoot.build());

        // Build child mod menus
        category.addEntry(org.orecruncher.sndctrl.ConfigGenerator.generate(builder, entryBuilder).build());
        category.addEntry(org.orecruncher.environs.ConfigGenerator.generate(builder, entryBuilder).build());
        category.addEntry(org.orecruncher.mobeffects.ConfigGenerator.generate(builder, entryBuilder).build());
    }
}

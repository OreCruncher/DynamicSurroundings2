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

package org.orecruncher.lib.config;

import me.shedaniel.clothconfig2.forge.api.ConfigBuilder;
import me.shedaniel.clothconfig2.forge.api.ConfigCategory;
import me.shedaniel.clothconfig2.forge.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.forge.gui.entries.*;
import me.shedaniel.clothconfig2.forge.impl.builders.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public abstract class ClothAPIFactory implements BiFunction<Minecraft, Screen, Screen> {

    private final ITextComponent title;
    private final Runnable save;

    public ClothAPIFactory(@Nonnull final ITextComponent title, @Nonnull final Runnable save) {
        this.title = title;
        this.save = save;
    }

    @Override
    public Screen apply(@Nonnull final Minecraft minecraft, @Nonnull final Screen screen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(screen)
                .setTitle(this.title)
                .setSavingRunnable(this.save);
        generate(builder);
        return builder.build();
    }

    protected abstract void generate(@Nonnull final ConfigBuilder builder);

    public static ConfigCategory createRootCategory(@Nonnull final ConfigBuilder builder) {
        return createCategory(builder, "dontcare");
    }

    public static ConfigCategory createCategory(@Nonnull final ConfigBuilder builder, @Nonnull final String translationKey) {
        return builder.getOrCreateCategory(new TranslationTextComponent(translationKey));
    }

    public static SubCategoryBuilder createSubCategory(@Nonnull final ConfigEntryBuilder entryBuilder, @Nonnull final String translationKey, final boolean expanded) {
        return entryBuilder.startSubCategory(new TranslationTextComponent(translationKey))
                .setTooltip(new TranslationTextComponent(translationKey + ".tooltip"))
                .setExpanded(expanded);
    }

    public static StringListEntry createString(@Nonnull final ConfigBuilder builder, @Nonnull final ForgeConfigSpec spec, @Nonnull final ForgeConfigSpec.ConfigValue<String> value) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(spec, value);
        final ITextComponent name = property.getConfigName();
        final ITextComponent tooltip = property.getTooltip();
        final StringFieldBuilder result = builder.entryBuilder()
                .startStrField(name, value.get())
                .setTooltip(tooltip)
                .setDefaultValue(value.get())
                .setSaveConsumer(value::set);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    public static BooleanListEntry createBoolean(@Nonnull final ConfigBuilder builder, @Nonnull final ForgeConfigSpec spec, @Nonnull final ForgeConfigSpec.BooleanValue value) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(spec, value);
        final ITextComponent name = property.getConfigName();
        final ITextComponent tooltip = property.getTooltip();
        final BooleanToggleBuilder result = builder.entryBuilder()
                .startBooleanToggle(name, value.get())
                .setTooltip(tooltip)
                .setDefaultValue(value.get())
                .setSaveConsumer(value::set);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    public static IntegerListEntry createInteger(@Nonnull final ConfigBuilder builder, @Nonnull final ForgeConfigSpec spec, @Nonnull final ForgeConfigSpec.IntValue value, final int min, final int max) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(spec, value);
        final ITextComponent name = property.getConfigName();
        final ITextComponent tooltip = property.getTooltip();
        final IntFieldBuilder result = builder.entryBuilder()
                .startIntField(name, value.get())
                .setTooltip(tooltip)
                .setDefaultValue(value.get())
                .setMin(min)
                .setMax(max)
                .setSaveConsumer(value::set);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    public static StringListListEntry createStringList(@Nonnull final ConfigBuilder builder, @Nonnull ForgeConfigSpec spec, @Nonnull final ForgeConfigSpec.ConfigValue<List<? extends String>> value, @Nullable final Function<String, Optional<ITextComponent>> validator) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(spec, value);
        final ITextComponent name = property.getConfigName();
        final ITextComponent tooltip = property.getTooltip();
        final List<String> list = value.get().stream().map(Object::toString).collect(Collectors.toList());
        final List<String> defaults = new ArrayList<>(list);
        final StringListBuilder result = builder.entryBuilder()
                .startStrList(name, list)
                .setTooltip(tooltip)
                .setDefaultValue(defaults)
                .setSaveConsumer(value::set);

        if (validator != null)
            result.setCellErrorSupplier(validator);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    public static <T extends Enum<T>> EnumListEntry<T> createEnumList(@Nonnull final ConfigBuilder builder, @Nonnull final ForgeConfigSpec spec, @Nonnull Class<T> clazz, @Nonnull final ForgeConfigSpec.EnumValue<T> value) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(spec, value);
        final ITextComponent name = property.getConfigName();
        final ITextComponent tooltip = property.getTooltip();
        final EnumSelectorBuilder<T> result = builder.entryBuilder()
                .startEnumSelector(name, clazz, value.get())
                .setTooltip(tooltip)
                .setDefaultValue(value.get())
                .setSaveConsumer(value::set);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    public static IntegerSliderEntry createIntegerSlider(@Nonnull final ConfigBuilder builder, @Nonnull final ForgeConfigSpec spec, @Nonnull final ForgeConfigSpec.IntValue value, final int min, final int max) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(spec, value);
        final ITextComponent name = property.getConfigName();
        final ITextComponent tooltip = property.getTooltip();
        final IntSliderBuilder result = builder.entryBuilder()
                .startIntSlider(name, value.get(), min, max)
                .setTooltip(tooltip)
                .setDefaultValue(value.get())
                .setSaveConsumer(value::set);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }
}

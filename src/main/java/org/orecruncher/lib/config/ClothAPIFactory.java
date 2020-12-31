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
import me.shedaniel.clothconfig2.forge.impl.builders.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiFunction;
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

    public static StringFieldBuilder createString(@Nonnull final ConfigBuilder builder, @Nonnull final String translationKey, final String defaultValue, @Nonnull final ForgeConfigSpec.ConfigValue<String> value) {
        return builder.entryBuilder()
                .startStrField(new TranslationTextComponent(translationKey), value.get())
                .setTooltip(new TranslationTextComponent(translationKey + ".tooltip"))
                .setDefaultValue(defaultValue)
                .setSaveConsumer(value::set);
    }

    public static BooleanToggleBuilder createBoolean(@Nonnull final ConfigBuilder builder, @Nonnull final String translationKey, final boolean defaultValue, @Nonnull final ForgeConfigSpec.BooleanValue value) {
        return builder.entryBuilder()
                .startBooleanToggle(new TranslationTextComponent(translationKey), value.get())
                .setTooltip(new TranslationTextComponent(translationKey + ".tooltip"))
                .setDefaultValue(defaultValue)
                .setSaveConsumer(value::set);
    }

    public static IntFieldBuilder createInteger(@Nonnull final ConfigBuilder builder, @Nonnull final String translationKey, final int defaultValue, @Nonnull final ForgeConfigSpec.IntValue value, final int min, final int max) {
        return builder.entryBuilder()
                .startIntField(new TranslationTextComponent(translationKey), value.get())
                .setTooltip(new TranslationTextComponent(translationKey + ".tooltip"))
                .setDefaultValue(defaultValue)
                .setMin(min)
                .setMax(max)
                .setSaveConsumer(value::set);
    }

    public static StringListBuilder createStringList(@Nonnull final ConfigBuilder builder, @Nonnull final String translationKey, @Nonnull final List<String> defaultValue, @Nonnull final ForgeConfigSpec.ConfigValue<List<? extends String>> value) {
        return builder.entryBuilder()
                .startStrList(new TranslationTextComponent(translationKey), value.get().stream().map(Object::toString).collect(Collectors.toList()))
                .setTooltip(new TranslationTextComponent(translationKey + ".tooltip"))
                .setDefaultValue(defaultValue)
                .setSaveConsumer(value::set);
    }

    public static <T extends Enum<T>> EnumSelectorBuilder<T> createEnumList(@Nonnull final ConfigBuilder builder, @Nonnull final String translationKey, @Nonnull Class<T> clazz, @Nonnull final T defaultValue, @Nonnull final ForgeConfigSpec.EnumValue<T> value) {
        return builder.entryBuilder()
                .startEnumSelector(new TranslationTextComponent(translationKey), clazz, defaultValue)
                .setTooltip(new TranslationTextComponent(translationKey + ".tooltip"))
                .setDefaultValue(defaultValue)
                .setSaveConsumer(value::set);
    }

    public static IntSliderBuilder createIntegerSlider(@Nonnull final ConfigBuilder builder, @Nonnull final String translationKey, final int defaultValue, @Nonnull final ForgeConfigSpec.IntValue value, final int min, final int max) {
        return builder.entryBuilder()
                .startIntSlider(new TranslationTextComponent(translationKey), value.get(), min, max)
                .setTooltip(new TranslationTextComponent(translationKey + ".tooltip"))
                .setDefaultValue(defaultValue)
                .setSaveConsumer(value::set);
    }

}

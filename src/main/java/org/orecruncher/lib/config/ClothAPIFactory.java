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
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.common.ForgeConfigSpec;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ClothAPIFactory implements BiFunction<Minecraft, Screen, Screen> {

    private final ITextComponent title;
    private final Runnable save;
    private final ResourceLocation background;

    public ClothAPIFactory(@Nonnull final ITextComponent title, @Nonnull final Runnable save) {
        this(title, save, null);
    }

    public ClothAPIFactory(@Nonnull final ITextComponent title, @Nonnull final Runnable save, @Nullable final ResourceLocation background) {
        this.title = title;
        this.save = save;
        this.background = background;
    }

    @Override
    public Screen apply(@Nonnull final Minecraft minecraft, @Nonnull final Screen screen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(screen)
                .setTitle(this.title)
                .setSavingRunnable(this.save);

        if (this.background != null) {
            builder.setDefaultBackgroundTexture(this.background);
        }

        generate(builder);
        return builder.build();
    }

    protected abstract void generate(@Nonnull final ConfigBuilder builder);

    public static ConfigCategory createRootCategory(@Nonnull final ConfigBuilder builder) {
        return createCategory(builder, "dontcare");
    }

    public static ConfigCategory createCategory(@Nonnull final ConfigBuilder builder, @Nonnull final String translationKey) {
        return builder.getOrCreateCategory(transformText(translationKey, TextFormatting.GOLD));
    }

    public static SubCategoryBuilder createSubCategory(@Nonnull final ConfigEntryBuilder entryBuilder, @Nonnull final String translationKey, final boolean expanded) {
        return createSubCategory(entryBuilder, translationKey, null, expanded);
    }

    public static SubCategoryBuilder createSubCategory(@Nonnull final ConfigEntryBuilder entryBuilder, @Nonnull final String translationKey, @Nullable final TextFormatting color, final boolean expanded) {
        final ITextComponent label = transformText(translationKey, color);

        final List<ITextComponent> toolTip = new ArrayList<>();
        toolTip.add(label);
        final List<ITextProperties> lines = GameUtils.getMC().fontRenderer.getCharacterManager().func_238362_b_(new TranslationTextComponent(translationKey + ".tooltip"), ConfigProperty.TOOLTIP_WIDTH, Style.EMPTY);
        for (final ITextProperties l : lines) {
            toolTip.add(new StringTextComponent(l.getString()));
        }

        return entryBuilder.startSubCategory(label)
                .setTooltip(toolTip.toArray(new ITextComponent[0]))
                .setExpanded(expanded);
    }

    public static StringListEntry createString(@Nonnull final ConfigBuilder builder, @Nonnull final ForgeConfigSpec.ConfigValue<String> value) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(value);
        final ITextComponent name = property.getConfigName();
        final StringFieldBuilder result = builder.entryBuilder()
                .startStrField(name, value.get())
                .setTooltip(property.getTooltip())
                .setDefaultValue(value.get())
                .setSaveConsumer(value::set);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    public static BooleanListEntry createBoolean(@Nonnull final ConfigBuilder builder, @Nonnull final ForgeConfigSpec.BooleanValue value) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(value);
        final ITextComponent name = property.getConfigName();
        final BooleanToggleBuilder result = builder.entryBuilder()
                .startBooleanToggle(name, value.get())
                .setTooltip(property.getTooltip())
                .setDefaultValue(value.get())
                .setYesNoTextSupplier(DialogTexts::optionsEnabled)
                .setSaveConsumer(value::set);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    public static IntegerListEntry createInteger(@Nonnull final ConfigBuilder builder, @Nonnull final ForgeConfigSpec.IntValue value) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(value);
        final ITextComponent name = property.getConfigName();
        final int min = property.<Integer>getMinValue();
        final int max = property.<Integer>getMaxValue();
        final IntFieldBuilder result = builder.entryBuilder()
                .startIntField(name, value.get())
                .setTooltip(property.getTooltip())
                .setDefaultValue(value.get())
                .setMin(min)
                .setMax(max)
                .setSaveConsumer(value::set);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    public static StringListListEntry createStringList(@Nonnull final ConfigBuilder builder, @Nonnull final ForgeConfigSpec.ConfigValue<List<? extends String>> value, @Nullable final Function<String, Optional<ITextComponent>> validator) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(value);
        final ITextComponent name = property.getConfigName();
        final List<String> list = value.get().stream().map(Object::toString).collect(Collectors.toList());
        final List<String> defaults = new ArrayList<>(list);
        final StringListBuilder result = builder.entryBuilder()
                .startStrList(name, list)
                .setTooltip(property.getTooltip())
                .setDefaultValue(defaults)
                .setSaveConsumer(value::set);

        if (validator != null)
            result.setCellErrorSupplier(validator);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    public static <T extends Enum<T>> EnumListEntry<T> createEnumList(@Nonnull final ConfigBuilder builder, @Nonnull Class<T> clazz, @Nonnull final ForgeConfigSpec.EnumValue<T> value) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(value);
        final ITextComponent name = property.getConfigName();
        final EnumSelectorBuilder<T> result = builder.entryBuilder()
                .startEnumSelector(name, clazz, value.get())
                .setTooltip(property.getTooltip())
                .setDefaultValue(value.get())
                .setSaveConsumer(value::set);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    public static IntegerSliderEntry createIntegerSlider(@Nonnull final ConfigBuilder builder, @Nonnull final ForgeConfigSpec.IntValue value) {
        final ConfigProperty property = ConfigProperty.getPropertyInfo(value);
        final ITextComponent name = property.getConfigName();
        final int min = property.<Integer>getMinValue();
        final int max = property.<Integer>getMaxValue();
        final IntSliderBuilder result = builder.entryBuilder()
                .startIntSlider(name, value.get(), min, max)
                .setTooltip(property.getTooltip())
                .setDefaultValue(value.get())
                .setSaveConsumer(value::set);

        if (property.getNeedsWorldRestart())
            result.requireRestart();

        return result.build();
    }

    private static ITextComponent transformText(@Nonnull final String key, @Nullable final TextFormatting color) {
        ITextComponent result = new TranslationTextComponent(key);
        if (color != null) {
            final String text = color + new TranslationTextComponent(key).getString();
            result = new StringTextComponent(text);
        }
        return result;
    }
}

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

import net.minecraft.client.gui.DialogTexts;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import org.orecruncher.lib.gui.GuiHelpers;
import org.orecruncher.lib.reflection.ObjectField;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class ConfigProperty {

    public static final int TOOLTIP_WIDTH = 300;

    private static final ObjectField<ForgeConfigSpec.ConfigValue, ForgeConfigSpec> specAccessor = new ObjectField<>(ForgeConfigSpec.ConfigValue.class, () -> null, "spec");
    private static final ObjectField<Object, Object> minAccessor = new ObjectField<>(
            "net.minecraftforge.common.ForgeConfigSpec$Range",
            () -> null,
            "min");
    private static final ObjectField<Object, Object> maxAccessor = new ObjectField<>(
            "net.minecraftforge.common.ForgeConfigSpec$Range",
            () -> null,
            "max");

    private final ForgeConfigSpec.ValueSpec valueSpec;
    private final String name;

    private ITextComponent[] toolTip;

    private ConfigProperty(@Nonnull final ForgeConfigSpec.ConfigValue<?> configEntry) {
        this(specAccessor.get(configEntry), configEntry);
    }

    private ConfigProperty(@Nonnull final ForgeConfigSpec spec, @Nonnull final ForgeConfigSpec.ConfigValue<?> configEntry) {
        final List<String> path = configEntry.getPath();
        this.valueSpec = spec.get(path);
        this.name = path.get(path.size() - 1);
    }

    public String getTranslationKey() {
        return this.valueSpec.getTranslationKey();
    }

    @Nonnull
    public IFormattableTextComponent getConfigName() {
        final String key = getTranslationKey();
        if (StringUtils.isNullOrEmpty(key)) {
            return new StringTextComponent(this.name);
        }

        return new TranslationTextComponent(key);
    }

    @Nullable
    public String getComment() {
        return this.valueSpec.getComment();
    }

    @Nullable
    public ITextComponent[] getTooltip() {
        if (this.toolTip == null) {
            final List<ITextComponent> result = new ArrayList<>();
            String key = getTranslationKey();
            if (StringUtils.isNullOrEmpty(key)) {
                key = getComment();
                if (StringUtils.isNullOrEmpty(key))
                    return null;
                result.add(new StringTextComponent(key));
            } else {
                final ITextComponent title = new StringTextComponent(TextFormatting.GOLD + new TranslationTextComponent(key).getString());
                result.add(title);
                result.addAll(GuiHelpers.getTrimmedTextCollection(key + ".tooltip", TOOLTIP_WIDTH));
            }

            final Object theDefault = getDefault();
            if (theDefault != null) {
                String text = theDefault.toString();
                if (text.compareToIgnoreCase("true") == 0)
                    text = DialogTexts.OPTIONS_ON.getString();
                else if (text.compareToIgnoreCase("false") == 0)
                    text = DialogTexts.OPTIONS_OFF.getString();
                else
                    text = GuiHelpers.getTrimmedText(text, TOOLTIP_WIDTH).getString();
                text = new TranslationTextComponent("dsurround.text.format.default", text).getString();
                result.add(new StringTextComponent(text));
            }

            final Object range = this.valueSpec.getRange();
            if (range != null) {
                result.add(new StringTextComponent(TextFormatting.GREEN + "[ " + range.toString() + " ]"));
            }

            if (getNeedsWorldRestart()) {
                result.add(new TranslationTextComponent("dsurround.text.tooltip.restartRequired"));
            }

            this.toolTip = result.toArray(new ITextComponent[0]);
        }

        return this.toolTip;
    }

    public boolean getNeedsWorldRestart() {
        return this.valueSpec.needsWorldRestart();
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefault() {
        return (T) this.valueSpec.getDefault();
    }

    @SuppressWarnings("unchecked")
    public <T> T getMinValue() {
        return (T) minAccessor.get(this.valueSpec.getRange());
    }

    @SuppressWarnings("unchecked")
    public <T> T getMaxValue() {
        return (T) maxAccessor.get(this.valueSpec.getRange());
    }

    @Nonnull
    public static ConfigProperty getPropertyInfo(@Nonnull final ForgeConfigSpec.ConfigValue<?> configEntry) {
        return new ConfigProperty(configEntry);
    }

}

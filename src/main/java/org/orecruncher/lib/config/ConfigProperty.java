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
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import org.orecruncher.lib.reflection.ObjectField;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class ConfigProperty {

    private static final ObjectField<ForgeConfigSpec.ConfigValue, ForgeConfigSpec> specAccessor = new ObjectField<>(ForgeConfigSpec.ConfigValue.class, () -> null, "spec");

    private final ForgeConfigSpec.ValueSpec valueSpec;
    private final String name;

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
    public IFormattableTextComponent getTooltip() {
        IFormattableTextComponent result;
        String key = getTranslationKey();
        if (StringUtils.isNullOrEmpty(key)) {
            key = getComment();
            if (StringUtils.isNullOrEmpty(key))
                return null;
            result = new StringTextComponent(key);
        } else {
            result = new TranslationTextComponent(key + ".tooltip");
        }

        final Object theDefault = getDefault();
        if (theDefault != null) {
            String text = theDefault.toString();
            if (text.compareToIgnoreCase("true") == 0)
                text = DialogTexts.OPTIONS_ON.getString();
            else if (text.compareToIgnoreCase("false") == 0)
                text = DialogTexts.OPTIONS_OFF.getString();
            else
                text = trimDown(text);
            text = new TranslationTextComponent("dsurround.msg.format.default", text).getString();
            result.append(new StringTextComponent(text));
        }

        return result;
    }

    private static String trimDown(@Nonnull final String txt) {
        final int maxLength = 48;
        if (txt.length() < maxLength)
            return txt;
        return txt.substring(0, maxLength - 3) + "...";
    }

    public boolean getNeedsWorldRestart() {
        return this.valueSpec.needsWorldRestart();
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefault() {
        return (T) this.valueSpec.getDefault();
    }

    @Nonnull
    public static ConfigProperty getPropertyInfo(@Nonnull final ForgeConfigSpec.ConfigValue<?> configEntry) {
        return new ConfigProperty(configEntry);
    }

}

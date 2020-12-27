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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.orecruncher.lib.Lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ConfigurationBase {

    public static Pair<ConfigurationBase, ForgeConfigSpec> generateSpec(@Nonnull final Supplier<? extends ConfigurationBase> configSupplier) {
        final ConfigurationBase instance = configSupplier.get();
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        return Pair.of(instance, builder.build());
    }

    private static void process(@Nonnull final ConfigurationBase instance, @Nonnull final ForgeConfigSpec.Builder builder) {

        // Iterate over the fields in the class
        for (final Field field : instance.getClass().getFields()) {
            // Get the comment if present
            final ConfigAnnotations.Comment comment = field.getAnnotation(ConfigAnnotations.Comment.class);

            // See if it is marked as a category
            final ConfigAnnotations.Category category = field.getAnnotation(ConfigAnnotations.Category.class);
            if (category != null) {
                // Get the field
                ConfigurationBase child;
                try {
                    child = (ConfigurationBase) field.get(instance);
                    if (child == null) {
                        Lib.LOGGER.warn("Config category instance '%s' not initialized, skipping", field.getName());
                        continue;
                    }
                } catch(@Nonnull final Throwable t) {
                    Lib.LOGGER.error(t, "Config category instance '%s' is not of type ConfigurationBase, skipping", field.getName());
                    continue;
                }

                // Tack on the comment before pushing
                if (comment != null)
                    builder.comment(comment.value());

                // Recurse into the options instance
                builder.push(category.value());
                process(child, builder);
                builder.pop();
                continue;
            }

            final ConfigAnnotations.OptionBoolean bool = field.getAnnotation(ConfigAnnotations.OptionBoolean.class);
            if (bool != null) {
                processBoolean(instance, field, comment, bool, builder);
                continue;
            }

            final ConfigAnnotations.OptionString str = field.getAnnotation(ConfigAnnotations.OptionString.class);
            if (str != null) {
                processString(instance, field, comment, str, builder);
                continue;
            }

            final ConfigAnnotations.OptionInteger integer = field.getAnnotation(ConfigAnnotations.OptionInteger.class);
            if (integer != null) {
                processInteger(instance, field, comment, integer, builder);
                continue;
            }

            final ConfigAnnotations.OptionDouble float0 = field.getAnnotation(ConfigAnnotations.OptionDouble.class);
            if (float0 != null) {
                processFloat(instance, field, comment, float0, builder);
                continue;
            }

            final ConfigAnnotations.OptionEnum enum0 = field.getAnnotation(ConfigAnnotations.OptionEnum.class);
            if (enum0 != null) {
                processEnum(instance, field, comment, enum0, builder);
                continue;
            }
        }
    }

    private static ForgeConfigSpec.BooleanValue processBoolean(@Nonnull final ConfigurationBase instance, @Nonnull final Field field, @Nullable final ConfigAnnotations.Comment comment, @Nonnull final ConfigAnnotations.OptionBoolean annotation, @Nonnull final ForgeConfigSpec.Builder builder) {
        boolean defaultValue = false;
        try {
            defaultValue = (boolean) field.get(instance);
        } catch (@Nonnull final Throwable ignored) {
        }

        if (comment != null && comment.value().length() > 0)
            builder.comment(comment.value());

        if (annotation.translationKey().length() > 0)
            builder.translation(annotation.translationKey());

        if (isRestartRequred(field))
            builder.worldRestart();

        return builder.define(annotation.value(), defaultValue);
    }

    private static ForgeConfigSpec.ConfigValue<String> processString(@Nonnull final ConfigurationBase instance, @Nonnull final Field field, @Nullable final ConfigAnnotations.Comment comment, @Nonnull final ConfigAnnotations.OptionString annotation, @Nonnull final ForgeConfigSpec.Builder builder) {
        String defaultValue = null;
        try {
            defaultValue = (String) field.get(instance);
        } catch (@Nonnull final Throwable ignored) {
        }

        if (comment != null && comment.value().length() > 0)
            builder.comment(comment.value());

        if (annotation.translationKey().length() > 0)
            builder.translation(annotation.translationKey());

        if (isRestartRequred(field))
            builder.worldRestart();

        return builder.define(annotation.value(), defaultValue);
    }

    private static ForgeConfigSpec.IntValue processInteger(@Nonnull final ConfigurationBase instance, @Nonnull final Field field, @Nullable final ConfigAnnotations.Comment comment, @Nonnull final ConfigAnnotations.OptionInteger annotation, @Nonnull final ForgeConfigSpec.Builder builder) {
        int defaultValue = 0;
        try {
            defaultValue = (int) field.get(instance);
        } catch (@Nonnull final Throwable ignored) {
        }

        if (comment != null && comment.value().length() > 0)
            builder.comment(comment.value());

        if (annotation.translationKey().length() > 0)
            builder.translation(annotation.translationKey());

        if (isRestartRequred(field))
            builder.worldRestart();

        return builder.defineInRange(annotation.value(), defaultValue, annotation.min(), annotation.max());
    }

    private static ForgeConfigSpec.DoubleValue processFloat(@Nonnull final ConfigurationBase instance, @Nonnull final Field field, @Nullable final ConfigAnnotations.Comment comment, @Nonnull final ConfigAnnotations.OptionDouble annotation, @Nonnull final ForgeConfigSpec.Builder builder) {
        float defaultValue = 0;
        try {
            defaultValue = (float) field.get(instance);
        } catch (@Nonnull final Throwable ignored) {
        }

        if (comment != null && comment.value().length() > 0)
            builder.comment(comment.value());

        if (annotation.translationKey().length() > 0)
            builder.translation(annotation.translationKey());

        if (isRestartRequred(field))
            builder.worldRestart();

        return builder.defineInRange(annotation.value(), defaultValue, annotation.min(), annotation.max());
    }

    private static ForgeConfigSpec.EnumValue processEnum(@Nonnull final ConfigurationBase instance, @Nonnull final Field field, @Nullable final ConfigAnnotations.Comment comment, @Nonnull final ConfigAnnotations.OptionEnum annotation, @Nonnull final ForgeConfigSpec.Builder builder) {
        Enum defaultValue = null;
        try {
            defaultValue = (Enum) field.get(instance);
        } catch (@Nonnull final Throwable ignored) {
        }

        if (comment != null && comment.value().length() > 0)
            builder.comment(comment.value());

        if (annotation.translationKey().length() > 0)
            builder.translation(annotation.translationKey());

        if (isRestartRequred(field))
            builder.worldRestart();

        return builder.defineEnum(annotation.value(), defaultValue);
    }

    private static boolean isRestartRequred(@Nonnull final Field field) {
        return field.getAnnotation(ConfigAnnotations.RestartRequired.class) != null;
    }

    /**
     * Marked protected to force derivation to create a new config section
     */
    protected ConfigurationBase() {

    }

    /**
     * Called when the configuration state has been updated and the configuration instance
     * has a chance to refresh
     */
    public void update() {

    }

}

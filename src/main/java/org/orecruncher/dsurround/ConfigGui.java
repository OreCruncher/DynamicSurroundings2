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

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import me.shedaniel.clothconfig2.forge.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.forge.api.ConfigBuilder;
import me.shedaniel.clothconfig2.forge.api.ConfigCategory;
import me.shedaniel.clothconfig2.forge.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.forge.impl.builders.IntFieldBuilder;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

import javax.annotation.Nonnull;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ConfigGui {

    public static void registerConfigGuiHandler(@Nonnull final ForgeConfigSpec spec, @Nonnull final Runnable save, @Nonnull final String titleKey) {
        final ModLoadingContext context = ModLoadingContext.get();
        context.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY,
                () -> (mc, screen) -> {
                    ConfigBuilder builder = ConfigBuilder.create()
                            .setParentScreen(screen)
                            .setTitle(new TranslationTextComponent(titleKey))
                            .setSavingRunnable(save);
                    generateMenu(spec, builder);
                    return builder.build();
                });
    }

    // Root level traversal of the config spec
    private static void generateMenu(@Nonnull final ForgeConfigSpec spec, @Nonnull final ConfigBuilder builder) {

        for (final UnmodifiableConfig.Entry kvp : spec.getValues().entrySet()) {
            if (kvp.getRawValue() instanceof AbstractConfig) {
                final AbstractConfig cfg = kvp.getRawValue();
                generateCategory(kvp.getKey(), cfg, builder);
            }
        }
    }

    private static void generateCategory(@Nonnull final String name, @Nonnull final AbstractConfig config, @Nonnull final ConfigBuilder builder) {

        DynamicSurroundings.LOGGER.info("Generating category '%s'", name);

        ConfigCategory category = builder.getOrCreateCategory(new StringTextComponent(name));

        for (final Map.Entry<String, Object> kvp : config.valueMap().entrySet()) {
            final String key = kvp.getKey();
            final Object obj = kvp.getValue();

            if (obj instanceof AbstractConfig) {
                generateCategory(key, (AbstractConfig) obj, builder);
            } else {
                // It's a value type
                generateSetting(key, obj, category, builder);
            }
        }
    }

    private static void generateSetting(@Nonnull final String name, @Nonnull final Object obj, @Nonnull final ConfigCategory category, @Nonnull final ConfigBuilder builder) {
        DynamicSurroundings.LOGGER.info("Generating setting '%s' (%s)", name, obj.getClass().getName());
/*
        AbstractConfigListEntry entry = null;

        if (obj instanceof ForgeConfigSpec.BooleanValue) {
            ForgeConfigSpec.BooleanValue val = (ForgeConfigSpec.BooleanValue) obj;
            val.
            entry = createBoolean(
                    builder,
                    "dsurround.cfg.logging.VersionCheck",
                    true,
                    client.logging.onlineVersionCheck);

            category.add

        } else if (obj instanceof ForgeConfigSpec.IntValue) {

        }
        */
    }

/*
        final ConfigCategory category = builder.getOrCreateCategory(new TranslationTextComponent("dsurround.cfg.logging"));

        BooleanToggleBuilder boolBuilder = createBoolean(
                builder,
                "dsurround.cfg.logging.VersionCheck",
                true,
                client.logging.onlineVersionCheck);

        category.addEntry(boolBuilder.build());

        boolBuilder = createBoolean(
                builder,
                "dsurround.cfg.logging.EnableDebug",
                false,
                client.logging.enableLogging);

        category.addEntry(boolBuilder.build());

        IntFieldBuilder intBuilder = createInteger(
                builder,
                "dsurround.cfg.logging.FlagMask",
                0,
                client.logging.flagMask,
                0,
                Integer.MAX_VALUE);

        category.addEntry(intBuilder.build());

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
    */
}
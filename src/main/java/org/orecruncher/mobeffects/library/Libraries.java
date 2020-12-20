/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.mobeffects.library;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.JsonUtils;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.library.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class Libraries {
    private Libraries() {
    }

    public static void initialize() {
        EffectLibrary.initialize();
        ItemLibrary.initialize();
        FootstepLibrary.initialize();

        // Config locations are mods plus resource packs that could contain Json files
        final List<String> configLocations = ForgeUtils.getConfigLocations();

        // Since other mods/packs can override our settings we need to process our data first.
        configLocations.remove(MobEffects.MOD_ID);
        configLocations.add(0, MobEffects.MOD_ID);

        // List of installed mods are the names of configs we will look for
        final List<String> installed = ForgeUtils.getModIdList();

        // Need to process the minecraft config first since it other configs can override.  Oh, and MobEffects
        // doesn't have a config since it uses minecraft.json.
        installed.remove(MobEffects.MOD_ID);
        installed.remove("minecraft");
        installed.add(0, "minecraft");

        for (final String loc: configLocations) {
            for (final String id : installed) {
                try {
                    final String resource = String.format("%s/%s.json", MobEffects.MOD_ID, id);
                    final ResourceLocation res = new ResourceLocation(loc, resource);
                    final ModConfig mod = JsonUtils.load(res, ModConfig.class);
                    FootstepLibrary.initFromConfig(mod);
                    ItemLibrary.initFromConfig(mod);
                } catch (@Nonnull final Throwable t) {
                    MobEffects.LOGGER.error(t, "Unable to load '%s.json' config data!", id);
                }
            }
        }
    }

    public static void complete() {
        FootstepLibrary.complete();
        ItemLibrary.complete();
    }
}

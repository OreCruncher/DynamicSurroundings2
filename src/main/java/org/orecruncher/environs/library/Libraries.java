/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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

package org.orecruncher.environs.library;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.library.config.ModConfig;
import org.orecruncher.lib.JsonUtils;
import org.orecruncher.lib.fml.ForgeUtils;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class Libraries {
    private Libraries() {

    }

    public static void initialize() {
        DimensionLibrary.initialize();
        BiomeLibrary.initialize();
        BlockStateLibrary.initialize();

        // Config locations are mods plus resource packs that could contain Json files
        final List<String> configLocations = ForgeUtils.getConfigLocations();

        // Since other mods/packs can override our settings we need to process our data first.
        configLocations.remove(Environs.MOD_ID);
        configLocations.add(0, Environs.MOD_ID);

        // List of installed mods are the names of configs we will look for
        final List<String> installed = ForgeUtils.getModIdList();

        // Need to process the minecraft config first since it other configs can override.  Oh, and MobEffects
        // doesn't have a config since it uses minecraft.json.
        installed.remove(Environs.MOD_ID);
        installed.remove("minecraft");
        installed.add(0, "minecraft");

        for (final String loc : configLocations) {
            for (final String id : installed) {
                try {
                    final String resource = String.format("%s/%s.json", Environs.MOD_ID, id);
                    final ResourceLocation res = new ResourceLocation(loc, resource);
                    final ModConfig mod = JsonUtils.load(res, ModConfig.class);
                    DimensionLibrary.initFromConfig(mod);
                    BiomeLibrary.initFromConfig(mod);
                    BlockStateLibrary.initFromConfig(mod);
                } catch (@Nonnull final Throwable t) {
                    Environs.LOGGER.error(t, "Unable to load '%s.json' config data!", id);
                }
            }
        }
    }

    public static void complete() {
        DimensionLibrary.complete();
        BiomeLibrary.complete();
        BlockStateLibrary.complete();
    }
}

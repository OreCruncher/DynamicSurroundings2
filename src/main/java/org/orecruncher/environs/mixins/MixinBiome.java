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
package org.orecruncher.environs.mixins;

import net.minecraft.world.biome.Biome;
import org.orecruncher.environs.library.BiomeInfo;
import org.orecruncher.environs.library.BiomeUtil;
import org.orecruncher.environs.misc.IMixinBiomeData;
import org.orecruncher.lib.gui.Color;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Biome.class)
public class MixinBiome implements IMixinBiomeData {

    private BiomeInfo environs_biomeInfo;

    @Nullable
    @Override
    public BiomeInfo getInfo() {
        return this.environs_biomeInfo;
    }

    @Override
    public void setInfo(@Nullable BiomeInfo info) {
        this.environs_biomeInfo = info;
    }

    @Inject(method = "getFogColor()I", at = @At("HEAD"), cancellable = true)
    public void getFogColor(CallbackInfoReturnable<Integer> cir) {
        // Need to invoke getBiomeData() because it will populate environs_biomeInfo if not already set
        final BiomeInfo info = BiomeUtil.getBiomeData((Biome) (Object) this);
        final Color color = info.getFogColor();
        if (color != null) {
            cir.setReturnValue(color.rgb());
        }
    }
}

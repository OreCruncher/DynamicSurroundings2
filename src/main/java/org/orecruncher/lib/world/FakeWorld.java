/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

package org.orecruncher.lib.world;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.world.*;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FakeWorld extends ClientWorld {

    public static FakeWorld create(@Nonnull final String name) {
        final WorldInfo info = new WorldInfo(new WorldSettings(-1, GameType.NOT_SET, false, false, WorldType.CUSTOMIZED), name);
        final WorldSettings settings = new WorldSettings(info);
        return new FakeWorld(null, settings, DimensionType.OVERWORLD, 12, null, null);
    }

    FakeWorld(ClientPlayNetHandler p_i51056_1_, WorldSettings p_i51056_2_, DimensionType p_i51056_3_, int p_i51056_4_, IProfiler p_i51056_5_, WorldRenderer p_i51056_6_) {
        super(p_i51056_1_, p_i51056_2_, p_i51056_3_, p_i51056_4_, p_i51056_5_, p_i51056_6_);
    }

}
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

package org.orecruncher.sndctrl.events;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BlockInspectionEvent extends Event {

    public final List<String> data = new ArrayList<>();
    public final BlockRayTraceResult rayTrace;
    public final World world;
    public final BlockState state;
    public final BlockPos pos;

    public BlockInspectionEvent(@Nonnull final BlockRayTraceResult trace, @Nonnull final World world, @Nonnull final BlockState state, @Nonnull final BlockPos pos) {
        this.rayTrace = trace;
        this.world = world;
        this.state = state;
        this.pos = pos;
    }
}

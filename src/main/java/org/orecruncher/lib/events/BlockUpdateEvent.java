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

package org.orecruncher.lib.events;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

public class BlockUpdateEvent extends Event {

    private final Collection<BlockPos> positions;
    private final Set<BlockPos> expanded;

    public BlockUpdateEvent(@Nonnull final Collection<BlockPos> positions) {
        this.positions = positions;
        this.expanded = new ObjectOpenHashSet<>();
    }

    @Nonnull
    public Collection<BlockPos> getPositions() {
        return this.positions;
    }

    @Nonnull
    public Collection<BlockPos> getExpandedPositions() {
        if (this.expanded.size() == 0) {
            for (final BlockPos center : this.positions)
                expand(center, this.expanded);
        }
        return this.expanded;
    }

    protected void expand(@Nonnull final BlockPos center, @Nonnull final Set<BlockPos> result) {
        for (int i = -1; i < 2; i++)
            for (int j = -1; j < 2; j++)
                for (int k = -1; k < 2; k++)
                    result.add(center.add(i, j, k));
    }
}

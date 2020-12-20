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

import com.google.common.base.MoreObjects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.WeightTable;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class WeightedAcousticEntry extends AcousticEntry implements WeightTable.IItem<IAcoustic> {

    private final int weight;

    public WeightedAcousticEntry(@Nonnull final IAcoustic acoustic, @Nullable String conditions, final int weight) {
        super(acoustic, conditions);
        this.weight = weight;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public IAcoustic getItem() {
        return getAcoustic();
    }

    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("weight", getWeight())
                .addValue(getItem().toString())
                .addValue(getConditionsForLogging())
                .toString();
    }
}

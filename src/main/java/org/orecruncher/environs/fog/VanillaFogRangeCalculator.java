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

package org.orecruncher.environs.fog;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import javax.annotation.Nonnull;

/**
 * Vanilla calculator that reflects whatever is in the event in terms of
 * start/end points for fog.
 */
@OnlyIn(Dist.CLIENT)
public class VanillaFogRangeCalculator implements IFogRangeCalculator {

    private final String name;

    public VanillaFogRangeCalculator() {
        this.name = "VanillaFogRangeCalculator";
    }

    protected VanillaFogRangeCalculator(@Nonnull final String name) {
        this.name = name;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Override
    @Nonnull
    public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        return new FogResult(event);
    }

    @Override
    public void tick() {

    }

}

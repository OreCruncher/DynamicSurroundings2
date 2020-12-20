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

@OnlyIn(Dist.CLIENT)
public interface IFogRangeCalculator {

    /**
     * The name of the fog calculator for logging purposes.
     *
     * @return The name of the fog calculator
     */
    @Nonnull
    String getName();

    /**
     * Called during the render pass to obtain parameters for fog rendering.
     *
     * @param event The event that is being fired
     * @return FogResult containing the fog information the calculator is interested
     * in reporting
     */
    @Nonnull
    FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event);

    /**
     * Called once every client side tick. Up to the calculator to figure out what
     * to do with the time, if anything.
     */
    void tick();

}

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
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.gui.Color;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class HolisticFogColorCalculator implements IFogColorCalculator {

    protected ObjectArray<IFogColorCalculator> calculators = new ObjectArray<>(4);
    protected Color cached;

    public void add(@Nonnull final IFogColorCalculator calc) {
        this.calculators.add(calc);
    }

    @Nonnull
    @Override
    public Color calculate(@Nonnull final EntityViewRenderEvent.FogColors event) {
        Color result = null;
        for (int i = 0; i < this.calculators.size(); i++) {
            final Color color = this.calculators.get(i).calculate(event);
            if (result == null)
                result = color;
            else
                result = result.mix(color);

        }
        return this.cached = result;
    }

    @Override
    public void tick() {
        this.calculators.forEach(IFogColorCalculator::tick);
    }

    @Override
    public String toString() {
        return this.cached != null ? this.cached.toString() : "<NOT SET>";
    }

}

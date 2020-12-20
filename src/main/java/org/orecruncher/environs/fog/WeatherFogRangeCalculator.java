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
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.WorldUtils;

import javax.annotation.Nonnull;

/**
 * Calculates the fog ranges based on current weather. The stronger the
 * intensity of the storm the foggier it gets.
 */
@OnlyIn(Dist.CLIENT)
public class WeatherFogRangeCalculator extends VanillaFogRangeCalculator {

    protected static final float START_IMPACT = 0.9F;
    protected static final float END_IMPACT = 0.4F;

    protected final FogResult cache = new FogResult();

    public WeatherFogRangeCalculator() {
        super("WeatherFogRangeCalculator");
    }

    @Override
    @Nonnull
    public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        // Start with what vanilla thinks
        this.cache.set(event);
        final float rainStr = WorldUtils.getRainStrength(GameUtils.getWorld(), (float) event.getRenderPartialTicks());
        if (rainStr > 0) {
            // Calculate our scaling factor
            final float startScale = 1F - (START_IMPACT * rainStr);
            final float endScale = 1F - (END_IMPACT * rainStr);
            this.cache.set(this.cache.getStart() * startScale, this.cache.getEnd() * endScale);
        }

        return this.cache;
    }
}

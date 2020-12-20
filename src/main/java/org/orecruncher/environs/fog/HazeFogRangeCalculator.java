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

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.orecruncher.environs.handlers.CommonState;
import org.orecruncher.environs.library.DimensionInfo;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;

/**
 * Calculates the fog ranges based on player elevation as compared to the
 * dimensions cloud height.
 */
@OnlyIn(Dist.CLIENT)
public class HazeFogRangeCalculator extends VanillaFogRangeCalculator {

    protected static final int BAND_OFFSETS = 15;
    protected static final int BAND_CORE_SIZE = 10;
    protected static final float IMPACT_FAR = 0.6F;
    protected static final float IMPACT_NEAR = 0.95F;

    protected final FogResult cached = new FogResult();

    public HazeFogRangeCalculator() {
        super("HazeFogRangeCalculator");
    }

    @Override
    @Nonnull
    public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        final DimensionInfo di = CommonState.getDimensionInfo();
        if (di.hasHaze()) {
            final float lowY = di.getCloudHeight() - BAND_OFFSETS;
            final float highY = di.getCloudHeight() + BAND_OFFSETS + BAND_CORE_SIZE;

            // Calculate the players Y. If it's in the band range calculate the fog
            // parameters
            final Vec3d eyes = GameUtils.getPlayer().getEyePosition((float) event.getRenderPartialTicks());
            if (eyes.y > lowY && eyes.y < highY) {
                final float coreLowY = lowY + BAND_OFFSETS;
                final float coreHighY = coreLowY + BAND_CORE_SIZE;

                float scaleFar = IMPACT_FAR;
                float scaleNear = IMPACT_NEAR;
                if (eyes.y < coreLowY) {
                    final float factor = (float) ((eyes.y - lowY) / BAND_OFFSETS);
                    scaleFar *= factor;
                    scaleNear *= factor;
                } else if (eyes.y > coreHighY) {
                    final float factor = (float) ((highY - eyes.y) / BAND_OFFSETS);
                    scaleFar *= factor;
                    scaleNear *= factor;
                }

                final float end = event.getFarPlaneDistance() * (1F - scaleFar);
                final float start = event.getFarPlaneDistance() * (1F - scaleNear);
                this.cached.set(start, end);
                return this.cached;
            }
        }

        this.cached.set(event);
        return this.cached;
    }

}

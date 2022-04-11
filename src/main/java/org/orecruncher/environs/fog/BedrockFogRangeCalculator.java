/*
 *  Dynamic Surroundings
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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.orecruncher.environs.config.Config;
import org.orecruncher.environs.handlers.CommonState;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.WorldUtils;

import javax.annotation.Nonnull;

/**
 * Implements the void fog (the fog at bedrock) of older versions of Minecraft.
 */
public class BedrockFogRangeCalculator extends VanillaFogRangeCalculator {

    protected final FogResult cached = new FogResult();
    protected double skyLight;

    public BedrockFogRangeCalculator() {
        super("BedrockFogRangeCalculator");
    }

    @Override
    public boolean enabled() {
        return Config.CLIENT.fog.enableBedrockFog.get();
    }

    @Override
    @Nonnull
    public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {

        this.cached.set(event);
        if (!CommonState.getDimensionInfo().isFlatWorld() && WorldUtils.hasVoidParticles(GameUtils.getWorld())) {
            final PlayerEntity player = GameUtils.getPlayer();
            final double factor = (MathHelper.lerp(event.getRenderPartialTicks(), player.lastTickPosY, player.getPosY()) + 4.0D) / 32.0D;
            double d0 = (this.skyLight / 16.0D) + factor;

            float end = event.getFarPlaneDistance();
            if (d0 < 1.0D) {
                if (d0 < 0.0D) {
                    d0 = 0.0D;
                }

                d0 *= d0;
                float f2 = 100.0F * (float) d0;

                if (f2 < 5.0F) {
                    f2 = 5.0F;
                }

                if (end > f2) {
                    end = f2;
                }
            }

            this.cached.setScaled(end, FogResult.DEFAULT_PLANE_SCALE);
        }

        return this.cached;
    }

    @Override
    public void tick() {
        this.skyLight = GameUtils.getPlayer().getBrightness();
    }
}

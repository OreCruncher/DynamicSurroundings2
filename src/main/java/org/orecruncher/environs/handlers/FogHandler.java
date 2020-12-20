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

package org.orecruncher.environs.handlers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.fog.*;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.math.LoggingTimerEMA;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FogHandler extends HandlerBase {

    protected final LoggingTimerEMA renderColor = new LoggingTimerEMA("Render Fog Color");
    protected final LoggingTimerEMA render = new LoggingTimerEMA("Render Fog");

    protected HolisticFogColorCalculator fogColor = new HolisticFogColorCalculator();
    protected HolisticFogRangeCalculator fogRange = new HolisticFogRangeCalculator();

    public FogHandler() {
        super("Fog Handler");
    }

    private boolean doFog() {
        return Config.CLIENT.fog.get_enableFog() && CommonState.getDimensionInfo().hasFog();
    }

    @Override
    public void process(@Nonnull final PlayerEntity player) {

        if (doFog()) {
            this.fogRange.tick();
            this.fogColor.tick();
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void fogColorEvent(final EntityViewRenderEvent.FogColors event) {
        if (doFog()) {
            this.renderColor.begin();
            final IFluidState fluidState = event.getInfo().getFluidState();
            if (fluidState.isEmpty()) {
                final Color color = this.fogColor.calculate(event);
                event.setRed(color.red());
                event.setGreen(color.green());
                event.setBlue(color.blue());
            }
            this.renderColor.end();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void fogRenderEvent(final EntityViewRenderEvent.RenderFogEvent event) {
        if (doFog()) {
            this.render.begin();
            final IFluidState fluidState = event.getInfo().getFluidState();
            if (fluidState.isEmpty()) {
                final FogResult result = this.fogRange.calculate(event);
                GlStateManager.fogStart(result.getStart());
                GlStateManager.fogEnd(result.getEnd());
            }
            this.render.end();
        }
    }

    @SubscribeEvent
    public void diagnostics(final DiagnosticEvent event) {
        if (Config.CLIENT.logging.get_enableLogging()) {
            if (doFog()) {
                event.getLeft().add("Fog Range: " + this.fogRange.toString());
                event.getLeft().add("Fog Color: " + this.fogColor.toString());
                event.addRenderTimer(this.renderColor);
                event.addRenderTimer(this.render);
            } else
                event.getLeft().add("FOG: IGNORED");
        }
    }

    @Override
    public void onConnect() {
        this.fogColor = new HolisticFogColorCalculator();
        this.fogRange = new HolisticFogRangeCalculator();

        if (Config.CLIENT.fog.get_enableBiomeFog()) {
            this.fogColor.add(new BiomeFogColorCalculator());
            this.fogRange.add(new BiomeFogRangeCalculator());
        }

        if (Config.CLIENT.fog.get_enableElevationHaze())
            this.fogRange.add(new HazeFogRangeCalculator());

        if (Config.CLIENT.fog.get_enableMorningFog()) {
            this.fogRange.add(new MorningFogRangeCalculator());
        }

        if (Config.CLIENT.fog.get_enableBedrockFog())
            this.fogRange.add(new BedrockFogRangeCalculator());

        if (Config.CLIENT.fog.get_enableWeatherFog())
            this.fogRange.add(new WeatherFogRangeCalculator());

//		if (this.theme.doFixedFog())
//			this.fogRange
//					.add(new FixedFogRangeCalculator(this.theme.getMinFogDistance(), this.theme.getMaxFogDistance()));

    }

}

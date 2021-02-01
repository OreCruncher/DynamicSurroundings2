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

package org.orecruncher.environs.handlers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.profiler.IProfiler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.orecruncher.environs.config.Config;
import org.orecruncher.environs.fog.*;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.math.LoggingTimerEMA;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FogHandler extends HandlerBase {

    protected final LoggingTimerEMA render = new LoggingTimerEMA("Render Fog");

    protected HolisticFogRangeCalculator fogRange = new HolisticFogRangeCalculator();

    public FogHandler() {
        super("Fog Handler");
    }

    private boolean doFog() {
        return Config.CLIENT.fog.enableFog.get() && CommonState.getDimensionInfo().hasFog();
    }

    @Override
    public void process(@Nonnull final PlayerEntity player) {
        if (doFog()) {
            this.fogRange.tick();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void fogRenderEvent(final EntityViewRenderEvent.RenderFogEvent event) {
        if (event.getType() == FogRenderer.FogType.FOG_TERRAIN && doFog()) {
            final IProfiler profiler = GameUtils.getMC().getProfiler();
            profiler.startSection("Environs Fog Render");
            this.render.begin();
            final FluidState fluidState = event.getInfo().getFluidState();
            if (fluidState.isEmpty()) {
                final FogResult result = this.fogRange.calculate(event);
                GlStateManager.fogStart(result.getStart());
                GlStateManager.fogEnd(result.getEnd());
            }
            this.render.end();
            profiler.endSection();
        }
    }

    @SubscribeEvent
    public void diagnostics(final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get()) {
            if (doFog()) {
                event.getLeft().add("Fog Range: " + this.fogRange.toString());
                event.addRenderTimer(this.render);
            } else
                event.getLeft().add("FOG: IGNORED");
        }
    }

    @Override
    public void onConnect() {
        this.fogRange = new HolisticFogRangeCalculator();

        if (Config.CLIENT.fog.enableBiomeFog.get()) {
            this.fogRange.add(new BiomeFogRangeCalculator());
        }

        if (Config.CLIENT.fog.enableElevationHaze.get())
            this.fogRange.add(new HazeFogRangeCalculator());

        if (Config.CLIENT.fog.enableMorningFog.get()) {
            this.fogRange.add(new MorningFogRangeCalculator());
        }

        if (Config.CLIENT.fog.enableBedrockFog.get())
            this.fogRange.add(new BedrockFogRangeCalculator());

        if (Config.CLIENT.fog.enableWeatherFog.get())
            this.fogRange.add(new WeatherFogRangeCalculator());

//		if (this.theme.doFixedFog())
//			this.fogRange
//					.add(new FixedFogRangeCalculator(this.theme.getMinFogDistance(), this.theme.getMaxFogDistance()));

    }

}

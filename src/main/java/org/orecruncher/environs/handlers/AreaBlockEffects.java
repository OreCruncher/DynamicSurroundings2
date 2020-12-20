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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.scanner.*;
import org.orecruncher.lib.events.BlockUpdateEvent;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.math.LoggingTimerEMA;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
class AreaBlockEffects extends HandlerBase {

    protected final LoggingTimerEMA blockChange = new LoggingTimerEMA("Area Block Update");
    protected ClientPlayerLocus locus;
    protected RandomBlockEffectScanner nearEffects;
    protected RandomBlockEffectScanner farEffects;
    protected AlwaysOnBlockEffectScanner alwaysOn;

    protected long nanos;

    public AreaBlockEffects() {
        super("Area Block Effects");
    }

    @Override
    public void process(@Nonnull final PlayerEntity player) {
        this.nearEffects.tick();
        this.farEffects.tick();
        this.alwaysOn.tick();
        this.blockChange.update(this.nanos);
        this.nanos = 0;
    }

    @Override
    public void onConnect() {
        this.locus = new ClientPlayerLocus();
        this.nearEffects = new RandomBlockEffectScanner(this.locus, RandomBlockEffectScanner.NEAR_RANGE);
        this.farEffects = new RandomBlockEffectScanner(this.locus, RandomBlockEffectScanner.FAR_RANGE);
        this.alwaysOn = new AlwaysOnBlockEffectScanner(this.locus, Config.CLIENT.effects.get_effectRange());
    }

    @Override
    public void onDisconnect() {
        this.locus = null;
        this.nearEffects = null;
        this.farEffects = null;
        this.alwaysOn = null;
    }

    @SubscribeEvent
    public void onDiagnostics(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.get_enableLogging())
            event.addRenderTimer(this.blockChange);
    }

    @SubscribeEvent
    public void onBlockUpdate(@Nonnull final BlockUpdateEvent event) {
        final long start = System.nanoTime();
        event.getExpandedPositions().forEach(this.alwaysOn::onBlockUpdate);
        this.nanos += System.nanoTime() - start;
    }
}

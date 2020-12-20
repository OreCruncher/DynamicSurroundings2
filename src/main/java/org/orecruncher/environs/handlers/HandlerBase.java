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

import com.google.common.base.MoreObjects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import org.orecruncher.lib.math.TimerEMA;
import org.orecruncher.lib.random.XorShiftRandom;

import javax.annotation.Nonnull;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class HandlerBase {

    protected static final Random RANDOM = XorShiftRandom.current();

    private final String handlerName;
    private final TimerEMA timer;

    HandlerBase(@Nonnull final String name) {
        this.handlerName = name;
        this.timer = new TimerEMA(this.handlerName);
    }

    @Nonnull
    public TimerEMA getTimer() {
        return this.timer;
    }

    /**
     * Used to obtain the handler name for logging purposes.
     *
     * @return Name of the handler
     */
    @Nonnull
    public final String getHandlerName() {
        return this.handlerName;
    }

    /**
     * Indicates whether the handler needs to be invoked for the given tick.
     *
     * @return true that the handler needs to be invoked, false otherwise
     */
    public boolean doTick(final long tick) {
        return true;
    }

    /**
     * Meat of the handlers processing logic. Will be invoked if doTick() returns
     * true.
     *
     * @param player The player currently behind the keyboard.
     */
    public void process(@Nonnull final PlayerEntity player) {

    }

    /**
     * Called when the client is connecting to a server. Useful for initializing
     * data to a baseline state.
     */
    public void onConnect() {
    }

    /**
     * Called when the client disconnects from a server. Useful for cleaning up
     * state space.
     */
    public void onDisconnect() {
    }

    //////////////////////////////
    //
    // DO NOT HOOK THESE EVENTS!
    //
    //////////////////////////////
    final void updateTimer(final long nanos) {
        this.timer.update(nanos);
    }

    final void connect0() {
        onConnect();
        MinecraftForge.EVENT_BUS.register(this);
    }

    final void disconnect0() {
        MinecraftForge.EVENT_BUS.unregister(this);
        onDisconnect();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", getHandlerName()).toString();
    }
}

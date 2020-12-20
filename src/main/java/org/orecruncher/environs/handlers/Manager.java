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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.Environs;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.logging.IModLog;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Environs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Manager {

    private static final IModLog LOGGER = Environs.LOGGER.createChild(Manager.class);

    private static final Manager instance_ = new Manager();
    private static boolean isConnected = false;

    public static Manager instance() {
        return instance_;
    }

    private final ObjectArray<HandlerBase> effectHandlers = new ObjectArray<>();

    private Manager() {
        init();
    }

    private void register(@Nonnull final HandlerBase handler) {
        this.effectHandlers.add(handler);
        LOGGER.debug("Registered handler [%s]", handler.getClass().getName());
    }

    private void init() {
        // This has to be first!
        register(new CommonStateHandler());
        register(new AreaBlockEffects());
        register(new BiomeSoundEffects());
        register(new ParticleSystems());
        register(new AuroraHandler());
        register(new FogHandler());
    }

    private void onConnect() {
        for (final HandlerBase h : this.effectHandlers)
            h.connect0();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onDisconnect() {
        MinecraftForge.EVENT_BUS.unregister(this);
        for (final HandlerBase h : this.effectHandlers)
            h.disconnect0();
    }

    public static void connect() {
        if (isConnected) {
            LOGGER.warn("Attempt to initialize EffectManager when it is already initialized");
            disconnect();
        }
        instance_.onConnect();
        isConnected = true;
    }

    public static void disconnect() {
        if (isConnected) {
            instance_.onDisconnect();
            isConnected = false;
        }
    }

    protected static PlayerEntity getPlayer() {
        return GameUtils.getPlayer();
    }

    protected boolean checkReady(@Nonnull final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END || Minecraft.getInstance().isGamePaused())
            return false;
        return GameUtils.isInGame();
    }

    public void onTick(@Nonnull final TickEvent.ClientTickEvent event) {
        if (!checkReady(event))
            return;

        final long tick = TickCounter.getTickCount();

        for (final HandlerBase handler : this.effectHandlers) {
            final long mark = System.nanoTime();
            if (handler.doTick(tick))
                handler.process(getPlayer());
            handler.updateTimer(System.nanoTime() - mark);
        }
    }

    @SubscribeEvent
    public static void diagnosticEvent(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.get_enableLogging())
            instance().effectHandlers.forEach(h -> event.addTimer(h.getTimer()));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void clientTick(@Nonnull final TickEvent.ClientTickEvent event) {
        if (isConnected)
            instance_.onTick(event);
    }
}

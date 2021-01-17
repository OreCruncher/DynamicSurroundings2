/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.lib.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.math.LoggingTimerEMA;
import org.orecruncher.sndctrl.config.Config;
import org.orecruncher.lib.effects.entity.CapabilityEntityFXData;
import org.orecruncher.lib.effects.entity.IEntityFX;
import org.orecruncher.sndctrl.api.effects.AbstractEntityEffect;
import org.orecruncher.sndctrl.library.EntityEffectLibrary;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Central repository for a collection of IEntityEffectFactory instances and the
 * IFactoryFilters associated with them. Typically there will be a single
 * instance of the EntityEffectLibrary for a project, but multiples can be
 * created based on the circumstances.
 */
@Mod.EventBusSubscriber(modid = DynamicSurroundings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EntityEffectHandler {

    private static final LoggingTimerEMA timer = new LoggingTimerEMA("Entity Effect Update");
    private static long nanos;

    private EntityEffectHandler() {
    }

    public static void initialize() {
        // Noop to cause class initializers to fire.
    }

    /**
     * Creates an EntityEffectHandler for the specified Entity. The IEffects
     * attached to the EntityEffectHandler is determined by an IFactoryFitler. An
     * EntityEffectHandler will always be created.
     *
     * @param entity The subject Entity for which an EntityEffectHandler is created
     * @return An EntityEffectHandler for the Entity
     */
    @Nonnull
    private static Optional<EntityEffectManager> create(@Nonnull final LivingEntity entity) {
        final ObjectArray<AbstractEntityEffect> effectToApply = EntityEffectLibrary.getEffects(entity);
        final EntityEffectManager result;
        if (effectToApply.size() > 0) {
            result = new EntityEffectManager(entity, effectToApply);
        } else {
            result = new EntityEffectManager(entity);
        }

        return Optional.of(result);
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onLivingUpdate(@Nonnull final LivingEvent.LivingUpdateEvent event) {
        final LivingEntity entity = event.getEntityLiving();
        if (entity != null && entity.getEntityWorld().isRemote) {

            final long start = System.nanoTime();

            entity.getCapability(CapabilityEntityFXData.FX_INFO).ifPresent(cap -> {
                final int range = Config.CLIENT.effects.effectRange.get();
                final int effectDistSq = range * range;
                final boolean inRange = entity.getDistanceSq(GameUtils.getPlayer()) <= effectDistSq;
                final EntityEffectManager mgr = cap.get();
                if (mgr != null && !inRange) {
                    cap.clear();
                } else if (mgr == null && inRange && entity.isAlive()) {
                    cap.set(create(entity).get());
                } else if (mgr != null) {
                    mgr.update();
                }
            });

            nanos += System.nanoTime() - start;
        }
    }

    private static void clearHandlers() {
        final Iterable<Entity> entities = GameUtils.getWorld().getAllEntities();
        for (final Entity e : entities) {
            e.getCapability(CapabilityEntityFXData.FX_INFO).ifPresent(IEntityFX::clear);
        }
    }

    /**
     * Check if the player joining the world is the one sitting at the keyboard. If
     * so we need to wipe out the existing handler list because the dimension
     * changed.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoin(@Nonnull final EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            if (GameUtils.getPlayer() == event.getEntity())
                clearHandlers();
        }
    }

    @SubscribeEvent
    public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {
        timer.update(nanos);
        nanos = 0;
    }

    @SubscribeEvent
    public static void onDiagnostics(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get())
            event.getRenderTimers().add(timer);
    }

}

/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
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

package org.orecruncher.mobeffects.misc;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.mobeffects.Config;
import org.orecruncher.mobeffects.MobEffects;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = MobEffects.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HidePotionParticlesHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void handler(@Nonnull final TickEvent.ClientTickEvent event) {
        if (Config.CLIENT.effects.get_hidePlayerPotionParticles() && GameUtils.isInGame()) {
            final boolean hide = GameUtils.isFirstPersonView();
            GameUtils.getPlayer().getDataManager().set(LivingEntity.HIDE_PARTICLES, hide);
        }
    }
}


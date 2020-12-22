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

package org.orecruncher.mobeffects.misc;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.lib.effects.EntityEffectManager;
import org.orecruncher.lib.effects.entity.CapabilityEntityFXData;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.sndctrl.events.EntityInspectionEvent;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = MobEffects.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityInspectionHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void handler(@Nonnull final EntityInspectionEvent event) {
        event.entity.getCapability(CapabilityEntityFXData.FX_INFO).ifPresent(cap -> {
            final EntityEffectManager mgr = cap.get();
            if (mgr != null) {
                event.data.add(TextFormatting.DARK_AQUA + "<Effects>");
                event.data.addAll(mgr.getAttachedEffects());
            }
        });
    }

}

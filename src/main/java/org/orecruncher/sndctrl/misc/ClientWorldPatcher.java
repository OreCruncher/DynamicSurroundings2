/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

package org.orecruncher.sndctrl.misc;

import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.sndctrl.config.Config;
import org.orecruncher.sndctrl.SoundControl;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientWorldPatcher {
    private ClientWorldPatcher() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldLoad(@Nonnull final WorldEvent.Load event) {
        if (Config.CLIENT.effects.fixupRandoms.get()) {
            final IWorld world = event.getWorld();
            if (world.isRemote() && world instanceof World) {
                final World w = (World) world;
                w.rand = new XorShiftRandom();
            }
        }
    }
}

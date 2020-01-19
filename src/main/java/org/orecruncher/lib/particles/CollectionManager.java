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

package org.orecruncher.lib.particles;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.math.TimerEMA;
import org.orecruncher.sndctrl.SoundControl;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CollectionManager {
    private CollectionManager() {

    }

    private static final ObjectArray<ParticleCollectionHelper> helpers = new ObjectArray<>();

    public static IParticleCollection create(@Nonnull final String name, @Nonnull final IParticleRenderType renderType) {
        final ParticleCollectionHelper helper = new ParticleCollectionHelper(name, renderType);
        helpers.add(helper);
        return helper;
    }

    @SubscribeEvent
    public static void onWorldUnload(@Nonnull final WorldEvent.Unload event) {
        if (event.getWorld() instanceof ClientWorld) {
            helpers.forEach(ParticleCollectionHelper::clear);
        }
    }

    @SubscribeEvent
    public static void diagnostics(@Nonnull final DiagnosticEvent event) {
        helpers.forEach(h -> {
            event.getLeft().add(TextFormatting.AQUA + h.toString());
            final TimerEMA render = h.getRenderTimer();
            if (render != null)
                event.addRenderTimer(render);
        });
    }

}

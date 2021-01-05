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

package org.orecruncher.sndctrl.audio.handlers;

import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.sndctrl.api.acoustics.IAcousticFactory;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;
import org.orecruncher.sndctrl.config.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.*;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.library.Primitives;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoundEventHandling {
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundEventHandling.class);
    private static boolean hasPlayed = false;

    private SoundEventHandling() {
    }

    @SubscribeEvent
    public static void onGuiOpen(@Nonnull final GuiOpenEvent event) {
        if (!hasPlayed && event.getGui() instanceof MainMenuScreen) {

            hasPlayed = true;

            final List<String> possibles = Config.CLIENT.sound.startupSoundList.get()
                    .stream()
                    .map(StringUtils::trim)
                    .filter(s -> s.length() > 0)
                    .collect(Collectors.toList());

            if (possibles.size() == 0)
                return;

            final String res;
            if (possibles.size() == 1) {
                res = possibles.get(0);
            } else {
                res = possibles.get(XorShiftRandom.current().nextInt(possibles.size()));
            }

            final ResourceLocation rl = new ResourceLocation(res);
            final IAcoustic acoustic = Primitives.getSound(rl, Category.MASTER);
            IAcousticFactory factory = acoustic.getFactory();
            if (factory != null) {
                final ISoundInstance instance = new PlayerCenteredSoundInstance(acoustic.getFactory().createSound(), Category.MASTER);
                GameUtils.getMC().enqueue(() -> {
                    try {
                        AudioEngine.play(instance);
                    } catch (@Nonnull final Throwable t) {
                        LOGGER.error(t, "Error executing startup sound '%s'", rl.toString());
                    }
                });
            }
        }
    }
}

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

package org.orecruncher.sndctrl.audio.handlers;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Listener;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.sndctrl.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoundEventHandling {
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundEventHandling.class);
    private static final float MUTE_VOLUME = 0.00001F;
    private static boolean hasPlayed = false;

    private SoundEventHandling() {
    }

    @SubscribeEvent
    public static void onGuiOpen(@Nonnull final GuiOpenEvent event) {
        if (!hasPlayed && event.getGui() instanceof MainMenuScreen) {

            hasPlayed = true;

            //@formatter:off
            final List<String> possibles = Config.CLIENT.sound.startupSoundList.get()
                    .stream()
                    .map(StringUtils::trim)
                    .filter(s -> s.length() > 0)
                    .collect(Collectors.toList());
            //@formatter:on

            if (possibles.size() == 0)
                return;

            final String res;
            if (possibles.size() == 1) {
                res = possibles.get(0);
            } else {
                res = possibles.get(XorShiftRandom.current().nextInt(possibles.size()));
            }

            final SoundEvent se = SoundRegistry.getSound(new ResourceLocation(res)).orElse(null);
            if (se != null) {
                final ISoundInstance sound = SoundBuilder.builder(se, SoundCategory.MASTER)
                        .setAttenuation(ISound.AttenuationType.NONE)
                        .build();

                // Queue it up on the main client thread.
                GameUtils.getMC().enqueue(() -> {
                    try {
                        AudioEngine.play(sound);
                    } catch (@Nonnull final Throwable t) {
                        LOGGER.error(t, "Error executing startup sound '%s'", se.toString());
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void clientTick(@Nonnull final TickEvent.ClientTickEvent event) {
        if (event.side != LogicalSide.CLIENT || event.phase == TickEvent.Phase.END)
            return;

        if (Config.CLIENT.sound.muteInBackground.get()) {
            final boolean active = GameUtils.getMC().isGameFocused();
            final boolean muted = isMuted();
            if (active && muted) {
                setMuted(false);
                LOGGER.debug("Unmuting sounds");
            } else if (!active && !muted) {
                setMuted(true);
                LOGGER.debug("Muting sounds");
            }
        }
    }

    private static boolean isMuted() {
        return SoundUtils.getMasterGain() == MUTE_VOLUME;
    }

    private static void setMuted(final boolean flag) {
        final Listener listener = SoundUtils.getListener();
        if (flag) {
            listener.setGain(MUTE_VOLUME);
        } else {
            final GameSettings options = GameUtils.getGameSettings();
            listener.setGain(options.getSoundLevel(SoundCategory.MASTER));
        }
    }
}

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

package org.orecruncher.dsurround.gui;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.dsurround.huds.lightlevel.LightLevelHUD;
import org.orecruncher.lib.GameUtils;

@Mod.EventBusSubscriber(modid = DynamicSurroundings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Keys {

    private static final KeyBinding lightLevelHUD;
    private static final KeyBinding chunkBorders;

    static {
        lightLevelHUD = new KeyBinding(
                "dsurround.text.lightlevel.toggle",
                InputMappings.INPUT_INVALID.getKeyCode(),
                "dsurround.text.controls.group");
        chunkBorders = new KeyBinding(
                "dsurround.text.chunkborders.toggle",
                InputMappings.INPUT_INVALID.getKeyCode(),
                "dsurround.text.controls.group");
        ClientRegistry.registerKeyBinding(lightLevelHUD);
        ClientRegistry.registerKeyBinding(chunkBorders);
    }

    @SubscribeEvent
    public static void keyPressed(InputEvent.KeyInputEvent event) {
        if (GameUtils.getMC().currentScreen == null && GameUtils.getPlayer() != null) {
            if (lightLevelHUD.isPressed()) {
                LightLevelHUD.toggleDisplay();
            }

            if (chunkBorders.isPressed()) {
                GameUtils.getMC().debugRenderer.toggleChunkBorders();
            }
        }
    }
}
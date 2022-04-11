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

package org.orecruncher.lib;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class GameUtils {
    private GameUtils() {

    }

    // Client methods
    @Nullable
    public static PlayerEntity getPlayer() {
        return getMC().player;
    }

    @Nullable
    public static ClientWorld getWorld() {
        return getMC().world;
    }

    @Nonnull
    public static Minecraft getMC() {
        return Minecraft.getInstance();
    }

    @Nonnull
    public static GameSettings getGameSettings() {
        return getMC().gameSettings;
    }

    public static boolean displayDebug() {
        return getGameSettings().showDebugInfo;
    }

    @Nonnull
    public static SoundHandler getSoundHander() {
        return getMC().getSoundHandler();
    }

    public static boolean isInGame() {
        return getWorld() != null && getPlayer() != null;
    }

    public static boolean isThirdPersonView() {
        return getGameSettings().getPointOfView() != PointOfView.FIRST_PERSON;
    }

    public static boolean isFirstPersonView() {
        return getGameSettings().getPointOfView() == PointOfView.FIRST_PERSON;
    }
}

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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class GameUtils {
    private GameUtils() {

    }

    // Client methods
    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static PlayerEntity getPlayer() {
        return getMC().player;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static World getWorld() {
        return getMC().world;
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static Minecraft getMC() {
        return Minecraft.getInstance();
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static GameSettings getGameSettings() {
        return getMC().gameSettings;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean displayDebug() {
        return getGameSettings().showDebugInfo;
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static SoundHandler getSoundHander() {
        return getMC().getSoundHandler();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isInGame() {
        return getWorld() != null && getPlayer() != null;
    }
}

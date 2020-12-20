/*
 * Dynamic Surroundings: Mob Effects
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

package org.orecruncher.mobeffects.compat;

import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;

public enum ModEnvironment {

    SereneSeasons("sereneseasons"),
    EnderIO("EnderIO"),
    Chisel("chisel"),
    ChiselAPI("ctm-api"),
    CoFHCore("cofhcore"),
    CosmeticArmorReworked("cosmeticarmorreworked"),
    ForgeMultipartCBE("forgemultipartcbe"),
    ConnectedTextures("ctm"),
    LittleTiles("littletiles"),
    ConstructArmory("conarm");

    protected final String modId;
    protected boolean isLoaded;

    ModEnvironment(@Nonnull final String modId) {
        this.modId = modId;
    }

    public static void initialize() {
        for (final ModEnvironment me : ModEnvironment.values())
            me.isLoaded = ModList.get().isLoaded(me.modId.toLowerCase());
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

}

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

package org.orecruncher.lib;

import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;

public enum ModEnvironment {

    SereneSeasons("sereneseasons"),
    ClothConfig("me.shedaniel.clothconfig2.forge.api.ConfigBuilder", true);

    protected final String modId;
    protected boolean isLoaded;
    protected boolean isAPI;

    ModEnvironment(@Nonnull final String modId) {
        this(modId, false);
    }

    ModEnvironment(@Nonnull final String modId, final boolean isAPI) {
        this.modId = modId;
        this.isAPI = isAPI;
    }

    static {
        for (final ModEnvironment me : ModEnvironment.values()) {
            if (me.isAPI) {
                try {
                    Class<?> clazz = Class.forName(me.modId);
                    me.isLoaded = true;
                } catch (@Nonnull final Throwable t) {
                    me.isLoaded = false;
                }
            } else {
                me.isLoaded = ModList.get().isLoaded(me.modId.toLowerCase());
            }
        }
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

}

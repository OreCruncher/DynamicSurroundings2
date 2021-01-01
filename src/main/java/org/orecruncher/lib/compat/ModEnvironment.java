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

package org.orecruncher.lib.compat;

import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;

public enum ModEnvironment {

    SoundPhysics("soundphysics"),
    SoundFilters("soundfilters"),
    SereneSeasons("sereneseasons"),
    ClothAPI("me.shedaniel.clothconfig2.forge.api.ConfigBuilder");

    protected final String modId;
    protected boolean isLoaded;

    ModEnvironment(@Nonnull final String modId) {
        this.modId = modId;
    }

    static {
        for (final ModEnvironment me : ModEnvironment.values()) {
            me.isLoaded = ModList.get().isLoaded(me.modId.toLowerCase());

            if (!me.isLoaded) {
                try {
                    final Class<?> clazz = Class.forName(me.modId);
                    me.isLoaded = true;
                } catch(@Nonnull Throwable ignore) {
                }
            }
        }
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

}

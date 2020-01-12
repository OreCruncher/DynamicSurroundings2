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

package org.orecruncher.lib.scripting;

import javax.annotation.Nonnull;

/**
 * A VariableSet is used to insert instances into the JavaScript runtime environment so that scripts can access game
 * and mod data safely.  For example, data related to the player can be encapsulated into a player data variable set,
 * and have that data updated once per tick.  This ticking allows for the calculation and caching of values that are
 * expensive to calculate and reused repeatedly through the tick.
 *
 * @param <T>
 */
public abstract class VariableSet<T> {

    @Nonnull
    private final String setName;

    protected VariableSet(@Nonnull final String setName) {
        this.setName = setName;
    }

    @Nonnull
    public String getSetName() {
        return this.setName;
    }

    public void update() {

    }

    /**
     * Produces a class instance that will be inserted into the JavaScript runtime so that scripts can access.  The
     * class should only have accessors on the interface and avoid state changing methods.
     *
     * @return Instance that can be registered with the JavaScript engine
     */
    @Nonnull
    public abstract T getInterface();

}

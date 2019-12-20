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

package org.orecruncher.lib.scripting.sets;

import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.scripting.VariableSet;

import javax.annotation.Nonnull;

public class DimensionVariables extends VariableSet<IDimensionVariables> implements IDimensionVariables {

    private final LazyVariable<Integer> id = new LazyVariable<>(() -> GameUtils.isInGame() ? GameUtils.getWorld().getDimension().getType().getId() : 0);
    private final LazyVariable<String> name = new LazyVariable<>(() -> GameUtils.isInGame() ? GameUtils.getWorld().getProviderName() : "UNKNOWN");
    private final LazyVariable<Boolean> hasSky = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getWorld().getDimension().hasSkyLight());

    public DimensionVariables() {
        super("dim");
    }

    @Nonnull
    @Override
    public IDimensionVariables getInterface() {
        return this;
    }

    @Override
    public void update() {
        this.id.reset();
        this.name.reset();
        this.hasSky.reset();
    }

    @Override
    public int getId() {
        return this.id.get();
    }

    @Override
    public String getDimName() {
        return this.name.get();
    }

    @Override
    public boolean hasSky() {
        return this.hasSky.get();
    }
}

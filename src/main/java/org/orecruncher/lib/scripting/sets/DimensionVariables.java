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

import net.minecraft.world.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.scripting.VariableSet;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class DimensionVariables extends VariableSet<IDimensionVariables> implements IDimensionVariables {

    private String id;
    private String name;
    private boolean hasSky;

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
        if (GameUtils.isInGame()) {
            final DimensionType dim = GameUtils.getWorld().getDimensionType();
            this.id = GameUtils.getWorld().getDimensionKey().getRegistryName().toString();
            this.hasSky = dim.hasSkyLight();
            // TODO: What is the right way to get the dimension resource location?
            this.name = GameUtils.getWorld().getProviderName();
        } else {
            this.id = "UNKNOWN";
            this.hasSky = false;
            this.name = "UNKNOWN";
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDimName() {
        return this.name;
    }

    @Override
    public boolean hasSky() {
        return this.hasSky;
    }
}

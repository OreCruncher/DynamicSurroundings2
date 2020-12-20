/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.handlers.scripts;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.handlers.CommonState;
import org.orecruncher.lib.scripting.VariableSet;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
class StateVariables extends VariableSet<IStateVariables> implements IStateVariables {

    public StateVariables() {
        super("state");
    }

    @Nonnull
    @Override
    public IStateVariables getInterface() {
        return this;
    }

    public void update() {

    }

    @Override
    public boolean isInside() {
        return CommonState.isInside();
    }

    @Override
    public float getCurrentTemperature() {
        return CommonState.getCurrentTemperature();
    }

    @Override
    public boolean isUnderground() {
        return CommonState.isUnderground();
    }

    @Override
    public boolean isInClouds() {
        return CommonState.isInClouds();
    }

    @Override
    public boolean isInSpace() {
        return CommonState.isInSpace();
    }

    @Override
    public boolean isInVillage() {
        return CommonState.isInVillage();
    }

    @Override
    public int getLightLevel() {
        return CommonState.getLightLevel();
    }
}

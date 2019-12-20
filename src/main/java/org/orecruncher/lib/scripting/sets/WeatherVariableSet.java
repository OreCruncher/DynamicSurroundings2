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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.scripting.VariableSet;

import javax.annotation.Nonnull;

public class WeatherVariableSet extends VariableSet<IWorldVariables> implements IWorldVariables {

    private final LazyVariable<Boolean> isRaining = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getWorld().isRaining());
    private final LazyVariable<Boolean> isThundering = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getWorld().isThundering());
    private final LazyVariable<Float> rainFall = new LazyVariable<>(() -> GameUtils.isInGame() ? GameUtils.getWorld().getRainStrength(1F) : 0F);
    private final LazyVariable<Float> temperature = new LazyVariable<>(() -> {
        if (GameUtils.isInGame()) {
            final World world = GameUtils.getWorld();
            final BlockPos pos = GameUtils.getPlayer().getPosition();
            return world.getBiome(pos).getTemperature(pos);
        }
        return 0F;
    });

    public WeatherVariableSet() {
        super("weather");
    }

    @Nonnull
    @Override
    public IWorldVariables getInterface() {
        return this;
    }

    @Override
    public void update() {
        this.isRaining.reset();
        this.isThundering.reset();
        this.rainFall.reset();
        this.temperature.reset();
    }

    @Override
    public boolean isRaining() {
        return this.isRaining.get();
    }

    @Override
    public boolean isThundering() {
        return this.isThundering.get();
    }

    @Override
    public float getRainFall() {
        return this.rainFall.get();
    }

    @Override
    public float getTemperature() {
        return this.temperature.get();
    }
}

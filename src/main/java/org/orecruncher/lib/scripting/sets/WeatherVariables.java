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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.scripting.VariableSet;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class WeatherVariables extends VariableSet<IWeatherVariables> implements IWeatherVariables {

    private final LazyVariable<Float> temperature = new LazyVariable<>(() -> {
        if (GameUtils.isInGame()) {
            final World world = GameUtils.getWorld();
            final BlockPos pos = GameUtils.getPlayer().getPosition();
            return WorldUtils.getTemperatureAt(world, pos);
        }
        return 0F;
    });
    private boolean isRaining;
    private boolean isThundering;
    private float rainIntensity;
    private float thunderIndensity;

    public WeatherVariables() {
        super("weather");
    }

    @Nonnull
    @Override
    public IWeatherVariables getInterface() {
        return this;
    }

    @Override
    public void update() {
        if (GameUtils.isInGame()) {
            final World world = GameUtils.getWorld();
            this.rainIntensity = WorldUtils.getRainStrength(world, 1F);
            this.thunderIndensity = WorldUtils.getThunderStrength(world, 1F);
            this.isRaining = WorldUtils.isRaining(world);
            this.isThundering = WorldUtils.isThundering(world);
        } else {
            this.rainIntensity = 0F;
            this.thunderIndensity = 0F;
            this.isRaining = false;
            this.isThundering = false;
        }
        this.temperature.reset();
    }

    @Override
    public boolean isRaining() {
        return this.isRaining;
    }

    @Override
    public boolean isThundering() {
        return this.isThundering;
    }

    @Override
    public float getRainIntensity() {
        return this.rainIntensity;
    }

    @Override
    public float getThunderIntensity() {
        return this.thunderIndensity;
    }

    @Override
    public float getTemperature() {
        return this.temperature.get();
    }
}

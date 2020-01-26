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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.WorldUtils;

@OnlyIn(Dist.CLIENT)
public interface IWeatherVariables {

    /**
     * Is it currently raining in the player world
     *
     * @return true if it is raining, false otherwise
     */
    boolean isRaining();

    /**
     * Is it currently thundering in the player world
     *
     * @return true if it is thundering, false otherwise
     */
    boolean isThundering();

    /**
     * Inverse of isRaining();
     *
     * @return true if it is not raining, false otherwise
     */
    default boolean isNotRaining() {
        return !isRaining();
    }

    /**
     * Inverse of isThundering()
     *
     * @return true if it is not thundering, false otherwise
     */
    default boolean isNotThundering() {
        return !isThundering();
    }

    /**
     * Get the current rain intensity
     *
     * @return 0 - 1
     */
    float getRainIntensity();

    /**
     * Get the current thunder intensity
     *
     * @return 0 - 1
     */
    float getThunderIntensity();

    /**
     * Gets the temperature at the current player location
     * @return
     */
    float getTemperature();

    /**
     * Indicates if the temperature at the player location is cold enough to show frost breath, etc.
     *
     * @return true if the current temperature conditions are frosty, false otherwise
     */
    default boolean isFrosty() {
        return WorldUtils.isColdTemperature(getTemperature());
    }

    /**
     * Indicaets if the temperature at the player location is cold enough for water to freeze.
     *
     * @return true if the current temperature allows water freezing, false otherwise.
     */
    default boolean canWaterFreeze() {
        return WorldUtils.isSnowTemperature(getTemperature());
    }
}

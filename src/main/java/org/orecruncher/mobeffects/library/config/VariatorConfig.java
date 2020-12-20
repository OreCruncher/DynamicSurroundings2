/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
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

package org.orecruncher.mobeffects.library.config;

import com.google.gson.annotations.SerializedName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VariatorConfig {

    @SerializedName("immobileDuration")
    public int immobileDuration = 4;
    @SerializedName("eventOnJump")
    public boolean eventOnJump = true;
    @SerializedName("landHardDistanceMin")
    public float landHardDistanceMin = 0.9F;
    @SerializedName("speedToJumpAsMultifoot")
    public float speedToJumpAsMultifoot = 0.005F;
    @SerializedName("speedToRun")
    public float speedToRun = 0.22F; // 0.022F; slow

    @SerializedName("stride")
    public float stride = 0.75F; // 0.95F; slow
    @SerializedName("strideStair")
    public float strideStair = this.stride * 0.65F;
    @SerializedName("strideLadder")
    public float strideLadder = 0.5F;
    @SerializedName("quadrupedMultiplier")
    public float quadrupedMultiplier = 1.25F; // 0.925; slow

    @SerializedName("playWander")
    public boolean playWander = true;
    @SerializedName("quadruped")
    public boolean quadruped = false;
    @SerializedName("playJump")
    public boolean playJump = false;
    @SerializedName("distanceToCenter")
    public float distanceToCenter = 0.2F;
    @SerializedName("hasFootprint")
    public boolean hasFootprint = true;
    @SerializedName("footprintStyle")
    public int footprintStyle = 6;
    @SerializedName("footprintScale")
    public float footprintScale = 1.0F;
    @SerializedName("volumeScale")
    public float volumeScale = 1.0F;
}

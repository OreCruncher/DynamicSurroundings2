/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2020 OreCruncher
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

package org.orecruncher.sndctrl.mixins;

import net.minecraft.client.audio.SoundSystem;
import org.orecruncher.sndctrl.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SoundSystem.class)
public class MixinSoundSystem {

    /**
     * Modify the number of streaming sounds that can be handled by the underlying sound engine.  Normally it
     * allows for 8.  This mixin will change to 10.
     * @param v Existing value for the number of streaming sounds (should be 8)
     * @return The quantity of streaming sounds (10)
     */
    @ModifyConstant(method = "init()V", constant = @Constant(intValue = 8))
    private int initialize(int v) {
        return Config.CLIENT.sound.streamingSoundCount.get();
    }
}

/*
 *  Dynamic Surroundings
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

package org.orecruncher.environs.library;

import org.orecruncher.environs.config.Config;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;

public final class Constants {
    private Constants() {

    }

    public static final ISoundCategory BIOMES = new Category("biomes", "environs.soundcategory.biomes", () -> Config.CLIENT.sound.biomeSoundVolume.get() / 100F, (v) -> Config.CLIENT.sound.biomeSoundVolume.set((int)(v * 100)));
    public static final ISoundCategory SPOT_SOUNDS = new Category("spot", "environs.soundcategory.spotsounds", () -> Config.CLIENT.sound.spotSoundVolume.get() / 100F, (v) -> Config.CLIENT.sound.spotSoundVolume.set((int)(v * 100)));
    public static final ISoundCategory WATERFALL = new Category("waterfall", "environs.soundcategory.waterfall",() -> Config.CLIENT.sound.waterfallSoundVolume.get() / 100F, (v) -> Config.CLIENT.sound.waterfallSoundVolume.set((int)(v * 100)), Config.CLIENT.sound.occludeWaterfall::get);
}

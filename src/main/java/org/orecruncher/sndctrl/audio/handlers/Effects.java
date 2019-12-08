/*
 * Dynamic Surroundings: Sound Control
 * Sound Physics
 * Copyright (C) 2019  OreCruncher
 * Copyright SonicEther
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
 *
 * Summary of changes:
 *
 * - Encapsulate EXTFx calls into separate classes
 * - Initialization of data
 */

package org.orecruncher.sndctrl.audio.handlers;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.audio.handlers.effects.AuxSlot;
import org.orecruncher.sndctrl.audio.handlers.effects.LowPassFilterSlot;
import org.orecruncher.sndctrl.audio.handlers.effects.ReverbData;
import org.orecruncher.sndctrl.audio.handlers.effects.ReverbEffectSlot;

@OnlyIn(Dist.CLIENT)
public final class Effects {
    // General config settings that need to make their way somewhere
    private static final float ROLLOFF_FACTOR = 1F;
    private static final float GLOBAL_REVERB_MULTIPLIER = 0.7F;
    private static final float GLOBAL_REVERB_BRIGHTNESS = 1F;

    public static final float GLOBAL_BLOCK_ABSORPTION = 1F;
    public static final float SNOW_AIR_ABSORPTION_FACTOR = 5F;
    public static final float RAIN_AIR_ABSORPTION_FACTOR = 2F;

    public static final ReverbData reverbData0;
    public static final ReverbData reverbData1;
    public static final ReverbData reverbData2;
    public static final ReverbData reverbData3;
    public static final AuxSlot auxSlot0 = new AuxSlot();
    public static final AuxSlot auxSlot1 = new AuxSlot();
    public static final AuxSlot auxSlot2 = new AuxSlot();
    public static final AuxSlot auxSlot3 = new AuxSlot();
    public static final ReverbEffectSlot reverb0 = new ReverbEffectSlot();
    public static final ReverbEffectSlot reverb1 = new ReverbEffectSlot();
    public static final ReverbEffectSlot reverb2 = new ReverbEffectSlot();
    public static final ReverbEffectSlot reverb3 = new ReverbEffectSlot();
    public static final LowPassFilterSlot filter0 = new LowPassFilterSlot();
    public static final LowPassFilterSlot filter1 = new LowPassFilterSlot();
    public static final LowPassFilterSlot filter2 = new LowPassFilterSlot();
    public static final LowPassFilterSlot filter3 = new LowPassFilterSlot();
    public static final LowPassFilterSlot direct = new LowPassFilterSlot();

    static {
        reverbData0 = new ReverbData();
        reverbData0.decayTime = 0.15F;
        reverbData0.density = 0.0F;
        reverbData0.diffusion = 1.0F;
        reverbData0.gain = 0.2F * 0.85F * GLOBAL_REVERB_MULTIPLIER;
        reverbData0.gainHF = 0.99F;
        reverbData0.decayHFRatio = 0.6F * GLOBAL_REVERB_BRIGHTNESS;
        reverbData0.reflectionsGain = 2.5F;
        reverbData0.reflectionsDelay = 0.001F;
        reverbData0.lateReverbGain = 1.26F;
        reverbData0.lateReverbDelay = 0.011F;
        reverbData0.airAbsorptionGainHF = 0.994F;
        reverbData0.roomRolloffFactor = 0.16F * ROLLOFF_FACTOR;

        reverbData1 = new ReverbData();
        reverbData1.decayTime = 0.55F;
        reverbData1.density = 0.0F;
        reverbData1.diffusion = 1.0F;
        reverbData1.gain = 0.3F * 0.85F * GLOBAL_REVERB_MULTIPLIER;
        reverbData1.gainHF = 0.99F;
        reverbData1.decayHFRatio = 0.7F * GLOBAL_REVERB_BRIGHTNESS;
        reverbData1.reflectionsGain = 0.2F;
        reverbData1.reflectionsDelay = 0.015F;
        reverbData1.lateReverbGain = 1.26F;
        reverbData1.lateReverbDelay = 0.011F;
        reverbData1.airAbsorptionGainHF = 0.994F;
        reverbData1.roomRolloffFactor = 0.15F * ROLLOFF_FACTOR;

        reverbData2 = new ReverbData();
        reverbData2.decayTime = 1.68F;
        reverbData2.density = 0.1F;
        reverbData2.diffusion = 1.0F;
        reverbData2.gain = 0.5F * 0.85F * GLOBAL_REVERB_MULTIPLIER;
        reverbData2.gainHF = 0.99F;
        reverbData2.decayHFRatio = 0.7F * GLOBAL_REVERB_BRIGHTNESS;
        reverbData2.reflectionsGain = 0.0F;
        reverbData2.reflectionsDelay = 0.021F;
        reverbData2.lateReverbGain = 1.26F;
        reverbData2.lateReverbDelay = 0.021F;
        reverbData2.airAbsorptionGainHF = 0.994F;
        reverbData2.roomRolloffFactor = 0.13F * ROLLOFF_FACTOR;

        reverbData3 = new ReverbData();
        reverbData3.decayTime = 4.142F;
        reverbData3.density = 0.5F;
        reverbData3.diffusion = 1.0F;
        reverbData3.gain = 0.4F * 0.85F * GLOBAL_REVERB_MULTIPLIER;
        reverbData3.gainHF = 0.89F;
        reverbData3.decayHFRatio = 0.7F * GLOBAL_REVERB_BRIGHTNESS;
        reverbData3.reflectionsGain = 0.0F;
        reverbData3.reflectionsDelay = 0.025F;
        reverbData3.lateReverbGain = 1.26F;
        reverbData3.lateReverbDelay = 0.021F;
        reverbData3.airAbsorptionGainHF = 0.994F;
        reverbData3.roomRolloffFactor = 0.11F * ROLLOFF_FACTOR;
    }

    private Effects() {

    }

    public static void initialize() {
        auxSlot0.initialize();
        auxSlot1.initialize();
        auxSlot2.initialize();
        auxSlot3.initialize();

        reverb0.initialize();
        reverb1.initialize();
        reverb2.initialize();
        reverb3.initialize();

        filter0.initialize();
        filter1.initialize();
        filter2.initialize();
        filter3.initialize();

        direct.initialize();

        reverbData0.setProcess(true);
        reverbData1.setProcess(true);
        reverbData2.setProcess(true);
        reverbData3.setProcess(true);

        reverb0.apply(reverbData0, auxSlot0);
        reverb1.apply(reverbData1, auxSlot1);
        reverb2.apply(reverbData2, auxSlot2);
        reverb3.apply(reverbData3, auxSlot3);
    }
}

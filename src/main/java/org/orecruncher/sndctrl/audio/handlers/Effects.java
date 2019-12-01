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
    public static final float rolloffFactor = 1F;
    public static final float globalVolumeMultiplier = 4.0f;
    public static final float globalReverbMultiplier = 0.7f;
    public static final double soundDistanceAllowance = 4F;
    public static final float globalReverbBrightness = 1F;
    public static final float globalBlockAbsorption = 1F;
    public static final float SNOW_AIR_ABSORPTION_FACTOR = 5F;
    public static final float RAIN_AIR_ABSORPTION_FACTOR = 2F;
    public static final float underwaterFilter = 0.8F;
    public static final boolean simplerSharedAirspaceSimulation = false;

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
        reverbData0.decayTime = 0.15f;
        reverbData0.density = 0.0f;
        reverbData0.diffusion = 1.0f;
        reverbData0.gain = 0.2f * 0.85f * globalReverbMultiplier;
        reverbData0.gainHF = 0.99f;
        reverbData0.decayHFRatio = 0.6f * globalReverbBrightness;
        reverbData0.reflectionsGain = 2.5f;
        reverbData0.reflectionsDelay = 0.001f;
        reverbData0.lateReverbGain = 1.26f;
        reverbData0.lateReverbDelay = 0.011f;
        reverbData0.airAbsorptionGainHF = 0.994f;
        reverbData0.roomRolloffFactor = 0.16f * rolloffFactor;

        reverbData1 = new ReverbData();
        reverbData1.decayTime = 0.55f;
        reverbData1.density = 0.0f;
        reverbData1.diffusion = 1.0f;
        reverbData1.gain = 0.3f * 0.85F * globalReverbMultiplier;
        reverbData1.gainHF = 0.99f;
        reverbData1.decayHFRatio = 0.7f * globalReverbBrightness;
        reverbData1.reflectionsGain = 0.2f;
        reverbData1.reflectionsDelay = 0.015f;
        reverbData1.lateReverbGain = 1.26f;
        reverbData1.lateReverbDelay = 0.011f;
        reverbData1.airAbsorptionGainHF = 0.994f;
        reverbData1.roomRolloffFactor = 0.15f * rolloffFactor;

        reverbData2 = new ReverbData();
        reverbData2.decayTime = 1.68f;
        reverbData2.density = 0.1f;
        reverbData2.diffusion = 1.0f;
        reverbData2.gain = 0.5f * 0.85F * globalReverbMultiplier;
        reverbData2.gainHF = 0.99f;
        reverbData2.decayHFRatio = 0.7f * globalReverbBrightness;
        reverbData2.reflectionsGain = 0.0f;
        reverbData2.reflectionsDelay = 0.021f;
        reverbData2.lateReverbGain = 1.26f;
        reverbData2.lateReverbDelay = 0.021f;
        reverbData2.airAbsorptionGainHF = 0.994f;
        reverbData2.roomRolloffFactor = 0.13f * rolloffFactor;

        reverbData3 = new ReverbData();
        reverbData3.decayTime = 4.142f;
        reverbData3.density = 0.5f;
        reverbData3.diffusion = 1.0f;
        reverbData3.gain = 0.4f * 0.85F * globalReverbMultiplier;
        reverbData3.gainHF = 0.89f;
        reverbData3.decayHFRatio = 0.7f * globalReverbBrightness;
        reverbData3.reflectionsGain = 0.0f;
        reverbData3.reflectionsDelay = 0.025f;
        reverbData3.lateReverbGain = 1.26f;
        reverbData3.lateReverbDelay = 0.021f;
        reverbData3.airAbsorptionGainHF = 0.994f;
        reverbData3.roomRolloffFactor = 0.11f * rolloffFactor;
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

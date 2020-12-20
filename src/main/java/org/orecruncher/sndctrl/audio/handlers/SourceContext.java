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

import com.google.common.base.MoreObjects;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.openal.EXTEfx;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.random.LCGRandom;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.SoundUtils;
import org.orecruncher.sndctrl.audio.handlers.effects.LowPassData;
import org.orecruncher.sndctrl.audio.handlers.effects.SourcePropertyFloat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ForkJoinTask;

/**
 * Used to track and apply sound effects for a given sound instance in the sound engine.  It is also a task to
 * facilitate queuing to background processing.
 */
@OnlyIn(Dist.CLIENT)
public final class SourceContext extends ForkJoinTask<Void> {

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SourceContext.class);
    // Lightweight randomizer used to distribute updates across an interval
    private static final LCGRandom RANDOM = new LCGRandom();
    // Frequency of sound effect updates in thread schedule ticks.  Works out to be 3 times a second.
    private static final int UPDATE_FEQUENCY_TICKS = 7;

    @Nonnull
    private final Object sync = new Object();
    @Nonnull
    private final LowPassData lowPass0;
    @Nonnull
    private final LowPassData lowPass1;
    @Nonnull
    private final LowPassData lowPass2;
    @Nonnull
    private final LowPassData lowPass3;
    @Nonnull
    private final LowPassData direct;
    @Nonnull
    private final SourcePropertyFloat airAbsorb;

    @Nullable
    private ISound sound;
    @Nonnull
    private Vector3d pos;

    private boolean isEnabled;
    private int updateCount;

    public SourceContext() {
        this.lowPass0 = new LowPassData();
        this.lowPass1 = new LowPassData();
        this.lowPass2 = new LowPassData();
        this.lowPass3 = new LowPassData();
        this.direct = new LowPassData();
        this.airAbsorb = new SourcePropertyFloat(EXTEfx.AL_AIR_ABSORPTION_FACTOR, EXTEfx.AL_DEFAULT_AIR_ABSORPTION_FACTOR, EXTEfx.AL_MIN_AIR_ABSORPTION_FACTOR, EXTEfx.AL_MAX_AIR_ABSORPTION_FACTOR);
        this.pos = Vector3d.ZERO;
    }

    public Object sync() {
        return this.sync;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void enable() {
        this.isEnabled = true;
    }

    @Nonnull
    public LowPassData getLowPass0() {
        return this.lowPass0;
    }

    @Nonnull
    public LowPassData getLowPass1() {
        return this.lowPass1;
    }

    @Nonnull
    public LowPassData getLowPass2() {
        return this.lowPass2;
    }

    @Nonnull
    public LowPassData getLowPass3() {
        return this.lowPass3;
    }

    @Nonnull
    public LowPassData getDirect() {
        return this.direct;
    }

    @Nonnull
    public SourcePropertyFloat getAirAbsorb() {
        return this.airAbsorb;
    }

    @Nonnull
    public Vector3d getPosition() {
        return this.pos;
    }

    public int getAttenuationDistance() {
        return this.sound != null ? this.sound.getSound().getAttenuationDistance() : -1;
    }

    public void attachSound(@Nonnull final ISound sound) {
        this.sound = sound;
        captureState();
    }

    @Nullable
    public ISound getSound() {
        return this.sound;
    }

    /**
     * Called on the SoundSource update thread when updating status.  Do not call from the client thread or bad things
     * can happen.
     */
    public void tick(final int sourceId) {
        if (isEnabled()) {
            synchronized (this.sync()) {
                // Upload the data
                Effects.filter0.apply(sourceId, this.lowPass0, 0, Effects.auxSlot0);
                Effects.filter1.apply(sourceId, this.lowPass1, 1, Effects.auxSlot1);
                Effects.filter2.apply(sourceId, this.lowPass2, 2, Effects.auxSlot2);
                Effects.filter3.apply(sourceId, this.lowPass3, 3, Effects.auxSlot3);
                Effects.direct.apply(sourceId, this.direct);

                this.airAbsorb.apply(sourceId);

                SoundFXProcessor.validate("SourceHandler::tick");
            }
        }
    }

    /**
     * Called by the sound processing thread when scheduling work items for sound updates.  This routine should only
     * be called by the background thread.
     *
     * @return true the work item should be scheduled; false otherwise
     */
    public boolean shouldExecute() {
        // If brand new randomize the start time
        if (this.updateCount == 0) {
            this.updateCount = RANDOM.nextInt(UPDATE_FEQUENCY_TICKS);
        }
        return (this.updateCount++ % UPDATE_FEQUENCY_TICKS) == 0;
    }

    // Ignored
    public final Void getRawResult() { return null; }
    // Ignored
    public final void setRawResult(Void x) {}

    /**
     * Called by the thread pool when executing the task
     *
     * @return Will always return true
     */
    @Override
    public final boolean exec() {
        captureState();
        updateImpl();
        return true;
    }

    private void updateImpl() {
        try {
            SoundFXUtils.calculate(SoundFXProcessor.getWorldContext(), this);
        } catch(@Nonnull final Throwable t) {
            LOGGER.error(t, "Error processing SoundContext %s", toString());
        }
    }

    private void captureState() {
        if (this.sound != null) {
            this.pos = new Vector3d(this.sound.getX(), this.sound.getY(), this.sound.getZ());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(SoundUtils.debugString(this.sound))
                .toString();
    }

}

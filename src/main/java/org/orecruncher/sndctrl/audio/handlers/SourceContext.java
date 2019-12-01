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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;
import org.orecruncher.sndctrl.audio.handlers.effects.LowPassData;
import org.orecruncher.sndctrl.audio.handlers.effects.SourceProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.FloatBuffer;

@OnlyIn(Dist.CLIENT)
public final class SourceContext {

    private static final int UPDATE_FEQUENCY = 4;

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
    private final SourceProperty airAbsorb;

    @Nullable
    private ISound sound;
    @Nonnull
    private Vec3d pos;

    private boolean isNew = true;
    private boolean isDisabled;
    private int updateCount = 0;

    public SourceContext() {
        this.lowPass0 = new LowPassData();
        this.lowPass1 = new LowPassData();
        this.lowPass2 = new LowPassData();
        this.lowPass3 = new LowPassData();
        this.direct = new LowPassData();
        this.airAbsorb = new SourceProperty(EXTEfx.AL_AIR_ABSORPTION_FACTOR, 1F, 0F, 10F);
        this.pos = Vec3d.ZERO;
    }

    public boolean isDisabled() {
        return this.isDisabled;
    }

    public void disable() {
        this.isDisabled = true;
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
    public SourceProperty getAirAbsorb() {
        return this.airAbsorb;
    }

    @Nonnull
    public SoundCategory getCategory() {
        return this.sound != null ? this.sound.getCategory() : SoundCategory.AMBIENT;
    }

    @Nonnull
    public Vec3d getPosition() {
        return this.pos;
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
        // Upload the data
        Effects.filter0.apply(sourceId, this.lowPass0, 0, Effects.auxSlot0);
        Effects.filter1.apply(sourceId, this.lowPass1, 1, Effects.auxSlot1);
        Effects.filter2.apply(sourceId, this.lowPass2, 2, Effects.auxSlot2);
        Effects.filter3.apply(sourceId, this.lowPass3, 3, Effects.auxSlot3);
        Effects.direct.apply(sourceId, this.direct);

        this.airAbsorb.apply(sourceId);

        SoundFXProcessor.validate("SourceHandler::tick");
    }

    /**
     * Called during the client tick to perform the various calculations that need to be made to make the sound do
     * special things.  Do not call from the SoundSource processing thread or bad things will happen!
     */
    public void update() {
        if ((this.updateCount % UPDATE_FEQUENCY) == 0) {
            this.updateCount++;
            captureState();
            updateImpl();
        }
    }

    /**
     * Called from the sound source when a play is triggered.  We need to a do a series of calculations prior to
     * playing the sound to establish an initial set of filters.  This routine will be triggered *before* the event
     * handler in SoundFXProcessor gets invoked.
     *
     * @param sourceId Id of the source that is playing
     */
    public void updateFromSource(final int sourceId) {
        if (this.isNew) {
            this.isNew = false;
            final FloatBuffer pos = BufferUtils.createFloatBuffer(3);
            AL10.alGetSourcefv(sourceId, AL10.AL_POSITION, pos);
            SoundFXProcessor.validate();
            this.pos = new Vec3d(pos.get(0), pos.get(1), pos.get(2));
            updateImpl();
            tick(sourceId);
        }
    }

    private void updateImpl() {
        SoundFXUtils.calculate(SoundFXProcessor.getWorldContext(), this);
    }

    private void captureState() {
        if (this.sound != null) {
            this.pos = new Vec3d(this.sound.getX(), this.sound.getY(), this.sound.getZ());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(this.sound)
                .toString();
    }
}

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

package org.orecruncher.sndctrl.audio;

import com.google.common.base.MoreObjects;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class SoundInstance extends LocatableSound implements ISoundInstance {

    @Nonnull
    private SoundState state;
    @Nonnull
    private ISoundCategory category;

    private int playDelay;

    public SoundInstance(@Nonnull final SoundEvent event, @Nonnull final ISoundCategory cat) {
        this(event.getName(), cat);
    }

    public SoundInstance(@Nonnull final ResourceLocation soundResource, @Nonnull final ISoundCategory cat) {
        super(soundResource, cat.getRealCategory());

        this.state = SoundState.NONE;
        this.category = cat;
        this.volume = 1F;
        this.pitch = 1F;
        this.x = this.y = this.z = 0;
        this.repeat = false;
        this.repeatDelay = 0;
        this.attenuationType = ISound.AttenuationType.LINEAR;

        this.playDelay = 0;
        this.sound = SoundHandler.MISSING_SOUND;

        // Force creation of the sound instance now.  Need the info in the OGG definition for attenuation
        // distance prior to submission.
        this.createAccessor(GameUtils.getSoundHander());
    }

    @Override
    @Nonnull
    public ISoundCategory getSoundCategory() {
        return this.category;
    }

    @Override
    @Nonnull
    public SoundState getState() {
        return this.state;
    }

    @Override
    public void setState(@Nonnull final SoundState state) {
        this.state = state;
    }

    public void setPitch(final float p) {
        this.pitch = p;
    }

    public void setPosition(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setPosition(@Nonnull final Vector3i pos) {
        this.setPosition(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
    }

    public void setPosition(@Nonnull final Vector3d pos) {
        this.setPosition((float) pos.x, (float) pos.y, (float) pos.z);
    }

    public void setAttenuationType(@Nonnull final ISound.AttenuationType type) {
        this.attenuationType = type;
    }

    public void setRepeat(final boolean flag) {
        this.repeat = flag;
    }

    public void setRepeatDelay(final int delay) {
        this.repeatDelay = delay;
    }

    public void setVolume(final float v) {
        this.volume = v;
    }

    public void setGlobal(final boolean flag) {
        this.global = flag;
    }

    public boolean isDonePlaying() {
        return getState().isTerminal();
    }

    @Override
    public int getPlayDelay() {
        return this.playDelay;
    }

    public void setPlayDelay(final int delay) {
        this.playDelay = delay;
    }

    @Override
    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(getSoundLocation().toString())
                .addValue(getSoundCategory().toString())
                .addValue(getAttenuationType().toString())
                .addValue(getState().toString())
                .add("v", getVolume())
                .add("p", getPitch())
                .add("x", getX())
                .add("y", getY())
                .add("z", getZ())
                .toString();
    }

}

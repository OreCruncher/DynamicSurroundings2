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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class SoundInstance extends LocatableSound implements ISoundInstance {

    //private static final float ATTENUATION_OFFSET = 32F;

    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    @Nonnull
    private SoundState state = SoundState.NONE;

    private boolean canMute;

    SoundInstance(@Nonnull final SoundEvent event, @Nonnull final SoundCategory cat) {
        this(event.getName(), cat);
    }

    SoundInstance(@Nonnull final ResourceLocation soundResource, @Nonnull final SoundCategory cat) {
        super(soundResource, cat);

        this.volume = 1F;
        this.pitch = 1F;
        this.setPosition(0, 0, 0);
        this.repeat = false;
        this.repeatDelay = 0;
        this.attenuationType = ISound.AttenuationType.LINEAR;

        this.canMute = cat == SoundCategory.MUSIC;
        this.sound = SoundHandler.MISSING_SOUND;
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
        this.pos.setPos(x, y, z);
    }

    public void setPosition(@Nonnull final Vec3i pos) {
        this.setPosition(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
    }

    public void setPosition(@Nonnull final Vec3d pos) {
        this.setPosition((float) pos.x, (float) pos.y, (float) pos.z);
    }

    /*
    @Override
    public float getY() {
        // Non-attenuated sounds we play 32 blocks above the player.  Makes it sound right with headphones.
        final float y = super.getY();
        return getAttenuationType() == AttenuationType.NONE ? y + ATTENUATION_OFFSET : y;
    }

     */

    public void setAttenuationType(@Nonnull final ISound.AttenuationType type) {
        this.attenuationType = type;
    }

    public void setRepeat(final boolean flag) {
        this.repeat = flag;
    }

    public void setRepeatDelay(final int delay) {
        this.repeatDelay = delay;
    }

    @Override
    public boolean canMute() {
        return this.canMute;
    }

    public void setCanMute(final boolean f) {
        this.canMute = f;
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
    @Nonnull
    public String toString() {
        //@formatter:off
        return MoreObjects.toStringHelper(this)
                .addValue(getSoundLocation().toString())
                .addValue(getCategory().toString())
                .addValue(getAttenuationType().toString())
                .addValue(getState().toString())
                .add("v", getVolume())
                .add("p", getPitch())
                .add("x", getX())
                .add("y", getY())
                .add("z", getZ())
                .toString();
        //@formatter:on
    }

}

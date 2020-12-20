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

package org.orecruncher.sndctrl.audio.acoustic;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.api.acoustics.AcousticEvent;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.IAcousticFactory;

import javax.annotation.Nonnull;

/**
 * Simple acoustic that has no sound.
 */
@OnlyIn(Dist.CLIENT)
public final class NullAcoustic implements IAcoustic {

    public static final IAcoustic INSTANCE = new NullAcoustic(new ResourceLocation(SoundControl.MOD_ID, "null_acoustic"));

    private final ResourceLocation name;

    public NullAcoustic(@Nonnull final ResourceLocation name) {
        this.name = name;
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public void play(@Nonnull final AcousticEvent event) {

    }

    @Override
    public void playAt(@Nonnull final BlockPos pos, @Nonnull final AcousticEvent event) {

    }

    @Override
    public void playAt(@Nonnull final Vector3d pos, @Nonnull final AcousticEvent event) {

    }

    @Override
    public void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent event) {

    }

    @Override
    public void playBackground(@Nonnull final AcousticEvent event) {

    }

    @Override
    public IAcousticFactory getFactory(@Nonnull final AcousticEvent event) {
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}

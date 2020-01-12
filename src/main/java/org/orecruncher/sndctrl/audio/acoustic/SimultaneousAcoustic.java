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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.sndctrl.audio.ISoundInstance;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Plays a group of acoustics simultaneously creating a composite effect
 */
@OnlyIn(Dist.CLIENT)
public class SimultaneousAcoustic implements IAcoustic {

    @Nonnull
    private final ResourceLocation name;
    @Nonnull
    private final ObjectArray<IAcoustic> acoustics = new ObjectArray<>(4);

    public SimultaneousAcoustic(@Nonnull final ResourceLocation name) {
        this.name = Objects.requireNonNull(name);
    }

    public void add(@Nonnull final IAcoustic a) {
        // Ignore null acoustics
        if (!(a instanceof NullAcoustic))
            this.acoustics.add(a);
    }

    @Override
    @Nonnull
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public void play(@Nonnull final AcousticEvent event) {
        for (final IAcoustic a : this.acoustics)
            a.play(event);
    }

    @Override
    public void playAt(@Nonnull final BlockPos pos, @Nonnull final AcousticEvent event) {
        for (final IAcoustic a : this.acoustics)
            a.playAt(pos, event);
    }

    @Override
    public void playAt(@Nonnull final Vec3d pos, @Nonnull final AcousticEvent event) {
        for (final IAcoustic a : this.acoustics)
            a.playAt(pos, event);
    }

    @Override
    public void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent event) {
        for (final IAcoustic a : this.acoustics)
            a.playNear(entity, event);
    }

    @Override
    public void playBackground(@Nonnull final AcousticEvent event) {
        for (final IAcoustic a : this.acoustics)
            a.playBackground(event);
    }

    @Override
    public ISoundInstance getSound() {
        if (this.acoustics.size() > 0)
            return this.acoustics.get(0).getSound();
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(getName().toString()).add("entries", this.acoustics.size()).toString();
    }
}

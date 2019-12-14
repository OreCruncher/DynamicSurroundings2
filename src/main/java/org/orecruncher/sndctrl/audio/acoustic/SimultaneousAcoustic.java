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

import net.minecraft.entity.Entity;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.collections.ObjectArray;

import javax.annotation.Nonnull;

/**
 * Plays a group of acoustics simultaneously creating a composite effect
 */
@OnlyIn(Dist.CLIENT)
public class SimultaneousAcoustic implements IAcoustic {

    @Nonnull
    private final String name;
    @Nonnull
    private final ObjectArray<IAcoustic> acoustics = new ObjectArray<>(4);

    public SimultaneousAcoustic(@Nonnull final String name) {
        this.name = StringUtils.isNullOrEmpty(name) ? "<UNNAMED>" : name;
    }

    public void add(@Nonnull final IAcoustic a) {
        this.acoustics.add(a);
    }

    @Override
    @Nonnull
    public String getName() {
        return this.name;
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
}

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
import org.orecruncher.sndctrl.api.acoustics.AcousticEvent;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.IAcousticFactory;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An acoustic that will play different sounds based on the AcousticEvent provided.  For example, for a given
 * EventSelectorAcoustic, an acoustic could be selected if a mob is walking vs. running.
 */
@OnlyIn(Dist.CLIENT)
public class EventSelectorAcoustic implements IAcoustic {

    private final Map<AcousticEvent, IAcoustic> mapping = new IdentityHashMap<>(4);
    @Nonnull
    private final ResourceLocation name;

    public EventSelectorAcoustic(@Nonnull final ResourceLocation name) {
        this.name = Objects.requireNonNull(name);
    }

    public void add(@Nonnull final AcousticEvent event, @Nonnull final IAcoustic acoustic) {
        this.mapping.put(event, acoustic);
    }

    @Nonnull
    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public void play(@Nonnull final AcousticEvent event) {
        resolve(event).ifPresent(IAcoustic::play);
    }

    @Override
    public void playAt(@Nonnull final BlockPos pos, @Nonnull final AcousticEvent event) {
        resolve(event).ifPresent(a -> a.playAt(pos));
    }

    @Override
    public void playAt(@Nonnull final Vector3d pos, @Nonnull final AcousticEvent event) {
        resolve(event).ifPresent(a -> a.playAt(pos));
    }

    @Override
    public void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent event) {
        resolve(event).ifPresent(a -> a.playNear(entity));
    }

    @Override
    public void playBackground(@Nonnull final AcousticEvent event) {
        resolve(event).ifPresent(IAcoustic::playBackground);
    }

    @Override
    public IAcousticFactory getFactory(@Nonnull final AcousticEvent event) {
        return resolve(event).map(IAcoustic::getFactory).orElse(null);
    }

    @Nonnull
    protected Optional<IAcoustic> resolve(@Nonnull final AcousticEvent event) {
        IAcoustic acoustic = this.mapping.get(event);
        if (acoustic == null && event.canTransition())
            acoustic = this.mapping.get(event.getTransition());
        return Optional.ofNullable(acoustic);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(getName().toString()).add("entries", this.mapping.size()).toString();
    }

}

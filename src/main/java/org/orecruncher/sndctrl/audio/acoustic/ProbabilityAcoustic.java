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
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.sndctrl.api.acoustics.AcousticEvent;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.IAcousticFactory;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Plays a random acoustic from a weighted list of selections.
 */
@OnlyIn(Dist.CLIENT)
public class ProbabilityAcoustic implements IAcoustic {

    private static final Random RANDOM = XorShiftRandom.current();

    protected final ResourceLocation name;
    protected final ObjectArray<Pair> acoustics = new ObjectArray<>();
    protected int totalWeight;

    public ProbabilityAcoustic(@Nonnull final ResourceLocation name)
    {
        this.name = Objects.requireNonNull(name);
    }

    public void add(@Nonnull final IAcoustic acoustic, final int weight) {
        this.totalWeight += weight;
        this.acoustics.add(new Pair(acoustic, weight));
    }

    @Nonnull
    private Optional<IAcoustic> select() {
        if (this.totalWeight <= 0)
            return Optional.empty();

        int i = this.acoustics.size();
        if (i == 1)
            return Optional.of(this.acoustics.get(0).acoustic);

        int targetWeight = RANDOM.nextInt(this.totalWeight);

        for (i = this.acoustics.size(); (targetWeight -= this.acoustics.get(i - 1).weight) >= 0; i--);

        return Optional.of(this.acoustics.get(i - 1).acoustic);
    }

    @Override
    @Nonnull
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public void play(@Nonnull final AcousticEvent event) {
        select().ifPresent(a -> a.play(event));
    }

    @Override
    public void playAt(@Nonnull BlockPos pos, @Nonnull final AcousticEvent event) {
        select().ifPresent(a -> a.playAt(pos, event));
    }

    @Override
    public void playAt(@Nonnull Vec3d pos, @Nonnull final AcousticEvent event) {
        select().ifPresent(a -> a.playAt(pos, event));
    }

    @Override
    public void playNear(@Nonnull Entity entity, @Nonnull final AcousticEvent event) {
        select().ifPresent(a -> a.playNear(entity, event));
    }

    @Override
    public void playBackground(@Nonnull final AcousticEvent event) {
        select().ifPresent(a -> a.playBackground(event));
    }

    @Override
    public IAcousticFactory getFactory() {
        return select().map(IAcoustic::getFactory).orElse(null);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(getName().toString()).add("entries", this.acoustics.size()).toString();
    }

    private static class Pair {
        public final int weight;
        public final IAcoustic acoustic;

        public Pair(@Nonnull final IAcoustic a, final int weight) {
            this.acoustic = a;
            this.weight = weight;
        }
    }
}

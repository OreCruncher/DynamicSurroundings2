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

package org.orecruncher.lib.particles;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.math.TimerEMA;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

@OnlyIn(Dist.CLIENT)
final class ParticleCollectionHelper implements IParticleCollection {

    protected final String name;
    protected final ParticleCollection.ICollectionFactory factory;
    protected final IParticleRenderType renderType;

    // Weak reference because the particle could be evicted from Minecraft's
    // particle manager for some reason.
    protected WeakReference<ParticleCollection> collection;

    public ParticleCollectionHelper(@Nonnull final String name, @Nonnull final IParticleRenderType renderType) {
        this(name, ParticleCollection.FACTORY, renderType);
    }

    public ParticleCollectionHelper(@Nonnull final String name, @Nonnull final ParticleCollection.ICollectionFactory factory,
                                    @Nonnull final IParticleRenderType renderType) {
        this.name = name;
        this.renderType = renderType;
        this.factory = factory;
    }

    @Override
    @Nonnull
    public String name() {
        return this.name;
    }

    @Nonnull
    private ParticleCollection get() {
        ParticleCollection pc = this.collection != null ? this.collection.get() : null;
        if (pc == null || !pc.isAlive() || pc.shouldDie()) {
            pc = this.factory.create(this.name, GameUtils.getWorld(), this.renderType);
            this.collection = new WeakReference<>(pc);
            GameUtils.getMC().particles.addEffect(pc);
        }
        return pc;
    }

    @Override
    public void add(@Nonnull final IParticleMote mote) {
        final ParticleCollection pc = get();
        if (pc.canFit())
            pc.addParticle(mote);
    }

    @Override
    public boolean canFit() {
        return get().canFit();
    }

    void clear() {
        final ParticleCollection pc = this.collection != null ? this.collection.get() : null;
        if (pc != null) {
            pc.setExpired();
            this.collection = null;
        }
    }

    @Nullable
    TimerEMA getRenderTimer() {
        final ParticleCollection pc = this.collection != null ? this.collection.get() : null;
        return pc != null ? pc.getRenderTimer() : null;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.name).append('=');
        final ParticleCollection pc = this.collection != null ? this.collection.get() : null;
        if (pc == null)
            builder.append("No Collection");
        else if (!pc.isAlive())
            builder.append("Expired");
        else if (pc.shouldDie())
            builder.append("Should Die");
        else
            builder.append(pc.size());
        return builder.toString();
    }
}
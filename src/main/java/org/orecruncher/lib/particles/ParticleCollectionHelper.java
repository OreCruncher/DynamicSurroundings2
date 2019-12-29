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

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

@OnlyIn(Dist.CLIENT)
public class ParticleCollectionHelper {

    protected final String name;
    protected final ParticleCollection.ICollectionFactory factory;
    protected final ResourceLocation texture;

    // Weak reference because the particle could be evicted from Minecraft's
    // particle manager for some reason.
    protected WeakReference<ParticleCollection> collection;

    public ParticleCollectionHelper(@Nonnull final String name, @Nonnull final ResourceLocation texture) {
        this(name, ParticleCollection.FACTORY, texture);
    }

    public ParticleCollectionHelper(@Nonnull final String name, @Nonnull final ParticleCollection.ICollectionFactory factory,
                                    @Nonnull final ResourceLocation texture) {
        this.name = name;
        this.texture = texture;
        this.factory = factory;
    }

    @Nonnull
    public String name() {
        return this.name;
    }

    @Nonnull
    public ParticleCollection get() {
        ParticleCollection pc = this.collection != null ? this.collection.get() : null;
        if (pc == null || !pc.isAlive() || pc.shouldDie()) {
            pc = this.factory.create(GameUtils.getWorld(), this.texture);
            this.collection = new WeakReference<>(pc);
            GameUtils.getMC().particles.addEffect(pc);
        }
        return pc;
    }

    public void clear() {
        final ParticleCollection pc = this.collection != null ? this.collection.get() : null;
        if (pc != null) {
            pc.setExpired();
            this.collection = null;
        }
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
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
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.math.LoggingTimerEMA;
import org.orecruncher.lib.math.TimerEMA;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
final class ParticleCollection extends BaseParticle {

    public static final ICollectionFactory FACTORY = ParticleCollection::new;
    protected static final int MAX_PARTICLES = 4000;
    protected static final int ALLOCATION_SIZE = 128;
    protected static final int TICK_GRACE = 2;
    /**
     * Predicate used to update a mote and return whether it is dead or not.
     */
    private static final Predicate<IParticleMote> UPDATE_REMOVE = mote -> !mote.tick();

    protected final LoggingTimerEMA render;
    protected final LoggingTimerEMA tick;
    protected final ObjectArray<IParticleMote> myParticles = new ObjectArray<>(ALLOCATION_SIZE);
    protected final IParticleRenderType renderType;
    protected long lastTickUpdate;

    ParticleCollection(@Nonnull final String name, @Nonnull final World world, @Nonnull final IParticleRenderType renderType) {
        super(world, 0, 0, 0);

        this.canCollide = false;
        this.renderType = renderType;
        this.render = new LoggingTimerEMA("Render " + name);
        this.tick = new LoggingTimerEMA("Tick " + name);
        this.lastTickUpdate = TickCounter.getTickCount();
    }

    public boolean canFit() {
        return this.myParticles.size() < MAX_PARTICLES;
    }

    public void addParticle(@Nonnull final IParticleMote mote) {
        if (canFit()) {
            this.myParticles.add(mote);
        }
    }

    public int size() {
        return this.myParticles.size();
    }

    @Nonnull
    public TimerEMA getRenderTimer() {
        return this.render;
    }

    @Nonnull
    public TimerEMA getTickTimer() {
        return this.tick;
    }

    public boolean shouldDie() {
        final boolean timeout = (TickCounter.getTickCount() - this.lastTickUpdate) > TICK_GRACE;
        return timeout || size() == 0 || this.world != GameUtils.getWorld();
    }

    @Override
    public void tick() {
        this.tick.begin();
        if (isAlive()) {
            this.lastTickUpdate = TickCounter.getTickCount();
            this.myParticles.removeIf(UPDATE_REMOVE);
            if (shouldDie()) {
                setExpired();
            }
        }
        this.tick.end();
    }

    @Override
    public void renderParticle(@Nonnull final BufferBuilder buffer, @Nonnull final ActiveRenderInfo info, final float partialTicks,
                               final float rotX, final float rotZ, final float rotYZ, final float rotXY, final float rotXZ) {
        this.render.begin();
        for (final IParticleMote mote : this.myParticles)
            mote.render(buffer, info, partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ);
        this.render.end();
    }

    @Override
    @Nonnull
    public IParticleRenderType getRenderType() {
        return this.renderType;
    }

    /**
     * Factory interface for creating particle collection instances. Used by the
     * ParticleCollections manager.
     */
    public interface ICollectionFactory {
        ParticleCollection create(@Nonnull final String name, @Nonnull final World world, @Nonnull final IParticleRenderType renderType);
    }

}
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

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.opengl.OpenGlState;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class ParticleCollection  extends BaseParticle {

    /**
     * Predicate used to update a mote and return whether it is dead or not.
     */
    private static final Predicate<IParticleMote> UPDATE_REMOVE = mote -> {
        mote.tick();
        return !mote.isAlive();
    };

    protected static final int MAX_PARTICLES = 4000;
    protected static final int ALLOCATION_SIZE = 128;
    protected static final int TICK_GRACE = 2;

    protected final ObjectArray<IParticleMote> myParticles = new ObjectArray<>(ALLOCATION_SIZE);
    protected final ResourceLocation texture;

    protected long lastTickUpdate;
    protected OpenGlState glState;

    public ParticleCollection(@Nonnull final World world, @Nonnull final ResourceLocation tex) {
        super(world, 0, 0, 0);

        this.canCollide = false;
        this.texture = tex;
        this.lastTickUpdate = TickCounter.getTickCount();
    }

    public boolean canFit() {
        return this.myParticles.size() < MAX_PARTICLES;
    }

    public boolean addParticle(@Nonnull final IParticleMote mote) {
        if (canFit()) {
            this.myParticles.add(mote);
            return true;
        }
        return false;
    }

    public ObjectArray<IParticleMote> getParticles() {
        return this.myParticles;
    }

    public int size() {
        return this.myParticles.size();
    }

    public boolean shouldDie() {
        final boolean timeout = (TickCounter.getTickCount() - this.lastTickUpdate) > TICK_GRACE;
        return timeout || size() == 0 || this.world != GameUtils.getWorld();
    }

    @Override
    public void tick() {
        if (!isAlive())
            return;

        this.lastTickUpdate =TickCounter.getTickCount();

        // Update state and remove the dead ones
        this.myParticles.removeIf(UPDATE_REMOVE);

        if (shouldDie()) {
            setExpired();
        }
    }

    @Nonnull
    protected VertexFormat getVertexFormat() {
        return DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP;
    }

    @Override
    public void renderParticle(@Nonnull final BufferBuilder buffer, @Nonnull final ActiveRenderInfo info, final float partialTicks,
                               final float rotX, final float rotZ, final float rotYZ, final float rotXY, final float rotXZ) {

        if (this.myParticles.size() == 0)
            return;

        bindTexture(this.texture);
        preRender();

        buffer.begin(GL11.GL_QUADS, getVertexFormat());
        for (int i = 0; i < this.myParticles.size(); i++)
            this.myParticles.get(i).render(buffer, info, partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ);
        Tessellator.getInstance().draw();

        postRender();
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.CUSTOM;
    }

    protected boolean enableLighting() {
        return false;
    }

    protected void preRender() {
        this.glState = OpenGlState.push();
        if (enableLighting())
            GlStateManager.enableLighting();
        else
            GlStateManager.disableLighting();
    }

    protected void postRender() {
        OpenGlState.pop(this.glState);
        this.glState = null;
    }

    /**
     * Factory interface for creating particle collection instances. Used by the
     * ParticleCollections manager.
     */
    public interface ICollectionFactory {
        ParticleCollection create(@Nonnull final World world, @Nonnull final ResourceLocation texture);
    }

    public static final ICollectionFactory FACTORY = ParticleCollection::new;

}
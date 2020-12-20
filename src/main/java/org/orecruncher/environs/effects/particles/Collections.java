/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2019  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.effects.particles;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.orecruncher.environs.Environs;
import org.orecruncher.lib.opengl.OpenGlUtil;
import org.orecruncher.lib.particles.*;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Environs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Collections {

    private static final IParticleRenderType RIPPLE_RENDER =
            new ParticleRenderType(new ResourceLocation(Environs.MOD_ID,"textures/particles/ripple.png")) {
                @Override
                public void beginRender(@Nonnull BufferBuilder buffer, @Nonnull TextureManager textureManager) {
                    RenderHelper.disableStandardItemLighting();
                    textureManager.bindTexture(RippleStyle.get().getTexture());
                    GlStateManager.depthMask(false);
                    GlStateManager.enableDepthTest();
                    OpenGlUtil.setStandardBlend();
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
                    buffer.begin(GL11.GL_QUADS, this.getVertexFormat());
                }
            };

    private static final IParticleRenderType SPRAY_RENDER = new ParticleRenderType(new ResourceLocation(Environs.MOD_ID,"textures/particles/rainsplash.png"));
    private static final IParticleRenderType FIREFLY_RENDER = IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;

    private final static IParticleCollection theRipples = CollectionManager.create("Rain Ripples", RIPPLE_RENDER);
    private final static IParticleCollection theSprays = CollectionManager.create("Water Spray", SPRAY_RENDER);
    private final static IParticleCollection theFireFlies = CollectionManager.create("Fireflies", FIREFLY_RENDER);

    private Collections() {

    }

    public static void addWaterRipple(@Nonnull final IBlockReader world, final double x, final double y,
                                      final double z) {
        if (theRipples.canFit()) {
            final IParticleMote mote = new MoteWaterRipple(world, x, y, z);
            theRipples.add(mote);
        }
    }

    public static boolean addWaterSpray(@Nonnull final IBlockReader world, final double x, final double y,
                                              final double z, final double dX, final double dY, final double dZ) {
        if (theSprays.canFit()) {
            final IParticleMote mote = new MoteWaterSpray(world, x, y, z, dX, dY, dZ);
            theSprays.add(mote);
            return true;
        }
        return false;
    }

    public static boolean canFitWaterSpray() {
        return theSprays.canFit();
    }

    public static void addRainSplash(@Nonnull final IBlockReader world, final double x, final double y,
                                              final double z) {
        if (theSprays.canFit()) {
            final IParticleMote mote = new MoteRainSplash(world, x, y, z);
            theSprays.add(mote);
        }
    }

    public static void addFireFly(@Nonnull final IBlockReader world, final double x, final double y, final double z) {
        if (theFireFlies.canFit()) {
            final IParticleMote mote = new MoteFireFly(world, x, y, z);
            theFireFlies.add(mote);
        }
    }

}

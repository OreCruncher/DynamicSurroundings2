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

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.gui.ColorPalette;
import org.orecruncher.lib.particles.AnimatedMote;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class MoteFireFly extends AnimatedMote {

    private static final float XZ_MOTION_DELTA = 0.2F;
    private static final float Y_MOTION_DELTA = XZ_MOTION_DELTA / 2.0F;
    private static final float ACCELERATION = 0.004F;

    private boolean doRender;

    public MoteFireFly(@Nonnull final IBlockReader world, final double x, final double y, final double z) {
        super(GameUtils.getMC().particles.sprites.get(ParticleTypes.FIREWORK.getRegistryName()), world, x, y, z, 0, 0, 0);

        this.motionX = RANDOM.nextGaussian() * XZ_MOTION_DELTA;
        this.motionZ = RANDOM.nextGaussian() * XZ_MOTION_DELTA;
        this.motionY = RANDOM.nextGaussian() * Y_MOTION_DELTA;

        this.xAcceleration = RANDOM.nextGaussian() * ACCELERATION;
        this.yAcceleration = RANDOM.nextGaussian() / 2.0D * ACCELERATION;
        this.zAcceleration = RANDOM.nextGaussian() * ACCELERATION;

        this.gravity = 0D;

        this.particleScale *= 0.75F * 0.25F * 0.1F;
        this.maxAge = 120 + RANDOM.nextInt(12);

        setColor(ColorPalette.MC_YELLOW);
        setColorFade(ColorPalette.MC_GREEN);
    }

    @Override
    public void update() {
        super.update();

        this.doRender = this.age < this.maxAge / 3 || (this.age + this.maxAge) / 3 % 2 == 0;
    }

    @Override
    public void renderParticle(@Nonnull IVertexBuilder buffer, @Nonnull ActiveRenderInfo info, float partialTicks) {
        if (this.doRender)
            super.renderParticle(buffer, info, partialTicks);
    }

}

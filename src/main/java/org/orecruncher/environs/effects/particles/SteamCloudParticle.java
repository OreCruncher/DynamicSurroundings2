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

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.random.XorShiftRandom;

import javax.annotation.Nonnull;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class SteamCloudParticle extends SpriteTexturedParticle {

    private static final Random RANDOM = XorShiftRandom.current();

    private final IAnimatedSprite field_217583_C;

    public SteamCloudParticle(World world, double x, double y, double z, double dY) {
        super(world, x, y, z, RANDOM.nextGaussian() * 0.02D, dY,
                RANDOM.nextGaussian() * 0.02D);

        this.field_217583_C = GameUtils.getMC().particles.sprites.get(ParticleTypes.CLOUD.getRegistryName());
        this.motionX *= 0.1F;
        this.motionY *= 0.1F;
        this.motionZ *= 0.1F;
        //this.motionX += motionX;
        this.motionY += dY;
        //this.motionZ += motionZ;
        float f1 = 1.0F - (float) (Math.random() * (double) 0.3F);
        this.particleRed = f1;
        this.particleGreen = f1;
        this.particleBlue = f1;
        this.particleScale *= 1.875F;
        int i = (int) (8.0D / (Math.random() * 0.8D + 0.3D));
        this.maxAge = (int) Math.max((float) i * 2.5F, 1.0F);
        this.canCollide = false;
        this.selectSpriteWithAge(this.field_217583_C);
    }

    @Nonnull
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public float getScale(float p_217561_1_) {
        return this.particleScale * MathHelper.clamp(((float) this.age + p_217561_1_) / (float) this.maxAge * 32.0F, 0.0F, 1.0F);
    }

    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ >= this.maxAge) {
            this.setExpired();
        } else {
            this.selectSpriteWithAge(this.field_217583_C);
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.96F;
            this.motionY *= 0.96F;
            this.motionZ *= 0.96F;

            if (this.onGround) {
                this.motionX *= 0.7F;
                this.motionZ *= 0.7F;
            }

        }
    }
}
/*
 *  Dynamic Surroundings: Mob Effects
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

package org.orecruncher.mobeffects.effects.particles;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class BubbleBreathParticle  extends SpriteTexturedParticle {
    public BubbleBreathParticle(@Nonnull final LivingEntity entity, final boolean isDrowning) {
        super(entity.getEntityWorld(), 0, 0, 0);

        // Reuse the bubble sheet
        final IAnimatedSprite spriteSet = GameUtils.getMC().particles.sprites.get(ParticleTypes.BUBBLE.getRegistryName());
        this.selectSpriteRandomly(spriteSet);

        final Vec3d origin = ParticleUtils.getBreathOrigin(entity);
        final Vec3d trajectory = ParticleUtils.getLookTrajectory(entity);
        final double factor = isDrowning ? 0.02D : 0.005D;

        this.setPosition(origin.x, origin.y, origin.z);
        this.prevPosX = origin.x;
        this.prevPosY = origin.y;
        this.prevPosZ = origin.z;

        this.motionX = trajectory.x * factor;
        this.motionY = trajectory.y * 0.002D;
        this.motionZ = trajectory.z * factor;

        this.particleGravity = 0F;

        this.setAlphaF(0.2F);
        this.setSize(0.02F, 0.02F);
        this.particleScale *= this.rand.nextFloat() * 0.6F + 0.2F;
        this.particleScale *= entity.isChild() ? 0.125F : 0.25F;
        this.maxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
    }

    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.maxAge-- <= 0) {
            this.setExpired();
        } else {
            this.motionY += 0.002D;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.8500000238418579D;
            this.motionY *= 0.8500000238418579D;
            this.motionZ *= 0.8500000238418579D;
            if (!this.world.getFluidState(new BlockPos(this.posX, this.posY, this.posZ)).isTagged(FluidTags.WATER)) {
                this.setExpired();
            }
        }
    }

    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}

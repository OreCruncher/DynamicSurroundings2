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

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.WorldUtils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@OnlyIn(Dist.CLIENT)
public class DustParticle extends DiggingParticle {

	private final BlockPos.Mutable pos = new BlockPos.Mutable();

	public DustParticle(final World world, final double x, final double y, final double z, final BlockState state) {
		this(world, x, y, z, 0, 0, 0, state);
	}

	public DustParticle(final World world, final double x, final double y, final double z, final double dX, final double dY, final double dZ, final BlockState state) {
		super(world, x, y, z, 0, 0, 0, state);

		this.canCollide = false;
		this.motionX = dX;
		this.motionY = dY;
		this.motionZ = dZ;

		multiplyParticleScaleBy((float) (0.3F + this.rand.nextGaussian() / 30.0F));
		setPosition(this.posX, this.posY, this.posZ);
	}

	@Override
	public void move(final double dX, final double dY, final double dZ) {
		this.posX += dX;
		this.posY += dY;
		this.posZ += dZ;
	}

	@Override
	public void tick() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= 0.04D * this.particleGravity;
		move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		this.pos.setPos(this.posX, this.posY, this.posZ);

		if (this.maxAge-- <= 0) {
			setExpired();
		} else if (WorldUtils.isBlockSolid(this.world, this.pos)) {
			setExpired();
		}
	}
}
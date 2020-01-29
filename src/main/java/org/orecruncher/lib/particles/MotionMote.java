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

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

/**
 * A particle that is capable of moving it's position in the world.
 */
@OnlyIn(Dist.CLIENT)
public abstract class MotionMote extends AgeableMote {

	protected double motionX;
	protected double motionY;
	protected double motionZ;
	protected double gravity;

	protected double prevX;
	protected double prevY;
	protected double prevZ;

	protected MotionMote(@Nonnull final IWorldReader world, final double x, final double y, final double z,
						 final double dX, final double dY, final double dZ) {
		super(world, x, y, z);
		this.prevX = this.posX;
		this.prevY = this.posY;
		this.prevZ = this.posZ;
		this.motionX = dX;
		this.motionY = dY;
		this.motionZ = dZ;
		this.gravity = 0.06D;
	}

	@Override
	protected float renderX(final float partialTicks) {
		return (float) (MathHelper.lerp(partialTicks, this.prevX, this.posX) - interpX());
	}

	@Override
	protected float renderY(final float partialTicks) {
		return (float) (MathHelper.lerp(partialTicks, this.prevY, this.posY) - interpY());
	}

	@Override
	protected float renderZ(final float partialTicks) {
		return (float) (MathHelper.lerp(partialTicks, this.prevZ, this.posZ) - interpZ());
	}

	protected Pair<Vec3d, Boolean> detectCollision() {
		final BlockState state = this.world.getBlockState(this.position);
		if (state.getMaterial() == Material.AIR)
			return null;
		if (state.isSolid()) {
			final VoxelShape shape = state.getCollisionShape(this.world, this.position, ISelectionContext.dummy());
			if (!shape.isEmpty()) {
				final double y = shape.getBoundingBox().maxY;
				if (y >= this.posY) {
					// Have a collision
					return Pair.of(new Vec3d(this.posX, y, this.posZ), true);
				}
			}
			// Hasn't collided yet
			return null;
		}

		final IFluidState fluid = state.getFluidState();
		if (!fluid.isEmpty() && fluid.isSource()) {
			// Potential of collision with a liquid
			final double height = fluid.getHeight() + this.position.getY();
			if (height >= this.posY) {
				// Hit the surface of water
				return Pair.of(new Vec3d(this.posX, height, this.posZ), false);
			}
		}

		return null;
	}

	protected void handleCollision(@Nonnull final Pair<Vec3d, Boolean> collision) {
		kill();
	}

	@Override
	protected void update() {

		this.prevX = this.posX;
		this.prevY = this.posY;
		this.prevZ = this.posZ;
		this.motionY -= this.gravity;

		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;

		this.position.setPos(this.posX, this.posY, this.posZ);

		final Pair<Vec3d, Boolean> collision = detectCollision();
		if (collision != null) {
			handleCollision(collision);
		} else {
			this.motionX *= 0.9800000190734863D;
			this.motionY *= 0.9800000190734863D;
			this.motionZ *= 0.9800000190734863D;
		}
	}

}

/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

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

	protected MotionMote(@Nonnull final IBlockReader world, final double x, final double y, final double z,
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
	protected float renderX(ActiveRenderInfo info, final float partialTicks) {
		return (float)(MathHelper.lerp(partialTicks, this.prevX, this.posX) - info.getProjectedView().getX());
	}

	@Override
	protected float renderY(ActiveRenderInfo info, final float partialTicks) {
		return (float)(MathHelper.lerp(partialTicks, this.prevY, this.posY) - info.getProjectedView().getY());
	}

	@Override
	protected float renderZ(ActiveRenderInfo info, final float partialTicks) {
		return (float)(MathHelper.lerp(partialTicks, this.prevZ, this.posZ) - info.getProjectedView().getZ());
	}

	/**
	 * Detects when a particle collides with a non-air block.  Override to provide custom detection logic.
	 *
	 * @return Instance containing collision information if the mote collided
	 */
	@Nonnull
	protected Optional<ParticleCollisionResult> detectCollision() {
		final BlockState state = this.world.getBlockState(this.position);

		// Air does not collide
		if (state.getMaterial() == Material.AIR)
			return Optional.empty();

		// If the current position blocks movement then it will block a particle
		if (state.getMaterial().blocksMovement()) {
			final VoxelShape shape = state.getCollisionShape(this.world, this.position, ISelectionContext.dummy());
			if (!shape.isEmpty()) {
				final double height = shape.getEnd(Direction.Axis.Y) + this.position.getY();
				if (height >= this.posY) {
					// Have a collision
					return Optional.of(new ParticleCollisionResult(
							this.world,
							new Vector3d(this.posX, height, this.posZ),
							state,
							true,
							null
					));
				}
			}
			// Hasn't collided yet
			return Optional.empty();
		}

		// Check fluid state because the particle could have landed in fluid
		final FluidState fluid = state.getFluidState();
		if (!fluid.isEmpty()) {
			// Potential of collision with a liquid
			final double height = fluid.getHeight() + this.position.getY();
			if (height >= this.posY) {
				// Hit the surface of liquid
				return Optional.of(new ParticleCollisionResult(
						this.world,
						new Vector3d(this.posX, height, this.posZ),
						state,
						false,
						fluid
				));
			}
		}

		return Optional.empty();
	}

	/**
	 * Handles what happens when a collision is detected.  Default implemetnation will kill the mote.
	 *
	 * @param collision Instance containing the collision information.
	 */
	protected void handleCollision(@Nonnull final ParticleCollisionResult collision) {
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

		final Optional<ParticleCollisionResult> result = detectCollision();
		if (result.isPresent()) {
			handleCollision(result.get());
		} else {
			this.motionX *= 0.9800000190734863D;
			this.motionY *= 0.9800000190734863D;
			this.motionZ *= 0.9800000190734863D;
		}
	}

}

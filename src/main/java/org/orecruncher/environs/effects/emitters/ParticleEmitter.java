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


package org.orecruncher.environs.effects.emitters;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.BlockPos;

@OnlyIn(Dist.CLIENT)
public abstract class ParticleEmitter {

	protected static final Random RANDOM = XorShiftRandom.current();

	protected final IBlockReader world;
	protected final double posX;
	protected final double posY;
	protected final double posZ;
	protected final BlockPos position;
	private boolean isAlive = true;

	protected ParticleEmitter(final IBlockReader worldIn, final double posXIn, final double posYIn, final double posZIn) {
		this.world = worldIn;
		this.posX = posXIn;
		this.posY = posYIn;
		this.posZ = posZIn;
		this.position = new BlockPos(posXIn, posYIn, posZIn);
	}

	@Nonnull
	public BlockPos getPos() {
		return this.position;
	}

	/*
	 * Adds a particle to the internal tracking list as well as adds it to the
	 * Minecraft particle manager.
	 */
	public void addParticle(@Nonnull final Particle particle) {
		GameUtils.getMC().particles.addEffect(particle);
	}

	public boolean isAlive() {
		return this.isAlive;
	}

	public void setExpired() {
		this.isAlive = false;
		cleanUp();
	}

	/*
	 * By default a system will stay alive indefinitely until the
	 * ParticleSystemManager kills it. Override to provide termination capability.
	 */
	public boolean shouldDie() {
		return false;
	}

	/*
	 * Perform any cleanup activities prior to dying.
	 */
	protected void cleanUp() {

	}

	/*
	 * Update the state of the particle system. Any particles are queued into the
	 * Minecraft particle system or to a ParticleCollection so they do not have to
	 * be ticked.
	 */
	public void tick() {
		if (shouldDie()) {
			setExpired();
			return;
		}

		// Let the system mull over what it wants to do
		think();

		if (isAlive())
			// Update any sounds
			soundUpdate();
	}

	/*
	 * Override to provide sound for the particle effect. Will be invoked whenever
	 * the particle system is updated by the particle manager.
	 */
	protected void soundUpdate() {

	}

	/*
	 * Override to provide some sort of intelligence to the system. The logic can do
	 * things like add new particles, remove old ones, update positions, etc. Will
	 * be invoked during the systems onUpdate() call.
	 */
	public abstract void think();

}

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

import net.minecraft.block.BlockState;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import org.orecruncher.environs.effects.particles.DustParticle;

@OnlyIn(Dist.CLIENT)
public class FountainJet extends Jet {

	protected final BlockState state;

	public FountainJet(final int strength, final IBlockReader world, final double x, final double y, final double z,
					   final BlockState state) {
		super(1, strength, world, x, y, z, 1);
		this.state = state;
	}

	@Override
	protected void spawnJetParticle() {
		final double motionX = RANDOM.nextGaussian() * 0.03D;
		final double motionZ = RANDOM.nextGaussian() * 0.03D;
		final double x = this.posX + RANDOM.nextGaussian() * 0.2D;
		final double z = this.posZ + RANDOM.nextGaussian() * 0.2D;
		final Particle particle = new DustParticle((World) this.world, x, this.posY, z, motionX, 0.5D, motionZ, this.state)
				.init();
		addParticle(particle);
	}

}

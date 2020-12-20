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

import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/*
 * Base for particle entities that are long lived and generate
 * other particles as a jet.  This entity does not render - just
 * serves as a particle factory.
 */
@OnlyIn(Dist.CLIENT)
public abstract class Jet extends ParticleEmitter {

	protected final int jetStrength;
	protected final int updateFrequency;

	protected final int particleMaxAge;
	protected int particleAge;

	public Jet(final int strength, final IBlockReader world, final double x, final double y, final double z) {
		this(0, strength, world, x, y, z, 3);
	}

	public Jet(final int layer, final int strength, final IBlockReader world, final double x, final double y,
			   final double z, final int freq) {
		super(world, x, y, z);

		this.jetStrength = strength;
		this.updateFrequency = freq;
		this.particleMaxAge = (RANDOM.nextInt(strength) + 2) * 20;
	}

	/*
	 * Override in derived class to provide particle for the jet.
	 */
	protected abstract void spawnJetParticle();

	@Override
	public boolean shouldDie() {
		return this.particleAge >= this.particleMaxAge;
	}

	/*
	 * During update see if a particle needs to be spawned so that it can rise up.
	 */
	@Override
	public void think() {

		// Check to see if a particle needs to be generated
		if (this.particleAge % this.updateFrequency == 0) {
			spawnJetParticle();
		}

		// Grow older
		this.particleAge++;
	}
}

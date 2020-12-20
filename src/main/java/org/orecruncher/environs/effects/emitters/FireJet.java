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

import net.minecraft.client.particle.FlameParticle;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.client.particle.Particle;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.sndctrl.api.acoustics.Library;

@OnlyIn(Dist.CLIENT)
public class FireJet extends Jet {

	private static final ResourceLocation FIRE_ACOUSTIC = new ResourceLocation("block.fire.ambient");
	protected final boolean isLava;
	protected final IParticleData particleType;
	protected final boolean isSolid;
	protected boolean soundFired;

	public FireJet(final int strength, final IBlockReader world, final double x, final double y, final double z, boolean isSolid) {
		super(strength, world, x, y, z);
		this.isLava = !isSolid && RANDOM.nextInt(3) == 0;
		this.particleType = this.isLava ? ParticleTypes.LAVA : ParticleTypes.FLAME;
		this.isSolid = isSolid;
	}

	@Override
	protected void soundUpdate() {
		if (!this.soundFired) {
			this.soundFired = true;
			if (this.jetStrength > 1) {
				Library.resolve(FIRE_ACOUSTIC).playAt(getPos());
			}
		}
	}

	@Override
	protected void spawnJetParticle() {
		double speedY = this.isLava ? 0 : this.jetStrength / 10.0D;
		float scale = this.jetStrength;
		double x = this.posX;
		double z = this.posZ;

		if (this.isSolid) {
			x += (RANDOM.nextDouble() - RANDOM.nextDouble()) * 0.5D;
			z += (RANDOM.nextDouble() - RANDOM.nextDouble()) * 0.5D;
			if (this.jetStrength == 1) {
				speedY *= 0.5D;
				scale *= 0.5F;
			}
		}

		final Particle particle = GameUtils.getMC().particles.addParticle(this.particleType, x, this.posY, z, 0, speedY, 0D);

		if (particle instanceof FlameParticle) {
			particle.multiplyParticleScaleBy(scale);
		}
	}
}

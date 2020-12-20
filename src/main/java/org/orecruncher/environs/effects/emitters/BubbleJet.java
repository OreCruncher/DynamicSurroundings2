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

import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;

@OnlyIn(Dist.CLIENT)
public class BubbleJet extends Jet {

	public BubbleJet(final int strength, final IBlockReader world, final double x, final double y, final double z) {
		super(strength, world, x, y, z);
	}

	@Override
	protected void spawnJetParticle() {
		GameUtils.getMC().particles.addParticle(ParticleTypes.BUBBLE, this.posX, this.posY, this.posZ, 0, 0.5D + this.jetStrength / 10D, 0D);
	}

}

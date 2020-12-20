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

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MoteRainSplash extends MoteWaterSpray {

	public MoteRainSplash(final IBlockReader world, final double x, final double y, final double z) {
		super(world, x, y, z, 0, 0, 0);

		// Setup motion
		this.motionX = (RANDOM.nextDouble() * 2.0D - 1.0D) * 0.4000000059604645D;
		this.motionY = (RANDOM.nextDouble() * 2.0D - 1.0D) * 0.4000000059604645D;
		this.motionZ = (RANDOM.nextDouble() * 2.0D - 1.0D) * 0.4000000059604645D;
		final float f = (float) (RANDOM.nextDouble() + RANDOM.nextDouble() + 1.0D) * 0.15F;
		final float f1 = MathHelper
				.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
		this.motionX = this.motionX / f1 * f * 0.4000000059604645D;
		this.motionY = this.motionY / f1 * f * 0.4000000059604645D + 0.10000000149011612D;
		this.motionZ = this.motionZ / f1 * f * 0.4000000059604645D;

		this.motionX *= 0.30000001192092896D;
		this.motionY = RANDOM.nextDouble() * 0.20000000298023224D + 0.10000000149011612D;
		this.motionZ *= 0.30000001192092896D;
	}

	@Override
	public void configureColor() {
		this.red = this.green = this.blue = 1F;
		this.alpha = 0.99F;
	}

}

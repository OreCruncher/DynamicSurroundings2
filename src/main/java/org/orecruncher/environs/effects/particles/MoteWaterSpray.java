/*
 *  Dynamic Surroundings
 *  Copyright (C) 2020  OreCruncher
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

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.orecruncher.lib.biomes.BiomeUtilities;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.particles.MotionMote;
import org.orecruncher.lib.particles.ParticleCollisionResult;
import org.orecruncher.lib.random.XorShiftRandom;

import javax.annotation.Nonnull;
import java.util.Random;

// TODO: Should this be animated?  Seems to overlap.
@OnlyIn(Dist.CLIENT)
public class MoteWaterSpray extends MotionMote {

	protected static final Random RANDOM = XorShiftRandom.current();

	protected float scale;

	protected final float texU1, texU2;
	protected final float texV1, texV2;

	public MoteWaterSpray(final IBlockReader world, final double x, final double y, final double z, final double dX,
						  final double dY, final double dZ) {

		super(world, x, y, z, dX, dY, dZ);

		this.maxAge = (int) (8.0F / (RANDOM.nextFloat() * 0.8F + 0.2F));
		this.scale = (RANDOM.nextFloat() * 0.5F + 0.5F) * 2.0F * 0.07F;

		final int textureIdx = RANDOM.nextInt(4);
		final int texX = textureIdx % 2;
		final int texY = textureIdx / 2;
		this.texU1 = texX * 0.5F;
		this.texU2 = this.texU1 + 0.5F;
		this.texV1 = texY * 0.5F;
		this.texV2 = this.texV1 + 0.5F;
	}

	@Override
	public void configureColor() {
		final Color waterColor = BiomeUtilities.getColorForLiquid(this.world, this.position);
		this.red = waterColor.red();
		this.green = waterColor.green();
		this.blue = waterColor.blue();
		this.alpha = 0.99F;
	}

	@Override
	public void handleCollision(@Nonnull final ParticleCollisionResult collision) {
		// Do the drip splash, but don't play the sound.  Sounds funny with waterfall effects
		ParticleHooks.splashHandler(Fluids.WATER, collision, false);
		super.handleCollision(collision);
	}

	@Override
	public void renderParticle(@Nonnull IVertexBuilder buffer, @Nonnull ActiveRenderInfo info, float partialTicks) {

		final float x = renderX(info, partialTicks);
		final float y = renderY(info, partialTicks);
		final float z = renderZ(info, partialTicks);

		Quaternion quaternion = info.getRotation();

		Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
		vector3f1.transform(quaternion);
		Vector3f[] avector3f = new Vector3f[]{
				new Vector3f(-1.0F, -1.0F, 0.0F),
				new Vector3f(-1.0F, 1.0F, 0.0F),
				new Vector3f(1.0F, 1.0F, 0.0F),
				new Vector3f(1.0F, -1.0F, 0.0F)};

		for (int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f[i];
			vector3f.transform(quaternion);
			vector3f.mul(this.scale);
			vector3f.add(x, y, z);
		}

		drawVertex(buffer, avector3f[0].getX(), avector3f[0].getY(), avector3f[0].getZ(), this.texU2, this.texV2);
		drawVertex(buffer, avector3f[1].getX(), avector3f[1].getY(), avector3f[1].getZ(), this.texU2, this.texV1);
		drawVertex(buffer, avector3f[2].getX(), avector3f[2].getY(), avector3f[2].getZ(), this.texU1, this.texV1);
		drawVertex(buffer, avector3f[3].getX(), avector3f[3].getY(), avector3f[3].getZ(), this.texU1, this.texV2);
	}

}

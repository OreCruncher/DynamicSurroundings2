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

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.client.renderer.BufferBuilder;
import org.orecruncher.environs.library.BiomeUtil;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.particles.MotionMote;
import org.orecruncher.lib.particles.ParticleCollisionResult;
import org.orecruncher.lib.random.XorShiftRandom;

import javax.annotation.Nonnull;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class MoteWaterSpray extends MotionMote {

	protected static final Random RANDOM = XorShiftRandom.current();

	protected float scale;

	protected final float texU1, texU2;
	protected final float texV1, texV2;
	protected final float f4;

	public MoteWaterSpray(final IBlockReader world, final double x, final double y, final double z, final double dX,
						  final double dY, final double dZ) {

		super(world, x, y, z, dX, dY, dZ);

		this.maxAge = (int) (8.0F / (RANDOM.nextFloat() * 0.8F + 0.2F));
		this.scale = (RANDOM.nextFloat() * 0.5F + 0.5F) * 2.0F;

		final int textureIdx = RANDOM.nextInt(4);
		final int texX = textureIdx % 2;
		final int texY = textureIdx / 2;
		this.texU1 = texX * 0.5F;
		this.texU2 = this.texU1 + 0.5F;
		this.texV1 = texY * 0.5F;
		this.texV2 = this.texV1 + 0.5F;

		// Tweak the constant to change the size of the raindrop
		this.f4 = 0.07F * this.scale;

	}

	@Override
	public void configureColor() {
		final Color waterColor = BiomeUtil.getColorForLiquid(this.world, this.position);
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

		final float x = this.renderX(info, partialTicks);
		final float y = this.renderY(info, partialTicks);
		final float z = this.renderZ(info, partialTicks);

		float rotX = info.getRotation().getX();
		float rotY = info.getRotation().getY();
		float rotZ = info.getRotation().getZ();
		float rotYZ = 0.0F;
		float rotXY = 0.0F;
		float rotXZ = 0.0F;

		drawVertex(buffer, x + (-rotX * this.f4 - rotXY * this.f4), y + (-rotZ * this.f4),
				z + (-rotYZ * this.f4 - rotXZ * this.f4), this.texU2, this.texV2);
		drawVertex(buffer, x + (-rotX * this.f4 + rotXY * this.f4), y + (rotZ * this.f4),
				z + (-rotYZ * this.f4 + rotXZ * this.f4), this.texU2, this.texV1);
		drawVertex(buffer, x + (rotX * this.f4 + rotXY * this.f4), y + (rotZ * this.f4),
				z + (rotYZ * this.f4 + rotXZ * this.f4), this.texU1, this.texV1);
		drawVertex(buffer, x + (rotX * this.f4 - rotXY * this.f4), y + (-rotZ * this.f4),
				z + (rotYZ * this.f4 - rotXZ * this.f4), this.texU1, this.texV2);
	}

}

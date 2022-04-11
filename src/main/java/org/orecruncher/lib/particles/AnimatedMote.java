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

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.random.XorShiftRandom;

import java.util.Random;

public abstract class AnimatedMote extends MotionMote {

	protected static final Random RANDOM = XorShiftRandom.current();

	/**
	 * How many different textures there are to progress through as the particle
	 * decays
	 */
	protected float baseAirFriction = 0.91F;
	/**
	 * The red value to drift toward
	 */
	protected float fadeTargetRed;
	/**
	 * The green value to drift toward
	 */
	protected float fadeTargetGreen;
	/**
	 * The blue value to drift toward
	 */
	protected float fadeTargetBlue;
	/**
	 * True if setColorFade has been called
	 */
	protected boolean fadingColor;

	protected float dRed;
	protected float dGreen;
	protected float dBlue;

	protected double xAcceleration;
	protected double yAcceleration;
	protected double zAcceleration;

	protected float texU1, texU2;
	protected float texV1, texV2;
	protected float particleScale;

	protected final IAnimatedSprite sprites;

	protected AnimatedMote(final @Nonnull IAnimatedSprite sprites, @Nonnull final IBlockReader world, double x, double y,
						   double z, double dX, double dY, double dZ) {
		super(world, x, y, z, dX, dY, dZ);

		this.sprites = sprites;
		this.particleScale = (RANDOM.nextFloat() * 0.5F + 0.5F) * 2.0F;
	}

	public void setColor(final int rgb) {
		this.red = ((rgb & 16711680) >> 16) / 255F;
		this.green = ((rgb & 65280) >> 8) / 255F;
		this.blue = (rgb & 255) / 255F;
		this.alpha = 0.99F;
	}

	public void setColor(@Nonnull final Color color) {
		this.red = color.red();
		this.green = color.green();
		this.blue = color.blue();
		this.alpha = 0.99F;
	}

	public void setColorFade(final int rgb) {
		this.fadeTargetRed = ((rgb & 16711680) >> 16) / 255.0F;
		this.fadeTargetGreen = ((rgb & 65280) >> 8) / 255.0F;
		this.fadeTargetBlue = (rgb & 255) / 255.0F;
		this.fadingColor = true;
		lerpColors();
	}

	public void setColorFade(@Nonnull final Color color) {
		this.fadeTargetRed = color.red();
		this.fadeTargetGreen = color.green();
		this.fadeTargetBlue = color.blue();
		lerpColors();
	}

	private void lerpColors() {
		final float scaling = 1 / (float) this.maxAge;
		this.dRed = MathHelper.lerp(scaling, this.red, this.fadeTargetRed);
		this.dGreen = MathHelper.lerp(scaling, this.green, this.fadeTargetGreen);
		this.dBlue = MathHelper.lerp(scaling, this.blue, this.fadeTargetBlue);
	}

	@Override
	public void handleCollision(@Nonnull final ParticleCollisionResult collision) {
		this.motionX *= 0.699999988079071D;
		this.motionZ *= 0.699999988079071D;
	}

	@Override
	public void update() {

		this.motionY += this.yAcceleration;
		this.motionX += this.xAcceleration;
		this.motionZ += this.zAcceleration;
		this.motionX *= this.baseAirFriction;
		this.motionY *= this.baseAirFriction;
		this.motionZ *= this.baseAirFriction;

		super.update();

		if (isAlive()) {

			if (this.age > this.maxAge / 2) {
				this.alpha = (int) ((1.0F - ((float) this.age - (float) (this.maxAge / 2)) / this.maxAge) * 254);

				if (this.fadingColor) {
					this.red += this.dRed;
					this.green += this.dGreen;
					this.blue += this.dBlue;
				}
			}

			setParticleTexture();
		}
	}

	@Override
	public void updateBrightness()
	{
		this.packedLighting = 15728880;
	}

	@Override
	public void renderParticle(@Nonnull final IVertexBuilder buffer, @Nonnull final ActiveRenderInfo info, float partialTicks) {

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
			vector3f.mul(this.particleScale);
			vector3f.add(x, y, z);
		}

		drawVertex(buffer, avector3f[0].getX(), avector3f[0].getY(), avector3f[0].getZ(), this.texU2, this.texV2);
		drawVertex(buffer, avector3f[1].getX(), avector3f[1].getY(), avector3f[1].getZ(), this.texU2, this.texV1);
		drawVertex(buffer, avector3f[2].getX(), avector3f[2].getY(), avector3f[2].getZ(), this.texU1, this.texV1);
		drawVertex(buffer, avector3f[3].getX(), avector3f[3].getY(), avector3f[3].getZ(), this.texU1, this.texV2);
	}

	public void setParticleTexture() {
		final TextureAtlasSprite texture = this.sprites.get(this.age, this.maxAge);
		this.texU1 = texture.getMinU();
		this.texU2 = texture.getMaxU();
		this.texV1 = texture.getMinV();
		this.texV2 = texture.getMaxV();
	}

}

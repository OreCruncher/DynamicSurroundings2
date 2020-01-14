/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.random.XorShiftRandom;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
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

	protected double xAcceleration;
	protected double yAcceleration;
	protected double zAcceleration;

	protected float texU1, texU2;
	protected float texV1, texV2;
	protected float particleScale;

	protected final IAnimatedSprite sprites;

	protected AnimatedMote(final @Nonnull IAnimatedSprite sprites, @Nonnull final IWorldReader world, double x, double y,
						   double z, double dX, double dY, double dZ) {
		super(world, x, y, z, dX, dY, dZ);

		this.sprites = sprites;
		this.particleScale = (RANDOM.nextFloat() * 0.5F + 0.5F) * 2.0F;
	}

	public void setColor(final int rgb) {
		this.red = ((rgb & 16711680) >> 16);
		this.green = ((rgb & 65280) >> 8);
		this.blue = (rgb & 255);
		this.alpha = 255;
	}

	/**
	 * sets a color for the particle to drift toward (20% closer each tick, never
	 * actually getting very close)
	 */
	public void setColorFade(final int rgb) {
		this.fadeTargetRed = ((rgb & 16711680) >> 16) / 255.0F;
		this.fadeTargetGreen = ((rgb & 65280) >> 8) / 255.0F;
		this.fadeTargetBlue = (rgb & 255) / 255.0F;
		this.fadingColor = true;
	}

	@Override
	public void handleCollision() {
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
				this.alpha = (int) ((1.0F - ((float) this.age - (float) (this.maxAge / 2)) / this.maxAge) * 255);

				if (this.fadingColor) {
					this.red += (this.fadeTargetRed - this.red) * 0.2F;
					this.green += (this.fadeTargetGreen - this.green) * 0.2F;
					this.blue += (this.fadeTargetBlue - this.blue) * 0.2F;
				}
			}

			setParticleTexture();
		}
	}

	@Override
	public int getBrightnessForRender(final float partialTick) {
		return 15728880;
	}

	@Override
	public void render(final BufferBuilder buffer, final ActiveRenderInfo info, float partialTicks, float rotX,
					   float rotZ, float rotYZ, float rotXY, float rotXZ) {

		final double x = renderX(partialTicks);
		final double y = renderY(partialTicks);
		final double z = renderZ(partialTicks);

		drawVertex(buffer, x + (-rotX * this.particleScale - rotXY * this.particleScale),
				y + (-rotZ * this.particleScale),
				z + (-rotYZ * this.particleScale - rotXZ * this.particleScale), this.texU2, this.texV2);
		drawVertex(buffer, x + (-rotX * this.particleScale + rotXY * this.particleScale),
				y + (rotZ * this.particleScale),
				z + (-rotYZ * this.particleScale + rotXZ * this.particleScale), this.texU2, this.texV1);
		drawVertex(buffer, x + (rotX * this.particleScale + rotXY * this.particleScale),
				y + (rotZ * this.particleScale),
				z + (rotYZ * this.particleScale + rotXZ * this.particleScale), this.texU1, this.texV1);
		drawVertex(buffer, x + (rotX * this.particleScale - rotXY * this.particleScale),
				y + (-rotZ * this.particleScale),
				z + (rotYZ * this.particleScale - rotXZ * this.particleScale), this.texU1, this.texV2);
	}

	public void setParticleTexture() {
		final TextureAtlasSprite texture = this.sprites.get(this.age, this.maxAge);
		this.texU1 = texture.getMinU();
		this.texU2 = texture.getMaxU();
		this.texV1 = texture.getMinV();
		this.texV2 = texture.getMaxV();
	}

}

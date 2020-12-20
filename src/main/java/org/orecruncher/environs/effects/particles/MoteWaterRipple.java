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
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.client.renderer.BufferBuilder;
import org.orecruncher.environs.library.BiomeUtil;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.particles.AgeableMote;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class MoteWaterRipple extends AgeableMote {

	private static final float TEX_SIZE_HALF = 0.5F;

	protected final float growthRate;
	protected float scale;
	protected float scaledWidth;

	protected float texU1, texU2;
	protected float texV1, texV2;

	public MoteWaterRipple(final IBlockReader world, final double x, final double y, final double z) {
		super(world, x, y, z);

		final RippleStyle style = RippleStyle.get();

		this.maxAge = style.getMaxAge();

		if (style.doScaling()) {
			this.growthRate = this.maxAge / 500F;
			this.scale = this.growthRate;
			this.scaledWidth = this.scale * TEX_SIZE_HALF;
		} else {
			this.growthRate = 0F;
			this.scale = 0F;
			this.scaledWidth = 0.5F;
		}

		this.posY -= 0.2D;

		final Color waterColor = BiomeUtil.getColorForLiquid(world, this.position);
		this.red = waterColor.red();
		this.green = waterColor.green();
		this.blue = waterColor.blue();
		this.alpha = 0.99F;

		this.texU1 = style.getU1(this.age);
		this.texU2 = style.getU2(this.age);
		this.texV1 = style.getV1(this.age);
		this.texV2 = style.getV2(this.age);
	}

	@Override
	public void update() {
		final RippleStyle style = RippleStyle.get();
		if (style.doScaling()) {
			this.scale += this.growthRate;
			this.scaledWidth = this.scale * TEX_SIZE_HALF;
		}

		if (style.doAlpha()) {
			this.alpha = (float) (this.maxAge - this.age) / (float) (this.maxAge + 3);
		}

		this.texU1 = style.getU1(this.age);
		this.texU2 = style.getU2(this.age);
		this.texV1 = style.getV1(this.age);
		this.texV2 = style.getV2(this.age);
	}

	@Override
	public void renderParticle(@Nonnull IVertexBuilder buffer, @Nonnull ActiveRenderInfo info, float partialTicks) {

		final float x = this.renderX(info, partialTicks);
		final float y = this.renderY(info, partialTicks);
		final float z = this.renderZ(info, partialTicks);

		drawVertex(buffer, -this.scaledWidth + x, y, this.scaledWidth + z, this.texU2, this.texV2);
		drawVertex(buffer, this.scaledWidth + x, y, this.scaledWidth + z, this.texU2, this.texV1);
		drawVertex(buffer, this.scaledWidth + x, y, -this.scaledWidth + z, this.texU1, this.texV1);
		drawVertex(buffer, -this.scaledWidth + x, y, -this.scaledWidth + z, this.texU1, this.texV2);
	}

}

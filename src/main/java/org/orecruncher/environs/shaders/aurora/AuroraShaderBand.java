/*
 *  Dynamic Surroundings: Environs
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

package org.orecruncher.environs.shaders.aurora;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.orecruncher.environs.shaders.Shaders;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.orecruncher.lib.opengl.OpenGlState;
import org.orecruncher.lib.opengl.OpenGlUtil;
import org.orecruncher.lib.shaders.ShaderProgram;

/*
 * Renders a shader generated aurora along a curved path.  Makes it ribbon like.
 */
@OnlyIn(Dist.CLIENT)
public class AuroraShaderBand extends AuroraBase {

	private static final double zero = 0;
	private static final float v1 = 0;
	private static final float v2 = 1F;
	
	protected ShaderProgram program;
	protected ShaderProgram.IShaderUseCallback callback;
	protected final float auroraWidth;
	protected final float panelTexWidth;
	
	//protected final BufferBuilder buffer;

	public AuroraShaderBand(final long seed) {
		super(seed, true);

		this.program = Shaders.AURORA;

		this.callback = shader -> {
			shader.set("time", AuroraUtils.getTimeSeconds() * 0.75F);
			shader.set("resolution", AuroraShaderBand.this.getAuroraWidth(), AuroraShaderBand.this.getAuroraHeight());
			shader.set("topColor", AuroraShaderBand.this.getFadeColor());
			shader.set("middleColor", AuroraShaderBand.this.getMiddleColor());
			shader.set("bottomColor", AuroraShaderBand.this.getBaseColor());
			shader.set("alpha", AuroraShaderBand.this.getAlpha());
		};

		this.auroraWidth = this.band.getNodeList().length * this.band.getNodeWidth();
		this.panelTexWidth = this.band.getNodeWidth() / this.auroraWidth;

		//this.buffer = createList();
	}

	@Override
	protected float getAlpha() {
		return MathStuff.clamp((this.band.getAlphaLimit() / 255F) * this.tracker.ageRatio() * 2.0F, 0F, 1F);
	}

	protected float getAuroraWidth() {
		return this.auroraWidth;
	}

	protected float getAuroraHeight() {
		return AuroraBand.AURORA_AMPLITUDE;
	}
	
	// Build out our aurora render area so we can reapply it each
	// render pass.  I am thinking there is a better way but
	// I don't know alot about this area of Minecraft.
	protected BufferBuilder createList() {
		final BufferBuilder renderer = new BufferBuilder(4096);
		final Panel[] array = this.band.getNodeList();
		
		renderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
		
		// Get the strip started
		final double posY = array[0].getModdedY();
		final double posX = array[0].tetX;
		final double posZ = array[0].tetZ;
		renderer.pos(posX, zero, posZ).tex(0, 0).endVertex();
		renderer.pos(posX, posY, posZ).tex(0, 1F).endVertex();
		
		for (int i = 0; i < array.length - 1; i++) {

			final float u1 = i * this.panelTexWidth;
			final float u2 = u1 + this.panelTexWidth;

			final double posX2;
			final double posZ2;
			final double posY2;

			if (i < array.length - 2) {
				final Panel nodePlus = array[i + 1];
				posX2 = nodePlus.tetX;
				posZ2 = nodePlus.tetZ;
				posY2 = nodePlus.getModdedY();
			} else {
				final Panel node = array[i];
				posX2 = node.posX;
				posZ2 = node.getModdedZ();
				posY2 = 0.0D;
			}

			renderer.pos(posX2, zero, posZ2).tex(u2, v1).endVertex();
			renderer.pos(posX2, posY2, posZ2).tex(u2, v2).endVertex();
		}
		
		renderer.finishDrawing();
		
		return renderer;
	}

	@Override
	public void render(final float partialTick) {

		if (this.program == null)
			return;

		this.band.translate(partialTick);

		final double tranY = getTranslationY(partialTick);
		final double tranX = getTranslationX(partialTick);
		final double tranZ = getTranslationZ(partialTick);

		final OpenGlState glState = OpenGlState.push();
		GlStateManager.disableLighting();
		OpenGlUtil.setAuroraBlend();
		GL11.glFrontFace(GL11.GL_CW);

		try {

			this.program.use(this.callback);

			for (int b = 0; b < this.bandCount; b++) {
				GlStateManager.pushMatrix();
				GlStateManager.translated(tranX, tranY, tranZ + this.offset * b);
				GlStateManager.scaled(0.5D, 10.0D, 0.5D);
				WorldVertexBufferUploader.draw(createList());
				//this.buffer.reset();
				GlStateManager.popMatrix();
			}

		} catch (final Exception ex) {
			ex.printStackTrace();
			this.program = null;
		} finally {
			try {
				if (this.program != null)
					this.program.unUse();
			} catch (final Throwable ignored) {
				;
			}
		}

		GL11.glFrontFace(GL11.GL_CCW);
		OpenGlState.pop(glState);
	}

	@Override
	public String toString() {
		return "<SHADER> " + super.toString();
	}
}

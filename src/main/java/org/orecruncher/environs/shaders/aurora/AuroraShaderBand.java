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

package org.orecruncher.environs.shaders.aurora;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.orecruncher.environs.shaders.Shaders;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.math.MathStuff;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/*
 * Renders a shader generated aurora along a curved path.  Makes it ribbon like.
 */
@OnlyIn(Dist.CLIENT)
public class AuroraShaderBand extends AuroraBase {

	private static final float ZERO = 0;
	private static final float V1 = 0;
	private static final float V2 = 1F;
	
	protected Shaders.Programs program;
	protected Consumer<Shaders.ShaderCallContext> callback;

	protected final float auroraWidth;
	protected final float panelTexWidth;
	
	public AuroraShaderBand(final long seed) {
		super(seed);

		this.program = Shaders.Programs.AURORA;

		this.callback = shaderCallContext -> {
			shaderCallContext.set("time", AuroraUtils.getTimeSeconds() * 0.75F);
			shaderCallContext.set("resolution", AuroraShaderBand.this.getAuroraWidth(), AuroraShaderBand.this.getAuroraHeight());
			shaderCallContext.set("topColor", AuroraShaderBand.this.getFadeColor());
			shaderCallContext.set("middleColor", AuroraShaderBand.this.getMiddleColor());
			shaderCallContext.set("bottomColor", AuroraShaderBand.this.getBaseColor());
			shaderCallContext.set("alpha", AuroraShaderBand.this.getAlpha());
		};

		this.auroraWidth = this.band.getNodeList().length * this.band.getNodeWidth();
		this.panelTexWidth = this.band.getNodeWidth() / this.auroraWidth;
	}

	@Override
	public void update() {
		super.update();
		this.band.update();
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

	protected void generateBandQuadLines(@Nonnull final Matrix4f matrix) {

		final RenderType renderType = AuroraRenderType.QUAD_LINES;
		final IRenderTypeBuffer.Impl buffer = GameUtils.getMC().getRenderTypeBuffers().getBufferSource();
		final IVertexBuilder renderer = buffer.getBuffer(renderType);

		for (int i = 0; ; i++) {
			final Vector3f[] quad = this.band.getPanelQuad(i);
			if (quad == null)
				break;

			final float u1 = i * this.panelTexWidth;
			final float u2 = u1 + this.panelTexWidth;

			renderer.pos(matrix, quad[0].getX(), quad[0].getY(), quad[0].getZ()).color(0, 255, 0, 254).endVertex();
			renderer.pos(matrix, quad[1].getX(), quad[1].getY(), quad[1].getZ()).color(0, 255, 0, 254).endVertex();
			renderer.pos(matrix, quad[2].getX(), quad[2].getY(), quad[2].getZ()).color(0, 255, 0, 254).endVertex();
			renderer.pos(matrix, quad[3].getX(), quad[3].getY(), quad[3].getZ()).color(0, 255, 0, 254).endVertex();
		}

		RenderSystem.disableDepthTest();
		buffer.finish(renderType);

	}

	protected void generateBandQuad(@Nonnull final Matrix4f matrix) {

		final RenderType renderType = AuroraRenderType.QUADS;
		final IRenderTypeBuffer.Impl buffer = GameUtils.getMC().getRenderTypeBuffers().getBufferSource();
		final IVertexBuilder renderer = buffer.getBuffer(renderType);

		for (int i = 0; ; i++) {
			final Vector3f[] quad = this.band.getPanelQuad(i);
			if (quad == null)
				break;

			final float u1 = i * this.panelTexWidth;
			final float u2 = u1 + this.panelTexWidth;

			renderer.pos(matrix, quad[0].getX(), quad[0].getY(), quad[0].getZ()).tex(u2, V2).endVertex();
			renderer.pos(matrix, quad[1].getX(), quad[1].getY(), quad[1].getZ()).tex(u2, V1).endVertex();
			renderer.pos(matrix, quad[2].getX(), quad[2].getY(), quad[2].getZ()).tex(u1, V1).endVertex();
			renderer.pos(matrix, quad[3].getX(), quad[3].getY(), quad[3].getZ()).tex(u1, V2).endVertex();
		}

		RenderSystem.disableDepthTest();
		buffer.finish(renderType);
	}

	// Build out our aurora render area so we can reapply it each
	// render pass.  I am thinking there is a better way but
	// I don't know alot about this area of Minecraft.
	protected void generateBand(@Nonnull final Matrix4f matrix) {

		final IRenderTypeBuffer.Impl buffer = GameUtils.getMC().getRenderTypeBuffers().getBufferSource();
		final IVertexBuilder renderer = buffer.getBuffer(AuroraRenderType.RENDER_TYPE);
		final Panel[] array = this.band.getNodeList();
		
		// Get the strip started
		final float posY = array[0].getModdedY();
		final float posX = array[0].tetX;
		final float posZ = array[0].tetZ;

		renderer.pos(matrix, posX, ZERO, posZ).tex(0, 0).endVertex();
		renderer.pos(matrix, posX, posY, posZ).tex(0, 1F).endVertex();
		
		for (int i = 0; i < array.length - 1; i++) {

			final float u1 = i * this.panelTexWidth;
			final float u2 = u1 + this.panelTexWidth;

			final float posX2;
			final float posZ2;
			final float posY2;

			if (i < array.length - 2) {
				final Panel nodePlus = array[i + 1];
				posX2 = nodePlus.tetX;
				posZ2 = nodePlus.tetZ;
				posY2 = nodePlus.getModdedY();
			} else {
				final Panel node = array[i];
				posX2 = node.posX;
				posZ2 = node.getModdedZ();
				posY2 = 0.0F;
			}

			renderer.pos(matrix, posX2, ZERO, posZ2).tex(u2, V1).endVertex();
			renderer.pos(matrix, posX2, posY2, posZ2).tex(u2, V2).endVertex();
		}

		RenderSystem.disableDepthTest();
		buffer.finish(AuroraRenderType.RENDER_TYPE);
	}

	@Override
	public void render(@Nonnull final RenderWorldLastEvent event) {

		if (this.program == null)
			return;

		final MatrixStack matrixStack = event.getMatrixStack();
		final float partialTick = event.getPartialTicks();
		this.band.translate(partialTick);

		final Vector3d view = GameUtils.getMC().gameRenderer.getActiveRenderInfo().getProjectedView();
		matrixStack.push();
		matrixStack.translate(-view.getX(), -view.getY(), -view.getZ());

		final double tranY = getTranslationY(partialTick);
		final double tranX = getTranslationX(partialTick);
		final double tranZ = getTranslationZ(partialTick);

		//GlStateManager.disableLighting();
		//GlStateManager.disableBlend();
		//OpenGlUtil.setAuroraBlend();
		//GL11.glFrontFace(GL11.GL_CW);

		//Shaders.useShader(this.program, this.callback);

		try {

			for (int b = 0; b < this.bandCount; b++) {
				matrixStack.push();
				matrixStack.translate(tranX, tranY, tranZ + this.offset * b);
				//matrixStack.scale(1F, 10F, 1F);
				generateBandQuadLines(matrixStack.getLast().getMatrix());
				matrixStack.pop();
			}

		} catch (final Exception ex) {
			ex.printStackTrace();
			this.program = null;
		}

		Shaders.releaseShader();

		matrixStack.pop();
		//GL11.glFrontFace(GL11.GL_CCW);
	}

	@Override
	public String toString() {
		return "<SHADER> " + super.toString();
	}
}

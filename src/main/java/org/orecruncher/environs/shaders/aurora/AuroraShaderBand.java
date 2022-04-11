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
import org.orecruncher.environs.shaders.ShaderPrograms;
import org.orecruncher.lib.shaders.ShaderCallContext;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.math.MathStuff;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/*
 * Renders a shader generated aurora along a curved path.  Makes it ribbon like.
 */
public class AuroraShaderBand extends AuroraBase {

	private static final float V1 = 0;
	private static final float V2 = 0.5F;
	
	protected ShaderPrograms program;
	protected Consumer<ShaderCallContext> callback;

	protected final float auroraWidth;
	protected final float panelTexWidth;
	
	public AuroraShaderBand(final long seed) {
		super(seed);

		this.program = ShaderPrograms.AURORA;

		this.callback = shaderCallContext -> {
			shaderCallContext.set("time", AuroraUtils.getTimeSeconds() * 0.75F);
			shaderCallContext.set("resolution", AuroraShaderBand.this.getAuroraWidth(), AuroraShaderBand.this.getAuroraHeight());
			shaderCallContext.set("topColor", AuroraShaderBand.this.getFadeColor());
			shaderCallContext.set("middleColor", AuroraShaderBand.this.getMiddleColor());
			shaderCallContext.set("bottomColor", AuroraShaderBand.this.getBaseColor());
			shaderCallContext.set("alpha", AuroraShaderBand.this.getAlpha());
		};

		this.auroraWidth = this.band.getPanelCount() * this.band.getNodeWidth();
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

	protected void generateBand(@Nonnull final IVertexBuilder builder, @Nonnull final Matrix4f matrix) {

		for (int i = 0; ; i++) {
			final Vector3f[] quad = this.band.getPanelQuad(i);
			if (quad == null)
				break;

			final float u1 = i * this.panelTexWidth;
			final float u2 = u1 + this.panelTexWidth;

			builder.pos(matrix, quad[0].getX(), quad[0].getY(), quad[0].getZ()).tex(u1, V1).endVertex();
			builder.pos(matrix, quad[1].getX(), quad[1].getY(), quad[1].getZ()).tex(u2, V1).endVertex();
			builder.pos(matrix, quad[2].getX(), quad[2].getY(), quad[2].getZ()).tex(u2, V2).endVertex();
			builder.pos(matrix, quad[3].getX(), quad[3].getY(), quad[3].getZ()).tex(u1, V2).endVertex();
		}

	}

	@Override
	public void render(@Nonnull final MatrixStack matrixStack, final float partialTick) {

		if (this.program == null)
			return;

		this.band.translate(partialTick);

		final double tranY = getTranslationY(partialTick);
		final double tranX = getTranslationX(partialTick);
		final double tranZ = getTranslationZ(partialTick);

		final Vector3d view = GameUtils.getMC().gameRenderer.getActiveRenderInfo().getProjectedView();
		matrixStack.push();
		matrixStack.translate(-view.getX(), -view.getY(), -view.getZ());

		final RenderType type = AuroraRenderType.QUAD;
		final IRenderTypeBuffer.Impl buffer = GameUtils.getMC().getRenderTypeBuffers().getBufferSource();

		ShaderPrograms.MANAGER.useShader(this.program, this.callback);

		try {

			for (int b = 0; b < this.bandCount; b++) {
				final IVertexBuilder builder = buffer.getBuffer(type);
				matrixStack.push();
				matrixStack.translate(tranX, tranY, tranZ + this.offset * b);
				generateBand(builder, matrixStack.getLast().getMatrix());
				matrixStack.pop();
				RenderSystem.disableDepthTest();
				buffer.finish(type);
			}

		} catch (final Exception ex) {
			ex.printStackTrace();
			this.program = null;
		}

		ShaderPrograms.MANAGER.releaseShader();

		matrixStack.pop();
	}

	@Override
	public String toString() {
		return "<SHADER> " + super.toString();
	}
}

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

package org.orecruncher.lib.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.shader.IShaderManager;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.client.shader.ShaderLoader;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL20;
import net.minecraft.util.ResourceLocation;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.Lib;
import org.orecruncher.lib.gui.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public final class ShaderManager {

	private final IShaderResourceProvider[] providers;
	private final ShaderProgram[] PROGRAMS;

	public ShaderManager(@Nonnull final IShaderResourceProvider[] providers) {
		this.providers = providers;
		this.PROGRAMS = new ShaderProgram[providers.length];
	}

	public static boolean supported() {
		return true;
	}

	public void useShader(final int shader, @Nullable final Consumer<ShaderCallContext> callback) {
		if (shader < 0 || shader > this.PROGRAMS.length)
			throw new IllegalArgumentException("The shader id provided is out of bounds");

		if (!supported())
			return;

		final ShaderProgram program = this.PROGRAMS[shader];

		if (program == null)
			return;

		final int programId = program.getProgram();
		ShaderLinkHelper.func_227804_a_(programId);

		if (callback != null) {
			callback.accept(new ShaderCallContext(programId));
		}
	}

	public void useShader(@Nonnull final Enum<?> provider, @Nullable final Consumer<ShaderCallContext> callback) {
		useShader(provider.ordinal(), callback);
	}

	public void useShader(@Nonnull final Enum<?> provider) {
		useShader(provider, null);
	}

	public void releaseShader() {
		if (supported())
			ShaderLinkHelper.func_227804_a_(0);
	}

	public final static class ShaderCallContext {

		private final int program;

		ShaderCallContext(final int program) {
			this.program = program;
		}

		public void set(@Nonnull final String uniform, final float value) {
			GL20.glUniform1f(getUniform(uniform), value);
		}

		public void set(@Nonnull final String uniform, final float v1, final float v2) {
			GL20.glUniform2f(getUniform(uniform), v1, v2);
		}

		public void set(@Nonnull final String uniform, @Nonnull final Color color) {
			set(uniform, color, 1.0F);
		}

		public void set(@Nonnull final String uniform, @Nonnull final Color color, final float alpha) {
			final float[] parms = new float[]{color.red(), color.green(), color.blue(), alpha};
			GL20.glUniform4fv(getUniform(uniform), parms);
		}

		public void set(@Nonnull final String uniform, @Nonnull final int... values) {
			GL20.glUniform1iv(getUniform(uniform), values);
		}

		private int getUniform(@Nonnull final String name) {
			return GlStateManager.getUniformLocation(this.program, name);
		}
	}

	private static class ShaderProgram implements IShaderManager {
		private final int program;
		private final ShaderLoader vert;
		private final ShaderLoader frag;

		private ShaderProgram(int program, @Nonnull final ShaderLoader vert, @Nonnull final ShaderLoader frag) {
			this.program = program;
			this.vert = vert;
			this.frag = frag;
		}

		@Override
		public int getProgram() {
			return program;
		}

		@Override
		public void markDirty() {

		}

		@Override
		@Nonnull
		public ShaderLoader getVertexShaderLoader() {
			return vert;
		}

		@Override
		@Nonnull
		public ShaderLoader getFragmentShaderLoader() {
			return frag;
		}
	}

	@SuppressWarnings("deprecation")
	public void initShaders() {
		if (!supported())
			return;

		if (GameUtils.getMC().getResourceManager() instanceof IReloadableResourceManager) {
			((IReloadableResourceManager) GameUtils.getMC().getResourceManager()).addReloadListener(
					(IResourceManagerReloadListener) manager -> {
						for (final ShaderProgram prog : PROGRAMS)
							if (prog != null)
								ShaderLinkHelper.deleteShader(prog);
						Arrays.fill(PROGRAMS, null);
						loadShaders(manager);
					});
		}
	}

	private void loadShaders(@Nonnull final IResourceManager manager) {
		for (int i = 0; i < this.providers.length; i++)
			this.PROGRAMS[i] = createProgram(manager, this.providers[i]);
	}

	@Nullable
	private static ShaderProgram createProgram(@Nonnull final IResourceManager manager, @Nonnull final IShaderResourceProvider provider) {
		try {
			final ShaderLoader vert = createShader(manager, provider.getVertex(), ShaderLoader.ShaderType.VERTEX);
			final ShaderLoader frag = createShader(manager, provider.getFragment(), ShaderLoader.ShaderType.FRAGMENT);
			final int progId = ShaderLinkHelper.createProgram();
			final ShaderProgram prog = new ShaderProgram(progId, vert, frag);
			ShaderLinkHelper.linkProgram(prog);
			return prog;
		} catch (IOException ex) {
			Lib.LOGGER.error(ex, "Failed to load program %s", provider.getShaderName());
		}
		return null;
	}

	private static ShaderLoader createShader(@Nonnull final IResourceManager manager, @Nonnull final ResourceLocation loc, @Nonnull final ShaderLoader.ShaderType shaderType) throws IOException {
		try (InputStream is = new BufferedInputStream(manager.getResource(loc).getInputStream())) {
			return ShaderLoader.func_216534_a(shaderType, loc.toString(), is, shaderType.name().toLowerCase(Locale.ROOT));
		}
	}
}

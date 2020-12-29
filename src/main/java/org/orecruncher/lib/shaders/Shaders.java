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
import sun.misc.SharedSecrets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Locale;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public final class Shaders<T extends Enum<T>> {

	private final Class<T> enumClass;
	private final EnumMap<T, ShaderProgram> PROGRAMS;

	public Shaders(@Nonnull final Class<T> enumClazz) {
		if (!IShaderResourceProvider.class.isAssignableFrom(enumClazz))
			throw new IllegalArgumentException("Enum must implement IShaderResourceProvider");

		this.enumClass = enumClazz;
		this.PROGRAMS = new EnumMap<>(enumClazz);
	}

	public static boolean areSupported() {
		return true;
	}

	public void useShader(@Nonnull final T shader, @Nullable final Consumer<ShaderCallContext> callback) {

		ShaderProgram prog = null;

		if (areSupported())
			prog = PROGRAMS.get(shader);

		if (prog == null) {
			return;
		}

		int program = prog.getProgram();
		ShaderLinkHelper.func_227804_a_(program);

		if (callback != null) {
			callback.accept(new ShaderCallContext(program));
		}
	}

	public void useShader(@Nonnull final T shader) {
		useShader(shader, null);
	}

	public void releaseShader() {
		if (areSupported())
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
		if (!areSupported())
			return;

		if (GameUtils.getMC().getResourceManager() instanceof IReloadableResourceManager) {
			((IReloadableResourceManager) GameUtils.getMC().getResourceManager()).addReloadListener(
					(IResourceManagerReloadListener) manager -> {
						PROGRAMS.values().forEach(ShaderLinkHelper::deleteShader);
						PROGRAMS.clear();
						loadShaders(manager);
					});
		}
	}

	private void loadShaders(@Nonnull final IResourceManager manager) {
		for (final T shader : this.getEnumValues()) {
			createProgram(manager, shader);
		}
	}

	private void createProgram(@Nonnull final IResourceManager manager, @Nonnull final T shader) {
		try {
			IShaderResourceProvider provider = (IShaderResourceProvider) shader;
			ShaderLoader vert = createShader(manager, provider.getVertex(), ShaderLoader.ShaderType.VERTEX);
			ShaderLoader frag = createShader(manager, provider.getFragment(), ShaderLoader.ShaderType.FRAGMENT);
			int progId = ShaderLinkHelper.createProgram();
			ShaderProgram prog = new ShaderProgram(progId, vert, frag);
			ShaderLinkHelper.linkProgram(prog);
			PROGRAMS.put(shader, prog);
		} catch (IOException ex) {
			Lib.LOGGER.error(ex, "Failed to load program %s", shader.name());
		}
	}

	private T[] getEnumValues() {
		return SharedSecrets.getJavaLangAccess().getEnumConstantsShared(this.enumClass);
	}

	private static ShaderLoader createShader(@Nonnull final IResourceManager manager, @Nonnull final ResourceLocation loc, @Nonnull final ShaderLoader.ShaderType shaderType) throws IOException {
		try (InputStream is = new BufferedInputStream(manager.getResource(loc).getInputStream())) {
			return ShaderLoader.func_216534_a(shaderType, loc.toString(), is, shaderType.name().toLowerCase(Locale.ROOT));
		}
	}
}

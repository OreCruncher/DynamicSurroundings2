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

import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.client.shader.ShaderLoader;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.util.ResourceLocation;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.Lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class ShaderManager<T extends Enum<T> & IShaderResourceProvider> {

	private final Class<T> clazz;
	private final EnumMap<T, ShaderProgram> programs;
	private final Supplier<Boolean> supportCheck;

	public ShaderManager(@Nonnull final Class<T> clazz) {
		this(clazz, () -> true);
	}

	public ShaderManager(@Nonnull final Class<T> clazz, @Nonnull final Supplier<Boolean> supportCheck) {
		Objects.requireNonNull(clazz);
		this.clazz = clazz;
		this.programs = new EnumMap<>(clazz);
		this.supportCheck = supportCheck;

		// Validate the entries provide sane info
		for (final T shader : clazz.getEnumConstants()) {
			final String shaderName = shader.name();
			Objects.requireNonNull(shader.getVertex(), String.format("%s provided null for vertex shader", shaderName));
			Objects.requireNonNull(shader.getFragment(), String.format("%s provided null for fragment shader", shaderName));
		}
	}

	public boolean supported() {
		return this.supportCheck.get();
	}

	public void useShader(@Nonnull final T shader, @Nullable final Consumer<ShaderCallContext> callback) {
		Objects.requireNonNull(shader);

		if (!supported())
			return;

		final ShaderProgram program = this.programs.get(shader);

		if (program == null)
			return;

		final int programId = program.getProgram();
		ShaderLinkHelper.func_227804_a_(programId);

		if (callback != null) {
			callback.accept(new ShaderCallContext(program));
		}
	}

	@SuppressWarnings("unused")
	public void useShader(@Nonnull final T shader) {
		useShader(shader, null);
	}

	public void releaseShader() {
		if (supported())
			ShaderLinkHelper.func_227804_a_(0);
	}

	@SuppressWarnings("deprecation")
	public void initShaders() {
		if (!supported())
			return;

		if (GameUtils.getMC().getResourceManager() instanceof IReloadableResourceManager) {
			((IReloadableResourceManager) GameUtils.getMC().getResourceManager()).addReloadListener(
					(IResourceManagerReloadListener) manager -> {
						this.programs.values().forEach(ShaderLinkHelper::deleteShader);
						this.programs.clear();
						loadShaders(manager);
					});
		}
	}

	private void loadShaders(@Nonnull final IResourceManager manager) {
		for (final T shader : this.clazz.getEnumConstants()) {
			final ShaderProgram program = createProgram(manager, shader);
			if (program != null)
				this.programs.put(shader, createProgram(manager, shader));
		}
	}

	@Nullable
	private ShaderProgram createProgram(@Nonnull final IResourceManager manager, @Nonnull final T shader) {
		try {
			final ShaderLoader vert = createShader(manager, shader.getVertex(), ShaderLoader.ShaderType.VERTEX);
			final ShaderLoader frag = createShader(manager, shader.getFragment(), ShaderLoader.ShaderType.FRAGMENT);
			final int programId = ShaderLinkHelper.createProgram();
			final ShaderProgram program = new ShaderProgram(shader.getShaderName(), programId, vert, frag);
			ShaderLinkHelper.linkProgram(program);
			program.setUniforms(shader.getUniforms());
			return program;
		} catch (IOException ex) {
			Lib.LOGGER.error(ex, "Failed to load program %s", shader.getShaderName());
		}
		return null;
	}

	private static ShaderLoader createShader(@Nonnull final IResourceManager manager, @Nonnull final ResourceLocation loc, @Nonnull final ShaderLoader.ShaderType shaderType) throws IOException {
		try (InputStream is = new BufferedInputStream(manager.getResource(loc).getInputStream())) {
			return ShaderLoader.func_216534_a(shaderType, loc.toString(), is, shaderType.name().toLowerCase(Locale.ROOT));
		}
	}
}

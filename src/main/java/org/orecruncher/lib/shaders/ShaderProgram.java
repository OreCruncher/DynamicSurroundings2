/*
 * Dynamic Surroundings: Sound Control
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

package org.orecruncher.lib.shaders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import org.orecruncher.lib.ResourceUtils;
import org.orecruncher.lib.gui.Color;

@OnlyIn(Dist.CLIENT)
public class ShaderProgram {

	protected final String name;
	protected int programId = -1;
	protected boolean isDrawing = false;

	protected int textureIndex = 0;

	public ShaderProgram(final String name) {
		if (StringUtils.isEmpty(name))
			this.name = "UNKNOWN";
		else
			this.name = name;
	}

	protected void initialize(final String vertexSrc, final String fragmentSrc) throws ShaderException {

		int vertShader = 0;
		int fragShader = 0;

		vertShader = createShader(vertexSrc, ARBVertexShader.GL_VERTEX_SHADER_ARB);
		fragShader = createShader(fragmentSrc, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

		if (vertShader == 0 || fragShader == 0)
			throw new ShaderException(this.name, "Unable to intialize shader!");

		this.programId = ARBShaderObjects.glCreateProgramObjectARB();

		if (this.programId != 0) {
			ARBShaderObjects.glAttachObjectARB(this.programId, vertShader);
			ARBShaderObjects.glAttachObjectARB(this.programId, fragShader);

			ARBShaderObjects.glLinkProgramARB(this.programId);
			if (ARBShaderObjects.glGetObjectParameteriARB(this.programId,
					ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
				throw new ShaderException(this.name, getLogInfo(this.programId));
			}

			ARBShaderObjects.glValidateProgramARB(this.programId);
			if (ARBShaderObjects.glGetObjectParameteriARB(this.programId,
					ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
				throw new ShaderException(this.name, getLogInfo(this.programId));
			}
		}
	}

	public final String getName() {
		return this.name;
	}

	public final int getProgramId() {
		return this.programId;
	}

	public final boolean isValid() {
		return this.programId != 0;
	}

	public final void use() throws ShaderException {
		this.use(null);
	}

	public final void unUse() throws ShaderException {
		try {
			final String log = getLogInfo(this.programId);
			if (!StringUtils.isEmpty(log))
				throw new ShaderException(this.name, log);
		} finally {
			ARBShaderObjects.glUseProgramObjectARB(0);
		}
	}

	public final void delete() {
		if (isValid()) {
			ARBShaderObjects.glUseProgramObjectARB(0);
			ARBShaderObjects.glDeleteObjectARB(this.programId);
		}
	}

	public final void use(final IShaderUseCallback callback) throws ShaderException {

		this.textureIndex = 0;
		ARBShaderObjects.glUseProgramObjectARB(this.programId);

		if (callback != null)
			callback.call(this);
	}

	protected final int getUniform(final String name) throws ShaderException {
		if (!isValid())
			throw new ShaderException(this.name, "ShaderProgram is not valid!");

		final int id = ARBShaderObjects.glGetUniformLocationARB(this.programId, name);
		if (id == -1)
			throw new ShaderException(this.name, String.format("Unknown uniform '%s'", name));

		return id;
	}

	public void set(final String name, final float... value) throws ShaderException {
		if (value.length == 0 || value.length > 4)
			throw new ShaderException(this.name, "Invalid number of elements");

		final int id = getUniform(name);
		switch (value.length) {
			case 1:
				ARBShaderObjects.glUniform1fARB(id, value[0]);
				break;
			case 2:
				ARBShaderObjects.glUniform2fARB(id, value[0], value[1]);
				break;
			case 3:
				ARBShaderObjects.glUniform3fARB(id, value[0], value[1], value[2]);
				break;
			case 4:
				ARBShaderObjects.glUniform4fARB(id, value[0], value[1], value[2], value[3]);
				break;
		}
	}

	public void set(final String name, final Color color) throws ShaderException {
		this.set(name, color, 1.0F);
	}

	public void set(final String name, final Color color, final float alpha) throws ShaderException {
		this.set(name, color.red(), color.green(), color.blue(), alpha);
	}

	public void set(final String name, final Vector3d vector) throws ShaderException {
		this.set(name, (float) vector.x, (float) vector.y, (float) vector.z);
	}

	public void set(final String name, final Vector3i vector) throws ShaderException {
		this.set(name, vector.getX(), vector.getY(), vector.getZ());
	}

	public void set(final String name, final Vector2f vector) throws ShaderException {
		this.set(name, vector.x, vector.y);
	}

	public void set(final String name, final int value) throws ShaderException {
		final int id = getUniform(name);
		ARBShaderObjects.glUniform1iARB(id, value);
	}
/*
	public void set(final String name, final ResourceLocation texture) throws ShaderException {
		final int textureId = this.textureIndex++;
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit + textureId);
		GlStateManager.enableTexture2D();
		GameUtils.getMC().getTextureManager().bindTexture(texture);
	}

 */

	protected void initialize(final ResourceLocation vertexResource, final ResourceLocation fragmentResource)
			throws Exception {
		final String vertex = ResourceUtils.readResource(vertexResource);
		final String fragment = ResourceUtils.readResource(fragmentResource);
		this.initialize(vertex, fragment);
	}

	public static ShaderProgram createProgram(final String name, final ResourceLocation vertex,
			final ResourceLocation fragment) throws Exception {
		final ShaderProgram prog = new ShaderProgram(name);
		prog.initialize(vertex, fragment);
		return prog;
	}

	private int createShader(final String src, final int shaderType) throws ShaderException {
		int shader = 0;
		try {
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

			if (shader == 0)
				return 0;

			ARBShaderObjects.glShaderSourceARB(shader, src);
			ARBShaderObjects.glCompileShaderARB(shader);

			if (ARBShaderObjects.glGetObjectParameteriARB(shader,
					ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
				throw new ShaderException(this.name, getLogInfo(shader));

			return shader;
		} catch (final Exception exc) {
			ARBShaderObjects.glDeleteObjectARB(shader);
			throw exc;
		}
	}

	private static String getLogInfo(final int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj,
				ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	public interface IShaderUseCallback {
		void call(final ShaderProgram program) throws ShaderException;
	}
}

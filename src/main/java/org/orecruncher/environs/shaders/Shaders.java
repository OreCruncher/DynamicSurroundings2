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

package org.orecruncher.environs.shaders;

import com.mojang.blaze3d.platform.GLX;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Environs;
import net.minecraft.util.ResourceLocation;
import org.orecruncher.lib.shaders.ShaderProgram;

@OnlyIn(Dist.CLIENT)
public final class Shaders {

	public static final ShaderProgram AURORA;

	static {

		AURORA = register(
				"Aurora",
				new ResourceLocation(Environs.MOD_ID, "shaders/aurora.vert"),
				new ResourceLocation(Environs.MOD_ID, "shaders/aurora.frag"));
	}

	public static boolean areShadersSupported() {
		return true;

		// TODO: Detect shader support
		//return GLX.isNextGen();
	}

	private static ShaderProgram register(final String name, final ResourceLocation vertex,
			final ResourceLocation fragment) {
		try {
			return ShaderProgram.createProgram(name, vertex, fragment);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
}

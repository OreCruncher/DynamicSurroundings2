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
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.shader.IShaderManager;
import net.minecraft.client.shader.ShaderLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.Lib;

import javax.annotation.Nonnull;
import java.util.Collection;

@OnlyIn(Dist.CLIENT)
final class ShaderProgram implements IShaderManager {

    private final String name;
    private final int program;
    private final ShaderLoader vert;
    private final ShaderLoader frag;
    private final Object2IntOpenHashMap<String> uniforms = new Object2IntOpenHashMap<>();

    ShaderProgram(@Nonnull final String name, int program, @Nonnull final ShaderLoader vert, @Nonnull final ShaderLoader frag) {
        this.name = name;
        this.program = program;
        this.vert = vert;
        this.frag = frag;

        this.uniforms.defaultReturnValue(-1);
    }

    @Override
    public int getProgram() {
        return program;
    }

    void setUniforms(@Nonnull final Collection<String> uniforms) {
        for (final String u : uniforms) {
            final int id = GlStateManager.getUniformLocation(this.program, u);
            if (id < 0)
                Lib.LOGGER.warn("Cannot locate uniform '%s' for shader '%s'", u, this.name);
            this.uniforms.put(u, id);
        }
    }

    int getUniform(@Nonnull final String uniform) {
        return this.uniforms.getInt(uniform);
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

    @Override
    public String toString() {
        return String.format("%s [%d]", this.name, this.program);
    }
}

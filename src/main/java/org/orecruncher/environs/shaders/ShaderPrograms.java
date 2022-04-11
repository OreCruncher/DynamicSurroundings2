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

package org.orecruncher.environs.shaders;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import org.orecruncher.lib.shaders.IShaderResourceProvider;
import org.orecruncher.lib.shaders.ShaderManager;

import javax.annotation.Nonnull;
import java.util.Collection;

public enum ShaderPrograms implements IShaderResourceProvider {

    AURORA(
            "environs:shaders/aurora.vert",
            "environs:shaders/aurora.frag",
            ImmutableList.of(
                    "time",
                    "resolution",
                    "topColor",
                    "middleColor",
                    "bottomColor",
                    "alpha"
            ));

    private final ResourceLocation vertex;
    private final ResourceLocation fragment;
    private final Collection<String> uniforms;

    public static final ShaderManager<ShaderPrograms> MANAGER = new ShaderManager<>(ShaderPrograms.class);

    ShaderPrograms(@Nonnull final String vert, @Nonnull final String frag, @Nonnull final Collection<String> uniforms) {
        this.vertex = new ResourceLocation(vert);
        this.fragment = new ResourceLocation(frag);
        this.uniforms = uniforms;
    }

    @Nonnull
    public ResourceLocation getVertex() {
        return this.vertex;
    }

    @Nonnull
    public ResourceLocation getFragment() {
        return this.fragment;
    }

    @Nonnull
    public String getShaderName() {
        return this.name();
    }

    @Nonnull
    public Collection<String> getUniforms() {
        return this.uniforms;
    }
}

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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL20;
import org.orecruncher.lib.gui.Color;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public final class ShaderCallContext {

    private final ShaderProgram program;

    ShaderCallContext(@Nonnull final ShaderProgram program) {
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
        final float[] params = new float[]{color.red(), color.green(), color.blue(), alpha};
        GL20.glUniform4fv(getUniform(uniform), params);
    }

    public void set(@Nonnull final String uniform, @Nonnull final int... values) {
        GL20.glUniform1iv(getUniform(uniform), values);
    }

    private int getUniform(@Nonnull final String name) {
        return this.program.getUniform(name);
    }
}

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

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.orecruncher.environs.Environs;

public class AuroraRenderType extends RenderType {
    public AuroraRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    private static final TransparencyState AURORA_TRANSPARENCY = new TransparencyState(
            "aurora_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            },
            RenderSystem::disableBlend);

    private static final TargetState TARGET = field_239238_U_;

    public static ResourceLocation TEXTURE = new ResourceLocation(Environs.MOD_ID,"textures/misc/aurora_band.png");

    public static final RenderType QUAD = makeType(
            "aurora_render_type",
            DefaultVertexFormats.POSITION_TEX,
            GL11.GL_QUADS,
            64,
            RenderType.State.getBuilder()
                    .texture(new TextureState(TEXTURE, false, false))
                    .transparency(AURORA_TRANSPARENCY)
                    .target(TARGET)
                    .fog(FOG)
                    .shadeModel(RenderState.SHADE_ENABLED)
                    .alpha(DEFAULT_ALPHA)
                    .depthTest(DEPTH_LEQUAL)
                    .cull(CULL_DISABLED)
                    .writeMask(RenderState.COLOR_DEPTH_WRITE)
                    .build(false));
}

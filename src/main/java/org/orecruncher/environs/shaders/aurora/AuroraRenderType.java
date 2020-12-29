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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused")
public class AuroraRenderType extends RenderType {
    public AuroraRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    protected static final RenderState.TransparencyState AURORA_TRANSPARENCY = new RenderState.TransparencyState("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    private static final LayerState PROJECTION_LAYERING = field_239235_M_;
    private static final LayerState VIEW_OFFSET_Z_LAYERING = field_239235_M_;

    private static final TargetState ITEM_ENTITY_TARGET = field_241712_U_;
    private static final TargetState CLOUDS_TARGET = field_239239_V_;
    private static final TargetState WEATHER_TARGET = field_239238_U_;

    public static RenderType RENDER_TYPE_QUAD =RenderType.makeType(
            "aurora_render_type",
            DefaultVertexFormats.POSITION_TEX,
            GL11.GL_QUADS,
            256,
            RenderType.State.getBuilder()
                    .layer(PROJECTION_LAYERING)
                    .transparency(AURORA_TRANSPARENCY) // TRANSLUCENT_TRANSPARENCY
                    .texture(new TextureState(new ResourceLocation("environs:textures/particles/none.png"), false, false))
                    //.depthTest(DEPTH_LEQUAL)
                    .depthTest(DEPTH_ALWAYS)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .writeMask(DEPTH_WRITE)
                    .build(false));

    public static RenderType RENDER_TYPE = RenderType.makeType(
            "aurora_render_type",
            DefaultVertexFormats.POSITION_TEX,
            GL11.GL_TRIANGLE_STRIP,
            256,
            RenderType.State.getBuilder()
                    .layer(PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY) // TRANSLUCENT_TRANSPARENCY
                    .texture(new TextureState(new ResourceLocation("environs:textures/particles/none.png"), false, false))
                    //.depthTest(DEPTH_LEQUAL)
                    .depthTest(DEPTH_ALWAYS)
                    //.cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .writeMask(DEPTH_WRITE)
                    .build(false));
}

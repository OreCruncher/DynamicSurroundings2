/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

package org.orecruncher.lib.opengl;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@OnlyIn(Dist.CLIENT)
public final class OpenGlState {

    private final boolean enableBlend;
    private final int blendSource;
    private final int blendDest;

    private final boolean enableAlphaTest;
    private final int alphaTestFunc;
    private final float alphaTestRef;

    private final boolean depthTest;
    private final int depthFunc;

    private final boolean lighting;
    private final boolean depthMask;
    private final boolean rescaleNormal;
    private final boolean texture2D;

    private static int getInteger(final int parm) {
        return GL11.glGetInteger(parm);
    }

    private static float getFloat(final int parm) {
        return GL11.glGetFloat(parm);
    }

    private static boolean isSet(final int parm) {
        return getInteger(parm) == GL11.GL_TRUE;
    }

    private OpenGlState() {
        this.enableBlend = isSet(GL11.GL_BLEND);
        this.blendSource = getInteger(GL11.GL_BLEND_SRC);
        this.blendDest = getInteger(GL11.GL_BLEND_DST);
        //this.blendEquation = getInteger(GL14.GL_BLEND_EQUATION);

        this.enableAlphaTest = isSet(GL11.GL_ALPHA_TEST);
        this.alphaTestFunc = getInteger(GL11.GL_ALPHA_TEST_FUNC);
        this.alphaTestRef = getFloat(GL11.GL_ALPHA_TEST_REF);

        this.depthTest = isSet(GL11.GL_DEPTH_TEST);
        this.depthFunc = getInteger(GL11.GL_DEPTH_FUNC);

        this.lighting = isSet(GL11.GL_LIGHTING);
        this.depthMask = isSet(GL11.GL_DEPTH_WRITEMASK);
        this.rescaleNormal = isSet(GL12.GL_RESCALE_NORMAL);
        this.texture2D = isSet(GL11.GL_TEXTURE_2D);

        GlStateManager.pushMatrix();
    }

    public static OpenGlState push() {
        return new OpenGlState();
    }

    public static void pop(final OpenGlState state) {
        GlStateManager.popMatrix();

        if (state.enableBlend)
            GlStateManager.enableBlend();
        else
            GlStateManager.disableBlend();
        GlStateManager.blendFunc(state.blendSource, state.blendDest);
        //GlStateManager.blendEquation(state.blendEquation);

        if (state.enableAlphaTest)
            GlStateManager.enableAlphaTest();
        else
            GlStateManager.disableAlphaTest();
        GlStateManager.alphaFunc(state.alphaTestFunc, state.alphaTestRef);

        if (state.depthTest)
            GlStateManager.enableDepthTest();
        else
            GlStateManager.disableDepthTest();
        GlStateManager.depthFunc(state.depthFunc);

        if (state.lighting)
            GlStateManager.enableLighting();
        else
            GlStateManager.disableLighting();

        if (state.rescaleNormal)
            GlStateManager.enableRescaleNormal();
        else
            GlStateManager.disableRescaleNormal();

        if (state.texture2D)
            GlStateManager.enableTexture();
        else
            GlStateManager.disableTexture();

        GlStateManager.depthMask(state.depthMask);
    }

}
/*
 * Dynamic Surroundings
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
package org.orecruncher.lib.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SettingSubMenu extends SettingsRowList.Entry {

    private final Button buttonProto;
    private final Runnable handler;

    public SettingSubMenu(@Nonnull final ITextComponent label, @Nonnull final Runnable handler) {
        super(label);

        this.handler = handler;

        this.buttonProto = new Button(
                0,
                0,
                SettingsParameters.BOTTOM_BUTTON_WIDTH,
                SettingsParameters.BUTTON_HEIGHT,
                this.label,
                (button) -> {
                    this.traverse();
                });
    }

    protected void traverse() {
        this.handler.run();
    }

    @Override
    public void render(@Nonnull final MatrixStack matrixStack, int id, int x, int y, int width, int height, int totalWidth, int totalHeight, boolean isMouseOver, float partialTicks) {
        this.buttonProto.x = x;
        this.buttonProto.y = y;
    }
}

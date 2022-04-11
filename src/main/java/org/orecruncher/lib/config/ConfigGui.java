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

package org.orecruncher.lib.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public class ConfigGui {

    public static void registerConfigGui(@Nonnull final BiFunction<Minecraft, Screen, Screen> factory) {
        final ModLoadingContext context = ModLoadingContext.get();
        context.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY,
                () -> factory);
    }

    public static class InstallClothGuiFactory implements BiFunction<Minecraft, Screen, Screen> {

        // Resources for displaying info about getting ClothAPI
        private static final ITextComponent title = new TranslationTextComponent("dsurround.dialog.missingcloth.title");
        private static final ITextComponent description = new TranslationTextComponent("dsurround.dialog.missingcloth.description");

        @Override
        public Screen apply(@Nonnull final Minecraft minecraft, @Nonnull final Screen screen) {
            return new InstallClothGui(screen, title, description);
        }
    }

    // Swipe the disconnected from server dialog.  All this to replace the button resource...maybe I will fancy it
    // up with dancing creepers or something.
    private static class InstallClothGui extends Screen {
        private final ITextComponent message;
        private IBidiRenderer field_243289_b = IBidiRenderer.field_243257_a;
        private final Screen nextScreen;
        private int textHeight;

        public InstallClothGui(Screen p_i242056_1_, ITextComponent p_i242056_2_, ITextComponent p_i242056_3_) {
            super(p_i242056_2_);
            this.nextScreen = p_i242056_1_;
            this.message = p_i242056_3_;
        }

        public boolean shouldCloseOnEsc() {
            return false;
        }

        protected void init() {
            this.field_243289_b = IBidiRenderer.func_243258_a(this.font, this.message, this.width - 50);
            this.textHeight = this.field_243289_b.func_241862_a() * 9;
            this.addButton(new Button(this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + 9, this.height - 30), 200, 20, DialogTexts.GUI_DONE, (p_213033_1_) -> {
                this.minecraft.displayGuiScreen(this.nextScreen);
            }));
        }

        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(matrixStack);
            drawCenteredString(matrixStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
            this.field_243289_b.func_241863_a(matrixStack, this.width / 2, this.height / 2 - this.textHeight / 2);
            super.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}

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

package org.orecruncher.sndctrl.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.widget.Slider;
import org.orecruncher.lib.gui.ColorPalette;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class QuickVolumeScreen extends Screen implements Slider.ISlider {

    private static final int SLIDER_WIDTH = 160;
    private static final int SLIDER_HEIGHT = 20;
    private static final int SLIDER_SPACING = 5;
    private static final Button.IPressable NULL_PRESSABLE = (b) -> { };
    private static final ITextComponent SUFFIX = new StringTextComponent("%");
    private static final ITextComponent FOOTER = new TranslationTextComponent("sndctrl.text.quickvolumemenu.footer");
    private static final ITextComponent TITLE = new TranslationTextComponent("sndctrl.text.quickvolumemenu.title");

    private final List<ISoundCategory> categories = new ArrayList<>();
    private final List<Slider> sliders = new ArrayList<>();

    private int footerY;

    protected QuickVolumeScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {

        // Clear out the old cached data
        this.categories.clear();
        this.sliders.clear();

        // Collect the widgets into a list
        this.categories.addAll(Category.getCategoriesForMenu());

        // Get base positioning information for display.  This should be roughly center of the screen.
        final int leftSide = (this.width - SLIDER_WIDTH) / 2;

        // Calculate the top of where the first slider would be
        final int totalHeight = this.categories.size() * (SLIDER_HEIGHT + SLIDER_SPACING);
        int top = (this.height - totalHeight) / 2;

        // Build slider widgets for them.
        for (final ISoundCategory category : this.categories) {
            final Slider slider = new Slider(
                    leftSide,
                    top,
                    SLIDER_WIDTH,
                    SLIDER_HEIGHT,
                    getSliderLabel(category.getTextComponent()),
                    SUFFIX,
                    0,
                    100,
                    (int)(category.getVolumeScale() * 100),
                    false,
                    true,
                    NULL_PRESSABLE,
                    this
            );

            slider.y = top;
            top += SLIDER_HEIGHT + SLIDER_SPACING;
            addButton(slider);
            this.sliders.add(slider);
        }

        this.footerY = top;
    }

    protected ITextComponent getSliderLabel(@Nonnull final ITextComponent text) {
        return ((IFormattableTextComponent)text).append(new StringTextComponent(": "));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // See if the mouse is over a slider and do the adjust thing
        for (final Slider slider : this.sliders) {
            if (slider.isMouseOver(mouseX, mouseY)) {
                slider.sliderValue += 0.05F * (delta > 0 ? 1 : -1);
                slider.updateSlider();
                break;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        for (final Slider slider : this.sliders)
            slider.dragging = false;
        return false;
    }

    @Override
    public void onChangeSliderValue(@Nonnull final Slider slider) {
        // Need to identify the ISoundCategory associated with the slider.
        int idx = 0;
        for (; idx < this.sliders.size(); idx++) {
            if (this.sliders.get(idx) == slider)
                break;
        }

        // Safety just in case
        if (idx >= this.sliders.size())
            return;

        // Set the value
        final ISoundCategory category = this.categories.get(idx);
        category.setVolumeScale(slider.getValueInt() / 100F);
    }

    @Override
    public void render(@Nonnull MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);

        // Render our text footer
        drawCenteredString(stack, this.font, FOOTER, this.width / 2, this.footerY, ColorPalette.WHITE.rgb());
    }

}

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
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class IndividualSoundControlScreen extends Screen {

    private static final int TOP_OFFSET = 10;
    private static final int BOTTOM_OFFSET = 15;
    private static final int HEADER_HEIGHT = 35;
    private static final int FOOTER_HEIGHT = 50;

    private static final int SEARCH_BAR_WIDTH = 200;
    private static final int SEARCH_BAR_HEIGHT = 20;

    private static final int SELECTION_HEIGHT_OFFSET = 5;
    private static final int SELECTION_WIDTH = 600;
    private static final int SELECTION_HEIGHT = 20;

    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 10;
    private static final int CONTROL_WIDTH = BUTTON_WIDTH * 2 + BUTTON_SPACING;

    private static final int TOOLTIP_Y_OFFSET = 30;

    private static final ITextComponent SAVE = new TranslationTextComponent("gui.done");
    private static final ITextComponent CANCEL = new TranslationTextComponent("gui.cancel");

    protected final Screen parent;
    protected final boolean enablePlay;
    protected TextFieldWidget searchField;
    protected IndividualSoundControlList soundConfigList;
    protected Button save;
    protected Button cancel;

    protected IndividualSoundControlScreen(@Nullable final Screen parent, final boolean enablePlay) {
        super(new TranslationTextComponent("sndctrl.text.soundconfig.title"));
        this.parent = parent;
        this.enablePlay = enablePlay;
    }

    @Override
    protected void init() {

        GameUtils.getMC().keyboardListener.enableRepeatEvents(true);

        // Setup search bar
        final int searchBarLeftMargin = (this.width - SEARCH_BAR_WIDTH) / 2;
        final int searchBarY = TOP_OFFSET + HEADER_HEIGHT - SEARCH_BAR_HEIGHT;
        this.searchField = new TextFieldWidget(
                this.font,
                searchBarLeftMargin,
                searchBarY,
                SEARCH_BAR_WIDTH,
                SEARCH_BAR_HEIGHT,
                this.searchField,   // Copy existing data over
                StringTextComponent.EMPTY);

        this.searchField.setResponder((filter) -> this.soundConfigList.setSearchFilter(() -> filter, false));

        this.children.add(this.searchField);

        // Setup the list control
        final int topY = TOP_OFFSET + HEADER_HEIGHT + SELECTION_HEIGHT_OFFSET;
        final int bottomY = this.height - BOTTOM_OFFSET - FOOTER_HEIGHT - SELECTION_HEIGHT_OFFSET;
        this.soundConfigList = new IndividualSoundControlList(
                this,
                GameUtils.getMC(),
                this.width,
                this.height,
                topY,
                bottomY,
                SELECTION_WIDTH,
                SELECTION_HEIGHT,
                this.enablePlay,
                () -> this.searchField.getText(),
                this.soundConfigList);

        this.children.add(this.soundConfigList);

        // Set the control buttons at the bottom
        final int controlMargin = (this.width - CONTROL_WIDTH) / 2;
        final int controlHeight = this.height - BOTTOM_OFFSET - BUTTON_HEIGHT;
        this.save = new Button(
                controlMargin,
                controlHeight,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                SAVE,
                this::save);
        this.addButton(this.save);

        this.cancel = new Button(
                controlMargin + BUTTON_WIDTH + BUTTON_SPACING,
                controlHeight,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                CANCEL,
                this::cancel);
        this.addButton(this.cancel);

        this.setFocusedDefault(this.searchField);
    }

    public void tick() {
        this.searchField.tick();
        this.soundConfigList.tick();
    }

    public boolean isPauseScreen() {
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers) || this.searchField.keyPressed(keyCode, scanCode, modifiers);
    }

    public void closeScreen() {
        GameUtils.getMC().displayGuiScreen(this.parent);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return this.searchField.charTyped(codePoint, modifiers);
    }

    public void render(@Nonnull final MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(0);
        //this.renderBackground(matrixStack);
        this.soundConfigList.render(matrixStack, mouseX, mouseY, partialTicks);
        this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, TOP_OFFSET, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        final IndividualSoundControlListEntry entry = this.soundConfigList.getEntryAt(mouseX, mouseY);
        if (entry != null) {
            final List<ITextComponent> toolTip = entry.getToolTip();
            this.renderWrappedToolTip(matrixStack, toolTip, mouseX, mouseY + TOOLTIP_Y_OFFSET, GameUtils.getMC().fontRenderer);
        }
    }

    // Handlers

    protected void save(@Nonnull final Button button) {
        // Gather the changes and push to underlying routine for parsing and packaging
        this.soundConfigList.saveChanges();
        this.onClose();
        this.closeScreen();
    }

    protected void cancel(@Nonnull final Button button) {
        // Just discard - no processing
        this.onClose();
        this.closeScreen();
    }
}

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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SettingsRowList extends AbstractOptionList<SettingsRowList.Entry> {

    public SettingsRowList(Minecraft p_i51139_1_, int p_i51139_2_, int p_i51139_3_, int p_i51139_4_, int p_i51139_5_, int p_i51139_6_) {
        super(p_i51139_1_, p_i51139_2_, p_i51139_3_, p_i51139_4_, p_i51139_5_, p_i51139_6_);
    }

    public void addSetting(SettingsRowList.Entry entry) {
        this.addEntry(entry);
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends AbstractOptionList.Entry<SettingsRowList.Entry> {

        protected final ITextComponent label;
        protected final ITextComponent toolTip;
        protected boolean isEnabled;
        protected boolean isHidden;

        public Entry(@Nonnull final ITextComponent label) {
            this.label = label;
            this.toolTip = new TranslationTextComponent(label.getUnformattedComponentText() + ".tooltip");
            this.isEnabled = true;
            this.isHidden = false;
        }

        public boolean isEnabled() {
            return this.isEnabled;
        }

        public void setEnabled(final boolean flag) {
            this.isEnabled = flag;
        }

        public boolean isHidden() {
            return this.isHidden;
        }

        public void setHidden(final boolean flag) {
            this.isHidden = flag;
        }

        @Override
        @Nonnull
        public List<? extends IGuiEventListener> getEventListeners() {
            return new ArrayList<>();
        }

    }

    public abstract static class SettingsEntry extends Entry {

        protected final Button undo;

        public SettingsEntry(@Nonnull final ITextComponent label) {
            super(label);

            this.undo = new Button(
                    0,
                    0,
                    SettingsParameters.BOTTOM_BUTTON_WIDTH,
                    SettingsParameters.BUTTON_HEIGHT,
                    new StringTextComponent("Undo"),
                    (button) -> {
                        this.undo();
                    });
        }

        public void undo() {

        }

        public abstract void save();

        @Override
        @Nonnull
        public List<? extends IGuiEventListener> getEventListeners() {
            return new ArrayList<>();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.undo.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.undo.mouseClicked(mouseX, mouseY, button);
        }

    }
}

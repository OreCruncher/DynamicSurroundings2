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
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.widget.Slider;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.gui.ColorPalette;
import org.orecruncher.lib.gui.GuiHelpers;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;
import org.orecruncher.sndctrl.api.sound.SoundBuilder;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.audio.SoundMetadata;
import org.orecruncher.sndctrl.library.IndividualSoundConfig;
import org.orecruncher.sndctrl.library.SoundLibrary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class IndividualSoundControlListEntry extends AbstractOptionList.Entry<IndividualSoundControlListEntry> implements Slider.ISlider, AutoCloseable {

    private static final int SLIDER_WIDTH = 100;
    private static final int BUTTON_WIDTH = 60;
    private static final int TOOLTIP_WIDTH = 300;
    private static final Button.IPressable NULL_PRESSABLE = (b) -> {};
    private static final ITextComponent CULL_ON = new TranslationTextComponent("sndctrl.text.soundconfig.cull");
    private static final ITextComponent CULL_OFF = new TranslationTextComponent("sndctrl.text.soundconfig.nocull");
    private static final ITextComponent BLOCK_ON = new TranslationTextComponent("sndctrl.text.soundconfig.block");
    private static final ITextComponent BLOCK_OFF = new TranslationTextComponent("sndctrl.text.soundconfig.noblock");
    private static final ITextComponent PLAY = new TranslationTextComponent("sndctrl.text.soundconfig.play");
    private static final ITextComponent STOP = new TranslationTextComponent("sndctrl.text.soundconfig.stop");
    private static final ITextComponent VANILLA_CREDIT = new TranslationTextComponent("sndctrl.text.tooltip.vanilla");
    private static final ITextComponent SLIDER_SUFFIX = new StringTextComponent("%");

    private static final TextFormatting[] CODING = new TextFormatting[] {TextFormatting.ITALIC, TextFormatting.AQUA};
    private static final Collection<ITextComponent> VOLUME_HELP = GuiHelpers.getTrimmedText("sndctrl.text.soundconfig.volume.help", TOOLTIP_WIDTH, CODING);
    private static final Collection<ITextComponent> PLAY_HELP = GuiHelpers.getTrimmedText("sndctrl.text.soundconfig.play.help", TOOLTIP_WIDTH, CODING);
    private static final Collection<ITextComponent> CULL_HELP = GuiHelpers.getTrimmedText("sndctrl.text.soundconfig.cull.help", TOOLTIP_WIDTH, CODING);
    private static final Collection<ITextComponent> BLOCK_HELP = GuiHelpers.getTrimmedText("sndctrl.text.soundconfig.block.help", TOOLTIP_WIDTH, CODING);

    private static final int CONTROL_SPACING = 3;

    private final IndividualSoundConfig config;
    private final Slider volume;
    private final Button blockButton;
    private final Button cullButton;
    private final Button playButton;

    private final List<Widget> children = new ArrayList<>();

    private List<ITextComponent> defaultTooltip;

    private ISoundInstance soundPlay;

    public IndividualSoundControlListEntry(@Nonnull final IndividualSoundConfig data, final boolean enablePlay) {
        this.config = data;

        this.volume = new Slider(
                0,
                0,
                SLIDER_WIDTH,
                0,
                StringTextComponent.EMPTY,
                SLIDER_SUFFIX,
                0,
                400,
                this.config.getVolumeScaleInt(),
                false,
                true,
                NULL_PRESSABLE,
                this);
        this.children.add(this.volume);

        this.blockButton = new Button(
                0,
                0,
                BUTTON_WIDTH,
                0,
                this.config.isBlocked() ? BLOCK_ON : BLOCK_OFF,
                this::toggleBlock);
        this.children.add(this.blockButton);

        this.cullButton = new Button(
                0,
                0,
                BUTTON_WIDTH,
                0,
                this.config.isCulled() ? CULL_ON : CULL_OFF,
                this::toggleCull);
        this.children.add(this.cullButton);

        this.playButton = new Button(
                0,
                0,
                BUTTON_WIDTH,
                0,
                PLAY,
                this::play) {

            @Override
            public void playDownSound(@Nonnull final SoundHandler ignore) {
                // Suppress the button click to avoid conflicting with the sound play
            }
        };

        this.playButton.active = enablePlay;
        this.children.add(this.playButton);
    }

    @Override
    @Nonnull
    public List<? extends IGuiEventListener> getEventListeners() {
        return this.children;
    }

    @Override
    public void render(@Nonnull final MatrixStack matrixStack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean mouseOver, float partialTick_) {
        final FontRenderer font = GameUtils.getMC().fontRenderer;
        final float labelY = rowTop + (rowHeight - font.FONT_HEIGHT) / 2F;
        final String text = this.config.getLocation().toString();
        font.drawString(matrixStack, text, (float) rowLeft, labelY, ColorPalette.WHITE.rgb());

        // Need to position the other controls appropriately
        int rightMargin = rowLeft + rowWidth;
        this.volume.x = rightMargin - this.volume.getWidth();
        this.volume.y = rowTop;
        this.volume.setHeight(rowHeight);
        rightMargin -= this.volume.getWidth() + CONTROL_SPACING;

        this.playButton.x = rightMargin - this.playButton.getWidth();
        this.playButton.y = rowTop;
        this.playButton.setHeight(rowHeight);
        rightMargin -= this.playButton.getWidth() + CONTROL_SPACING;

        this.blockButton.x = rightMargin - this.blockButton.getWidth();
        this.blockButton.y = rowTop;
        this.blockButton.setHeight(rowHeight);
        rightMargin -= this.blockButton.getWidth() + CONTROL_SPACING;

        this.cullButton.x = rightMargin - this.cullButton.getWidth();
        this.cullButton.setHeight(rowHeight);
        this.cullButton.y = rowTop;

        for (final Widget w : this.children)
            w.render(matrixStack, mouseX, mouseY, partialTick_);
    }

    protected void toggleBlock(@Nonnull final Button button) {
        this.config.setIsBlocked(!this.config.isBlocked());
        button.setMessage(this.config.isBlocked() ? BLOCK_ON : BLOCK_OFF);
    }

    protected void toggleCull(@Nonnull final Button button) {
        this.config.setIsCulled(!this.config.isCulled());
        button.setMessage(this.config.isCulled() ? CULL_ON : CULL_OFF);
    }

    @Override
    public void onChangeSliderValue(@Nonnull final Slider slider) {
        this.config.setVolumeScaleInt(slider.getValueInt());
    }

    protected void play(@Nonnull final Button button) {
        if (this.soundPlay == null) {
            final Optional<SoundEvent> event = SoundLibrary.getSound(this.config.getLocation());
            event.ifPresent(se -> {
                this.soundPlay = SoundBuilder.builder(se, Category.CONFIG)
                        .setGlobal(true)
                        .setVolume(this.config.getVolumeScale())
                        .build();
                AudioEngine.play(this.soundPlay);
                this.playButton.setMessage(STOP);
            });
        } else {
            AudioEngine.stop(this.soundPlay);
            this.soundPlay = null;
            this.playButton.setMessage(PLAY);
        }
    }

    @Override
    public void close() {
        if (this.soundPlay != null) {
            AudioEngine.stop(this.soundPlay);
            this.soundPlay = null;
        }
    }

    public void tick() {
        if (this.soundPlay != null) {
            if (this.soundPlay.getState().isTerminal()) {
                this.soundPlay = null;
                this.playButton.setMessage(PLAY);
            }
        }
    }

    @Nonnull
    protected List<ITextComponent> getToolTip(final int mouseX, final int mouseY) {
        if (this.defaultTooltip == null) {
            this.defaultTooltip = new ArrayList<>();
            final ResourceLocation loc = this.config.getLocation();

            final String modName = ForgeUtils.getModDisplayName(loc.getNamespace());
            this.defaultTooltip.add(new StringTextComponent(TextFormatting.GOLD + modName));

            this.defaultTooltip.add(new StringTextComponent(TextFormatting.GRAY + loc.toString()));

            final SoundMetadata meta = SoundLibrary.getSoundMetadata(loc);
            final ITextComponent title = meta.getTitle();
            if (title != StringTextComponent.EMPTY)
                this.defaultTooltip.add(title);
            final ISoundCategory category = meta.getCategory();
            if (category != Category.NEUTRAL) {
                this.defaultTooltip.add(new TranslationTextComponent("sndctrl.text.tooltip.category").append(category.getTextComponent()));
            }

            if (modName.equals("Minecraft"))
                this.defaultTooltip.add(VANILLA_CREDIT);
            else
                this.defaultTooltip.addAll(meta.getCredits());
        }

        final List<ITextComponent> result = new ArrayList<>(this.defaultTooltip);

        if (this.volume.isMouseOver(mouseX, mouseY)) {
            result.addAll(VOLUME_HELP);
        } else if (this.blockButton.isMouseOver(mouseX, mouseY)) {
            result.addAll(BLOCK_HELP);
        } else if (this.cullButton.isMouseOver(mouseX, mouseY)) {
            result.addAll(CULL_HELP);
        } else if (this.playButton.isMouseOver(mouseX, mouseY)) {
            result.addAll(PLAY_HELP);
        }

        return result;
    }

    /**
     * Retrieves the updated data from the entry
     * @return Updated IndividualSoundControl data
     */
    @Nonnull
    public IndividualSoundConfig getData() {
        return this.config;
    }

}

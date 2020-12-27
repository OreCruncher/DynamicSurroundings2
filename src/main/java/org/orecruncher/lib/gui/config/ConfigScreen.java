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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public final class ConfigScreen extends Screen {

    public static int depthCounter = 0;

    /**
     * Activation context for this menu instance.
     */
    private final Context context;

    /**
     * The object for registering options on this screen and controlling how
     * they are presented
     */
    protected SettingsRowList settingsRowList;

    /**
     * Constructs a new {@link ConfigScreen} instance.
     *
     * @param parentScreen the screen that will become this screen's parent
     * @param title the title to list at the top of the window
     * @param someOptionList the root level options for the configuration
     */
    public ConfigScreen(@Nonnull final Screen parentScreen, @Nonnull final String title, @Nonnull final List<?> someOptionList) {
        super(new TranslationTextComponent(title));
        this.context = new Context(parentScreen, new TranslationTextComponent(title), someOptionList, true);
    }

    /**
     * Constructor for a child config option menu
     *
     * @param ctx Activation context for the menu instance
     */
    public ConfigScreen(@Nonnull final Context ctx) {
        super(ctx.title);
        this.context = ctx;
    }

    /**
     * Initializes this GUI with options list and buttons.
     */
    @Override
    protected void init() {
        this.settingsRowList = new SettingsRowList(
                getMinecraft(), this.width, this.height,
                SettingsParameters.OPTIONS_LIST_TOP_HEIGHT,
                this.height - SettingsParameters.OPTIONS_LIST_BOTTOM_OFFSET,
                SettingsParameters.OPTIONS_LIST_ITEM_HEIGHT);

        intializeMenuOptions(this.settingsRowList);

        this.children.add(this.settingsRowList);

        // Add the Done/Cancel buttons
        this.addButton(new Button(
                (this.width - SettingsParameters.BUTTONS_INTERVAL) / 2 - SettingsParameters.BOTTOM_BUTTON_WIDTH,
                this.height - SettingsParameters.BOTTOM_BUTTON_HEIGHT_OFFSET,
                SettingsParameters.BOTTOM_BUTTON_WIDTH, SettingsParameters.BUTTON_HEIGHT,
                SettingsParameters.CANCEL_BUTTON_TEXT,
                button -> this.onCancel())
        );
        this.addButton(new Button(
                (this.width + SettingsParameters.BUTTONS_INTERVAL) / 2,
                this.height - SettingsParameters.BOTTOM_BUTTON_HEIGHT_OFFSET,
                SettingsParameters.BOTTOM_BUTTON_WIDTH, SettingsParameters.BUTTON_HEIGHT,
                SettingsParameters.DONE_BUTTON_TEXT,
                button -> this.onDone())
        );
    }

    /**
     * Initialize the content of the options that show up for manipulation.
     */
    protected void intializeMenuOptions(@Nonnull final SettingsRowList options) {
        // Need to traverse the incoming menu options and create the necessary options
        /*
        options.addEntry(new ChildMenuSetting("This is a test", (settings) -> {
            depthCounter++;
            activateChildMenu(new TranslationTextComponent(String.valueOf(depthCounter)), ImmutableList.of());
        }));

        options.addEntry(new BooleanOption("Boolean", (unused) -> true, (unused, newVal) -> {
        }));

         */
    }

    /**
     * Activates a chidl menu with the given subtitle and options to manage
     * @param subMenuTitle Subtitle to list on the menu screen
     * @param options Options to manage
     */
    protected void activateChildMenu(@Nonnull final ITextComponent subMenuTitle, @Nonnull final List<?> options) {
        final String subMenuText = this.context.title.getString() + " > " + subMenuTitle.getString();
        final Context newContext = new Context(this, new TranslationTextComponent(subMenuText), options, false);
        final ConfigScreen child = createChildScreen(newContext);
        activateScreen(child);
    }

    /**
     * Create a child menu screen.  Override to do any special handling, such as activating any custom menu
     * implementation.
     * @param ctx Menu context for the new child screen
     * @return Instance of ChildScreen to enable configuration of the provided context
     */
    protected ConfigScreen createChildScreen(@Nonnull final Context ctx) {
        return new ConfigScreen(ctx);
    }

    protected void activateScreen(@Nonnull final Screen screen) {
        getMinecraft().displayGuiScreen(screen);
    }

    /**
     * Draws this GUI on the screen.
     *
     * @param mouseX horizontal location of the mouse
     * @param mouseY vertical location of the mouse
     * @param partialTicks number of partial ticks
     */
    @Override
    public void render(@Nonnull final MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.settingsRowList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title.getString(),
                this.width / 2, SettingsParameters.TITLE_HEIGHT, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void onDone() {
        // Save state here.  If this is the parent we need to commit the changes into the active
        // config state.
        depthCounter--;
        changeToParent();
    }

    public void onCancel() {
        // Don't save state - clean up if needed
        depthCounter--;
        changeToParent();
    }

    protected void changeToParent() {
        getMinecraft().displayGuiScreen(this.context.parentScreen);
    }

    /**
     * Closes this screen.  Clean up anything GUI related.  Config should be saved or discarded at the time
     * this is called.
     */
    @Override
    public void onClose() {
        // Called when the menu is about to disappear.  Can happen when traversing to a child menu or back.
    }

    public static class Context {

        public final Screen parentScreen;
        public final ITextComponent title;
        public final boolean isRoot;
        public final List<?> options;

        Context(@Nullable final Screen parent, @Nonnull final ITextComponent title, @Nonnull final List<?> options, final boolean isRoot) {
            this.parentScreen = parent;
            this.title = title;
            this.isRoot = isRoot;
            this.options = options;
        }
    }
}
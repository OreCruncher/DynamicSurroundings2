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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.StringUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.library.IndividualSoundConfig;
import org.orecruncher.sndctrl.library.SoundLibrary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class IndividualSoundControlList extends AbstractOptionList<IndividualSoundControlListEntry> {

    private final Screen parent;
    private final boolean enablePlay;
    private final int width;
    private List<IndividualSoundConfig> source;
    private String lastSearchText = null;

    public IndividualSoundControlList(@Nonnull final Screen parent, @Nonnull final Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotWidth, int slotHeightIn, boolean enablePlay, @Nonnull final Supplier<String> filter, @Nullable final IndividualSoundControlList oldList) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);

        this.parent = parent;
        this.enablePlay = enablePlay;
        this.width = slotWidth;

        // Things like resizing will cause reconstruction and this preserves the existing state
        if (oldList != null)
            this.source = oldList.source;

        // Initialize the first pass
        this.setSearchFilter(filter, false);
    }

    public void tick() {
        this.getEventListeners().stream()
                .map(IndividualSoundControlListEntry.class::cast)
                .forEach(IndividualSoundControlListEntry::tick);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return (this.parent.width + this.getRowWidth()) / 2 + 20;
    }

    public void setSearchFilter(@Nonnull final Supplier<String> filterBy, final boolean forceReload) {
        final String filter = filterBy.get();

        if (!forceReload && this.lastSearchText != null && this.lastSearchText.equals(filter))
            return;

        this.lastSearchText = filter;

        // Clear any existing children - they are going to be repopulated
        this.clearEntries();

        // Load up source if needed
        if (this.source == null || forceReload)
            this.source = new ArrayList<>(SoundLibrary.getSortedSoundConfigurations());

        // Get the filter string.  It's a simple contains check.
        final Function<IndividualSoundConfig, Boolean> process;

        if (StringUtils.isNullOrEmpty(filter)) {
            process = (isc) -> true;
        } else {
            process = (isc) -> isc.getLocation().toString().contains(filter);
        }

        IndividualSoundControlListEntry first = null;
        for (IndividualSoundConfig cfg : this.source) {
            if (process.apply(cfg)) {
                final IndividualSoundControlListEntry entry = new IndividualSoundControlListEntry(cfg, this.enablePlay);
                if (first == null)
                    first = entry;
                this.addEntry(entry);
            }
        }

        if (first != null)
            this.ensureVisible(first);
    }

    @Nullable
    public IndividualSoundControlListEntry getEntryAt(final int mouseX, final int mouseY) {
        return this.getEntryAtPosition(mouseX, mouseY);
    }

    // Gathers all the sound configs that are different from default for handling.
    @Nonnull
    protected Collection<IndividualSoundConfig> getConfigs() {
        final List<IndividualSoundConfig> configs = new ArrayList<>();
        for (final IndividualSoundConfig cfg : this.source) {
            if (!cfg.isDefault())
                configs.add(cfg);
        }
        return configs;
    }

    public void saveChanges() {
        SoundLibrary.updateSoundConfigurations(getConfigs());
    }

}
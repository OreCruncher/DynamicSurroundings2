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

package org.orecruncher.lib.scripting.sets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.scripting.VariableSet;
import org.orecruncher.lib.seasons.Season;
import org.orecruncher.lib.seasons.SeasonType;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class SeasonVariables  extends VariableSet<ISeasonVariables> implements ISeasonVariables {

    private final LazyVariable<Season> season = new LazyVariable<>(() -> Season.getSeason(GameUtils.getWorld()));
    private final LazyVariable<String> seasonName = new LazyVariable<>(() -> season.get().getFormattedText());

    public SeasonVariables() {
        super("season");
    }

    @Nonnull
    @Override
    public ISeasonVariables getInterface() {
        return this;
    }

    @Override
    public void update() {
        this.season.reset();
        this.seasonName.reset();
    }

    @Override
    public boolean isSpring() {
        return this.season.get().getType() == SeasonType.SPRING;
    }

    @Override
    public boolean isSummer() {
        return this.season.get().getType() == SeasonType.SUMMER;
    }

    @Override
    public boolean isAutumn() {
        return this.season.get().getType() == SeasonType.AUTUMN;
    }

    @Override
    public boolean isWinter() {
        return this.season.get().getType() == SeasonType.WINTER;
    }

    @Override
    public String getSeason() {
        return this.seasonName.get();
    }
}

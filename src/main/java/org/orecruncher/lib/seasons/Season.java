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

package org.orecruncher.lib.seasons;

import net.minecraft.world.World;
import org.orecruncher.lib.Localization;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.misc.ModEnvironment;
import sereneseasons.api.season.ISeasonState;

import javax.annotation.Nonnull;

public enum Season {

    NONE(SeasonType.NONE, SeasonSubType.NONE),

    EARLY_SPRING(SeasonType.SPRING, SeasonSubType.EARLY),
    MID_SPRING(SeasonType.SPRING, SeasonSubType.MID),
    LATE_SPRING(SeasonType.SPRING, SeasonSubType.LATE),

    EARLY_SUMMER(SeasonType.SUMMER, SeasonSubType.EARLY),
    MID_SUMMER(SeasonType.SUMMER, SeasonSubType.MID),
    LATE_SUMMER(SeasonType.SUMMER, SeasonSubType.LATE),

    EARLY_AUTUMN(SeasonType.AUTUMN, SeasonSubType.EARLY),
    MID_AUTUMN(SeasonType.AUTUMN, SeasonSubType.MID),
    LATE_AUTUMN(SeasonType.AUTUMN, SeasonSubType.LATE),

    EARLY_WINTER(SeasonType.WINTER, SeasonSubType.EARLY),
    MID_WINTER(SeasonType.WINTER, SeasonSubType.MID),
    LATE_WINTER(SeasonType.WINTER, SeasonSubType.LATE);

    private static final String FORMAT_NONE = SoundControl.MOD_ID + ".season.noseason";
    private static final String FORMAT_STRING = SoundControl.MOD_ID + ".season.format";
    private static final ISeasonHelper SEASON_HELPER;

    static {
        if (ModEnvironment.SereneSeasons.isLoaded())
            SEASON_HELPER = Season::getSereneSeason;
        else
            SEASON_HELPER = world -> Season.NONE;
    }

    private final SeasonType season;
    private final SeasonSubType subType;

    Season(@Nonnull final SeasonType type, @Nonnull final SeasonSubType subType) {
        this.season = type;
        this.subType = subType;
    }

    @Nonnull
    public SeasonType getType() {
        return this.season;
    }

    @Nonnull
    public SeasonSubType getSubType() {
        return this.subType;
    }

    @Nonnull
    public static Season getSeason(@Nonnull final World world) {
        return SEASON_HELPER.getSeason(world);
    }

    @Nonnull
    private static Season getSereneSeason(@Nonnull final World world) {
        Season season = Season.NONE;
        final ISeasonState state = sereneseasons.api.season.SeasonHelper.getSeasonState(world);
        if (state != null) {
            switch (state.getSubSeason()) {
                case EARLY_SPRING:
                    season = Season.EARLY_SPRING;
                    break;
                case MID_SPRING:
                    season = Season.MID_SPRING;
                    break;
                case LATE_SPRING:
                    season = Season.LATE_SPRING;
                    break;
                case EARLY_SUMMER:
                    season = Season.EARLY_SUMMER;
                    break;
                case MID_SUMMER:
                    season = Season.MID_SUMMER;
                    break;
                case LATE_SUMMER:
                    season = Season.LATE_SUMMER;
                    break;
                case EARLY_AUTUMN:
                    season = Season.EARLY_AUTUMN;
                    break;
                case MID_AUTUMN:
                    season = Season.MID_AUTUMN;
                    break;
                case LATE_AUTUMN:
                    season = Season.LATE_AUTUMN;
                    break;
                case EARLY_WINTER:
                    season = Season.EARLY_WINTER;
                    break;
                case MID_WINTER:
                    season = Season.MID_WINTER;
                    break;
                case LATE_WINTER:
                    season = Season.LATE_WINTER;
                    break;
            }
        }
        return season;
    }

    @Nonnull
    public String getFormattedText() {
        if (this.season == SeasonType.NONE) {
            return Localization.load(FORMAT_NONE);
        }

        return Localization.format(
                FORMAT_STRING,
                this.subType.getFormattedText(),
                this.season.getFormattedText()
        );
    }

    private interface ISeasonHelper {
        Season getSeason(@Nonnull final World world);
    }
}

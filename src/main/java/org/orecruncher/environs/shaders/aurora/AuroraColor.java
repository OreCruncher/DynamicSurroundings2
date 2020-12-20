/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.shaders.aurora;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.gui.ColorPalette;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public final class AuroraColor {

    private static final List<AuroraColor> COLOR_SETS = new ArrayList<>();
    private static final float WARMER = 0.3F;
    private static final float COOLER = -0.3F;

    static {

        COLOR_SETS.add(new AuroraColor(new Color(0x0, 0xff, 0x99), new Color(0x33, 0xff, 0x00)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.BLUE, ColorPalette.GREEN));
        COLOR_SETS.add(new AuroraColor(ColorPalette.MAGENTA, ColorPalette.GREEN));
        COLOR_SETS.add(new AuroraColor(ColorPalette.INDIGO, ColorPalette.GREEN));
        COLOR_SETS.add(new AuroraColor(ColorPalette.TURQOISE, ColorPalette.LGREEN));
        COLOR_SETS.add(new AuroraColor(ColorPalette.YELLOW, ColorPalette.RED));
        COLOR_SETS.add(new AuroraColor(ColorPalette.GREEN, ColorPalette.RED));
        COLOR_SETS.add(new AuroraColor(ColorPalette.GREEN, ColorPalette.YELLOW));
        COLOR_SETS.add(new AuroraColor(ColorPalette.RED, ColorPalette.YELLOW));
        COLOR_SETS.add(new AuroraColor(ColorPalette.NAVY, ColorPalette.INDIGO));
        COLOR_SETS.add(new AuroraColor(ColorPalette.CYAN, ColorPalette.MAGENTA));
        COLOR_SETS.add(new AuroraColor(ColorPalette.AURORA_GREEN, ColorPalette.AURORA_RED, ColorPalette.AURORA_BLUE));

        // Warmer versions
        COLOR_SETS.add(new AuroraColor(ColorPalette.YELLOW.luminance(WARMER),
                ColorPalette.RED.luminance(WARMER)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.GREEN.luminance(WARMER),
                ColorPalette.RED.luminance(WARMER)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.GREEN.luminance(WARMER),
                ColorPalette.YELLOW.luminance(WARMER)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.BLUE.luminance(WARMER),
                ColorPalette.GREEN.luminance(WARMER)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.INDIGO.luminance(WARMER),
                ColorPalette.GREEN.luminance(WARMER)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.AURORA_GREEN.luminance(WARMER),
                ColorPalette.AURORA_RED.luminance(WARMER), ColorPalette.AURORA_BLUE.luminance(WARMER)));

        // Cooler versions
        COLOR_SETS.add(new AuroraColor(ColorPalette.YELLOW.luminance(COOLER),
                ColorPalette.RED.luminance(COOLER)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.GREEN.luminance(COOLER),
                ColorPalette.RED.luminance(COOLER)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.GREEN.luminance(COOLER),
                ColorPalette.YELLOW.luminance(COOLER)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.BLUE.luminance(COOLER),
                ColorPalette.GREEN.luminance(COOLER)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.INDIGO.luminance(COOLER),
                ColorPalette.GREEN.luminance(COOLER)));
        COLOR_SETS.add(new AuroraColor(ColorPalette.AURORA_GREEN.luminance(COOLER),
                ColorPalette.AURORA_RED.luminance(COOLER), ColorPalette.AURORA_BLUE.luminance(COOLER)));

    }

    /**
     * Color that forms the base of the aurora and is the brightest.
     */
    public final Color baseColor;
    /**
     * Color that forms the top of the aurora and usually fades to black.
     */
    public final Color fadeColor;
    /**
     * Mid-band color for aurora styles that use it.
     */
    public final Color middleColor;

    private AuroraColor(@Nonnull final Color base, @Nonnull final Color fade) {
        this(base, fade, base);
    }

    private AuroraColor(@Nonnull final Color base, @Nonnull final Color fade, @Nonnull final Color mid) {
        this.baseColor = base;
        this.fadeColor = fade;
        this.middleColor = mid;
    }

    @Nonnull
    public static AuroraColor get(@Nonnull final Random random) {
        final int idx = random.nextInt(COLOR_SETS.size());
        return COLOR_SETS.get(idx);
    }
}

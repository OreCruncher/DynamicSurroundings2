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

package org.orecruncher.lib;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;

/**
 * Holds an RGB triple. See: http://www.rapidtables.com/web/color/RGB_Color.htm
 */
@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class Color {

    public static final Color RED = new Color(255, 0, 0);
    public static final Color ORANGE = new Color(255, 127, 0);
    public static final Color YELLOW = new Color(255, 255, 0);
    public static final Color LGREEN = new Color(127, 255, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color TURQOISE = new Color(0, 255, 127);
    public static final Color CYAN = new Color(0, 255, 255);
    public static final Color AUQUAMARINE = new Color(0, 127, 255);
    public static final Color BLUE = new Color(0, 0, 255);
    public static final Color VIOLET = new Color(127, 0, 255);
    public static final Color MAGENTA = new Color(255, 0, 255);
    public static final Color RASPBERRY = new Color(255, 0, 127);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color PURPLE = new Color(80, 0, 80);
    public static final Color INDIGO = new Color(75, 0, 130);
    public static final Color NAVY = new Color(0, 0, 128);
    public static final Color TAN = new Color(210, 180, 140);
    public static final Color GOLD = new Color(255, 215, 0);
    public static final Color GRAY = new Color(128, 128, 128);
    public static final Color LGRAY = new Color(192, 192, 192);
    public static final Color SLATEGRAY = new Color(112, 128, 144);
    public static final Color DARKSLATEGRAY = new Color(47, 79, 79);

    // Minecraft colors mapped to codes
    public static final Color MC_BLACK = new Color(0, 0, 0);
    public static final Color MC_DARKBLUE = new Color(0, 0, 170);
    public static final Color MC_DARKGREEN = new Color(0, 170, 0);
    public static final Color MC_DARKAQUA = new Color(0, 170, 170);
    public static final Color MC_DARKRED = new Color(170, 0, 0);
    public static final Color MC_DARKPURPLE = new Color(170, 0, 170);
    public static final Color MC_GOLD = new Color(255, 170, 0);
    public static final Color MC_GRAY = new Color(170, 170, 170);
    public static final Color MC_DARKGRAY = new Color(85, 85, 85);
    public static final Color MC_BLUE = new Color(85, 85, 255);
    public static final Color MC_GREEN = new Color(85, 255, 85);
    public static final Color MC_AQUA = new Color(85, 255, 255);
    public static final Color MC_RED = new Color(255, 85, 85);
    public static final Color MC_LIGHTPURPLE = new Color(255, 85, 255);
    public static final Color MC_YELLOW = new Color(255, 255, 85);
    public static final Color MC_WHITE = new Color(255, 255, 255);

    // Basic Aurora color
    public static final Color AURORA_RED = new Color(1.0F, 0F, 0F);
    public static final Color AURORA_GREEN = new Color(0.5F, 1.0F, 0.0F);
    public static final Color AURORA_BLUE = new Color(0F, 0.8F, 1.0F);

    private static final Map<TextFormatting, Color> colorLookup = new EnumMap<>(TextFormatting.class);

    static {
        colorLookup.put(TextFormatting.BLACK, MC_BLACK);
        colorLookup.put(TextFormatting.DARK_BLUE, MC_DARKBLUE);
        colorLookup.put(TextFormatting.DARK_GREEN, MC_DARKGREEN);
        colorLookup.put(TextFormatting.DARK_AQUA, MC_DARKAQUA);
        colorLookup.put(TextFormatting.DARK_RED, MC_DARKRED);
        colorLookup.put(TextFormatting.DARK_PURPLE, MC_DARKPURPLE);
        colorLookup.put(TextFormatting.GOLD, MC_GOLD);
        colorLookup.put(TextFormatting.GRAY, MC_GRAY);
        colorLookup.put(TextFormatting.DARK_GRAY, MC_DARKGRAY);
        colorLookup.put(TextFormatting.BLUE, MC_BLUE);
        colorLookup.put(TextFormatting.GREEN, MC_GREEN);
        colorLookup.put(TextFormatting.AQUA, MC_AQUA);
        colorLookup.put(TextFormatting.RED, MC_RED);
        colorLookup.put(TextFormatting.LIGHT_PURPLE, MC_LIGHTPURPLE);
        colorLookup.put(TextFormatting.YELLOW, MC_YELLOW);
        colorLookup.put(TextFormatting.WHITE, MC_WHITE);
    }

    protected float red;
    protected float green;
    protected float blue;
    protected float alpha;
    public Color(@Nonnull final Color color) {
        this(color.red, color.green, color.blue, color.alpha);
    }

    public Color(final int red, final int green, final int blue) {
        this(red, green, blue, 255);
    }

    public Color(final int red, final int green, final int blue, final int alpha) {
        this(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public Color(@Nonnull final Vec3d vec) {
        this((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public Color(final int rgb) {
        this((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, (rgb >> 24) & 0xff);
    }

    public Color(final float red, final float green, final float blue) {
        this(red, green, blue, 1F);
    }

    public Color(final float red, final float green, final float blue, final float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public Color(final double red, final double green, final double blue, final double alpha) {
        this((float) red, (float) green, (float) blue, (float) alpha);
    }

    public static Color getColor(final TextFormatting format) {
        return colorLookup.get(format);
    }

    protected static float blend(final float c1, final float c2, final float factor) {
        return (float) Math.sqrt((1.0F - factor) * c1 * c1 + factor * c2 * c2);
    }

    public float red() {
        return this.red;
    }

    public float green() {
        return this.green;
    }

    public float blue() {
        return this.blue;
    }

    @Nonnull
    public Vec3d toVec3d() {
        return new Vec3d(this.red, this.green, this.blue);
    }

    /*
     * Calculates the RGB adjustments to make to the color to arrive at the target
     * color after the specified number of iterations.
     */
    @Nonnull
    public Vec3d transitionTo(@Nonnull final Color target, final int iterations) {
        final double deltaRed = (target.red - this.red) / iterations;
        final double deltaGreen = (target.green - this.green) / iterations;
        final double deltaBlue = (target.blue - this.blue) / iterations;
        return new Vec3d(deltaRed, deltaGreen, deltaBlue);
    }

    @Nonnull
    public Color scale(final float scaleFactor) {
        return scale(scaleFactor, scaleFactor, scaleFactor);
    }

    @Nonnull
    public Color scale(final float scaleRed, final float scaleGreen, final float scaleBlue) {
        return new Color(this.red * scaleRed, this.green * scaleGreen, this.blue * scaleBlue, this.alpha);
    }

    @Nonnull
    public Color add(@Nonnull final Color color) {
        return add(color.red, color.green, color.blue);
    }

    @Nonnull
    public Color add(final float red, final float green, final float blue) {
        return new Color(this.red + red, this.green + green, this.blue + blue, this.alpha);
    }

    @Nonnull
    public Color blend(@Nonnull final Color color, final float factor) {
        return new Color(
                blend(this.red, color.red, factor),
                blend(this.green, color.green, factor),
                blend(this.blue, color.blue, factor),
                this.alpha);
    }

    @Nonnull
    public Color mix(@Nonnull final Color color) {
        return mix(color.red, color.green, color.blue);
    }

    @Nonnull
    public Color mix(final float red, final float green, final float blue) {
        return new Color(
                (this.red + red) / 2.0F,
                (this.green + green) / 2.0F,
                (this.blue + blue) / 2.0F,
                this.alpha);
    }

    // Adjust luminance based on the specified percent. > 0 brightens; < 0
    // darkens
    @Nonnull
    public Color luminance(final float percent) {
        final float r = Math.min(Math.max(0, this.red + (this.red * percent)), 1.0F);
        final float g = Math.min(Math.max(0, this.green + (this.green * percent)), 1.0F);
        final float b = Math.min(Math.max(0, this.blue + (this.blue * percent)), 1.0F);
        return new Color(r, g, b, this.alpha);
    }

    public int rgb() {
        final int iRed = (int) (this.red * 255);
        final int iGreen = (int) (this.green * 255);
        final int iBlue = (int) (this.blue * 255);
        final int iAlpha = (int) (this.alpha * 255);
        return iAlpha << 24 | iRed << 16 | iGreen << 8 | iBlue;
    }

    @Override
    public int hashCode() {
        int result = Float.hashCode(this.red);
        result = 31 * result + Float.hashCode(this.green);
        result = 31 * result + Float.hashCode(this.blue);
        result = 31 * result + Float.hashCode(this.alpha);
        return result;
    }

    @Override
    public boolean equals(final Object anObject) {
        if (!(anObject instanceof Color))
            return false;
        final Color color = (Color) anObject;
        return this.red == color.red && this.green == color.green && this.blue == color.blue && this.alpha == this.alpha;
    }

    @Nonnull
    public MutableColor asMutable() {
        return new MutableColor(this);
    }

    @Override
    @Nonnull
    public String toString() {
        return "[r:" + (int) (this.red * 255) +
                ",g:" + (int) (this.green * 255) +
                ",b:" + (int) (this.blue * 255) +
                ",a:" + (int) (this.alpha * 255) +
                ']';
    }

    public static final class MutableColor extends Color {

        MutableColor(@Nonnull final Color color) {
            super(color);
        }

        @Nonnull
        @Override
        public Color add(final float red, final float green, final float blue) {
            this.red += red;
            this.green += green;
            this.blue += blue;
            return this;
        }

        @Nonnull
        @Override
        public Color blend(@Nonnull final Color color, final float factor) {
            this.red = blend(this.red, color.red, factor);
            this.green = blend(this.green, color.green, factor);
            this.blue = blend(this.blue, color.blue, factor);
            return this;
        }

        @Nonnull
        @Override
        public Color scale(final float scaleRed, final float scaleGreen, final float scaleBlue) {
            this.red *= scaleRed;
            this.green *= scaleGreen;
            this.blue *= scaleBlue;
            return this;
        }

        @Nonnull
        @Override
        public Color mix(final float red, final float green, final float blue) {
            this.red = (this.red + red) / 2.0F;
            this.green = (this.green + green) / 2.0F;
            this.blue = (this.blue + blue) / 2.0F;
            return this;
        }

        @Nonnull
        public Color adjust(@Nonnull final Vec3d adjust, @Nonnull final Color target) {
            this.red += adjust.x;
            if ((adjust.x < 0.0F && this.red < target.red) || (adjust.x > 0.0F && this.red > target.red)) {
                this.red = target.red;
            }

            this.green += adjust.y;
            if ((adjust.y < 0.0F && this.green < target.green) || (adjust.y > 0.0F && this.green > target.green)) {
                this.green = target.green;
            }

            this.blue += adjust.z;
            if ((adjust.z < 0.0F && this.blue < target.blue) || (adjust.z > 0.0F && this.blue > target.blue)) {
                this.blue = target.blue;
            }
            return this;
        }

        @Nonnull
        public Color asImmutable() {
            return new Color(this);
        }
    }
}

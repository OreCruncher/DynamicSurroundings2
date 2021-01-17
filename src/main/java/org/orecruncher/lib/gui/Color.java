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

package org.orecruncher.lib.gui;

import com.google.common.base.Preconditions;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.math.MathStuff;

import javax.annotation.Nonnull;

/**
 * Holds an RGB triple. See: http://www.rapidtables.com/web/color/RGB_Color.htm
 */
@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class Color {

    protected float red;
    protected float green;
    protected float blue;
    protected float alpha;

    public Color(@Nonnull final String fmt) {
        final String[] parts = fmt.split(",");
        Preconditions.checkArgument(parts.length > 2);

        final int r = Integer.getInteger(parts[0]);
        final int g = Integer.getInteger(parts[1]);
        final int b = Integer.getInteger(parts[2]);
        final int a = parts.length == 4 ? Integer.getInteger(parts[3]) : 255;

        this.red = MathStuff.clamp1(r / 255F);
        this.green = MathStuff.clamp1(g / 255F);
        this.blue = MathStuff.clamp1(b / 255F);
        this.alpha = MathStuff.clamp1(a / 255F);
    }

    public Color(@Nonnull final TextFormatting fmt) {
        Preconditions.checkArgument(fmt.isColor());
        Preconditions.checkNotNull(fmt.getColor());

        final int color = fmt.getColor();
        this.red = ((color >> 16) & 0xff) / 255F;
        this.green = ((color >> 8) & 0xff) / 255F;
        this.blue = (color & 0xff) / 255F;
        this.alpha = 1F;
    }

    public Color(@Nonnull final Color color) {
        this(color.red, color.green, color.blue, color.alpha);
    }

    public Color(final int red, final int green, final int blue) {
        this(red, green, blue, 255);
    }

    public Color(final int red, final int green, final int blue, final int alpha) {
        this(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public Color(@Nonnull final Vector3d vec) {
        this((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public Color(final int rgb) {
        this((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, (rgb >> 24) & 0xff);
    }

    public Color(final float red, final float green, final float blue) {
        this(red, green, blue, 1F);
    }

    public Color(final float red, final float green, final float blue, final float alpha) {
        this.red = MathStuff.clamp1(red);
        this.green = MathStuff.clamp1(green);
        this.blue = MathStuff.clamp1(blue);
        this.alpha = MathStuff.clamp1(alpha);
    }

    public Color(final double red, final double green, final double blue, final double alpha) {
        this((float) red, (float) green, (float) blue, (float) alpha);
    }

    public static Color parse(@Nonnull final String input) {
        if (input.startsWith("#")) {
            return new Color(Integer.parseInt(input.substring(1), 16));
        }

        int[] parts = Utilities.splitToInts(input, ',');

        if (parts.length < 3) {
            throw new IllegalArgumentException(String.format("'%s' is not a valid color definition", input));
        }

        return new Color(
                MathStuff.clamp(parts[0], 0, 255),
                MathStuff.clamp(parts[1], 0, 255),
                MathStuff.clamp(parts[2], 0, 255)
        );
    }

    protected static float blnd(final float c1, final float c2, final float factor) {
        return MathStuff.clamp1((float) Math.sqrt((1.0F - factor) * c1 * c1 + factor * c2 * c2));
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
    public Vector3d toVec3d() {
        return new Vector3d(this.red, this.green, this.blue);
    }

    /*
     * Calculates the RGB adjustments to make to the color to arrive at the target
     * color after the specified number of iterations.
     */
    @Nonnull
    public Vector3d transitionTo(@Nonnull final Color target, final int iterations) {
        final double deltaRed = (target.red - this.red) / iterations;
        final double deltaGreen = (target.green - this.green) / iterations;
        final double deltaBlue = (target.blue - this.blue) / iterations;
        return new Vector3d(deltaRed, deltaGreen, deltaBlue);
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
                blnd(this.red, color.red, factor),
                blnd(this.green, color.green, factor),
                blnd(this.blue, color.blue, factor),
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
        final float r = MathStuff.clamp1(this.red + (this.red * percent));
        final float g = MathStuff.clamp1(this.green + (this.green * percent));
        final float b = MathStuff.clamp1(this.blue + (this.blue * percent));
        return new Color(r, g, b, this.alpha);
    }

    public int rgb() {
        final int iRed = (int) (this.red * 255);
        final int iGreen = (int) (this.green * 255);
        final int iBlue = (int) (this.blue * 255);
        final int iAlpha = (int) (this.alpha * 255);
        return iAlpha << 24 | iRed << 16 | iGreen << 8 | iBlue;
    }

    public int rgbWithAlpha(final float alpha) {
        return rgbWithAlpha(alpha * 255);
    }

    public int rgbWithAlpha(final int alpha) {
        final int iRed = (int) (this.red * 255);
        final int iGreen = (int) (this.green * 255);
        final int iBlue = (int) (this.blue * 255);
        final int iAlpha = (int) (this.alpha * 255);
        return alpha * 255 << 24 | iRed << 16 | iGreen << 8 | iBlue;
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
            this.red = MathStuff.clamp1(this.red + red);
            this.green = MathStuff.clamp1(this.green + green);
            this.blue = MathStuff.clamp1(this.blue + blue);
            return this;
        }

        @Nonnull
        @Override
        public Color blend(@Nonnull final Color color, final float factor) {
            this.red = blnd(this.red, color.red, factor);
            this.green = blnd(this.green, color.green, factor);
            this.blue = blnd(this.blue, color.blue, factor);
            return this;
        }

        @Nonnull
        @Override
        public Color scale(final float scaleRed, final float scaleGreen, final float scaleBlue) {
            this.red = MathStuff.clamp1(this.red * scaleRed);
            this.green = MathStuff.clamp1(this.green * scaleGreen);
            this.blue = MathStuff.clamp1(this.blue * scaleBlue);
            return this;
        }

        @Nonnull
        @Override
        public Color mix(final float red, final float green, final float blue) {
            this.red = MathStuff.clamp1((this.red + red) / 2.0F);
            this.green = MathStuff.clamp1((this.green + green) / 2.0F);
            this.blue = MathStuff.clamp1((this.blue + blue) / 2.0F);
            return this;
        }

        @Nonnull
        public Color adjust(@Nonnull final Vector3d adjust, @Nonnull final Color target) {
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

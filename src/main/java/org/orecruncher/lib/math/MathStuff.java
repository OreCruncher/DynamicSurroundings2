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

package org.orecruncher.lib.math;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;

/**
 * Replacement algos for SIN_TABLE/cos in Minecraft's MathStuff routines. Use
 * the Riven method:
 * <p>
 * http://riven8192.blogspot.com/2009/08/fastmath-sincos-lookup-tables.html
 * http://riven8192.blogspot.com/2009/08/fastmath-atan2-lookup-table.html
 */
@SuppressWarnings("unused")
public final class MathStuff {
    public static final double PHI = 0.5D + Math.sqrt(5) / 2D;  // Golden ratio
    public static final float PHI_F = (float) PHI;
    public static final double ANGLE = PHI * Math.PI * 2D;
    public static final float ANGLE_F = (float) ANGLE;
    public static final float PI_F = (float) Math.PI;
    public static final float E_F = (float) Math.E;

    private static final int SIN_BITS = 16;
    private static final int SIN_MASK = ~(-1 << SIN_BITS);
    private static final int SIN_COUNT = SIN_MASK + 1;
    private static final float RAD_FULL = (float) (Math.PI * 2.0);
    private static final float RAD_TO_INDEX = SIN_COUNT / RAD_FULL;
    private static final float DEG_FULL = 360.0F;
    private static final float DEG_TO_INDEX = SIN_COUNT / DEG_FULL;
    private static final float COS_TO_SIN = (float) (Math.PI / 2.0);
    private static final float[] SIN_TABLE = new float[SIN_COUNT];
    private static final int ATAN2_BITS = 7;
    private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
    private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
    private static final int ATAN2_COUNT = ATAN2_MASK + 1;
    private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
    private static final float ATAN2_DIM_MINUS_1 = (ATAN2_DIM - 1);
    private static final float[] ATAN2_TABLE = new float[ATAN2_COUNT];
    private static final float RAD_TO_DEG = (float) (180.D / Math.PI);
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0D);

    static {

        final double twoPi = Math.PI * 2;
        for (int i = 0; i < SIN_COUNT; i++) {
            SIN_TABLE[i] = (float) Math.sin((i + 0.5D) / SIN_COUNT * twoPi);
        }

        // Fix-up cardinals
        for (int i = 0; i < 360; i += 90) {
            SIN_TABLE[(int) (i * DEG_TO_INDEX) & SIN_MASK] = (float) Math.sin(i * Math.PI / 180.0);
        }

        // atan2
        for (int i = 0; i < ATAN2_DIM; i++) {
            for (int j = 0; j < ATAN2_DIM; j++) {
                final float x0 = (float) i / ATAN2_DIM;
                final float y0 = (float) j / ATAN2_DIM;
                ATAN2_TABLE[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
            }
        }
    }

    private MathStuff() {
    }

    public static float sin(final float rad) {
        return SIN_TABLE[(int) (rad * RAD_TO_INDEX) & SIN_MASK];
    }

    public static float cos(final float rad) {
        return SIN_TABLE[(int) ((rad + COS_TO_SIN) * RAD_TO_INDEX) & SIN_MASK];
    }

    public static float tan(final float rad) {
        return sin(rad) / cos(rad);
    }

    public static double sin(final double rad) {
        final float tmp = (float) rad;
        return SIN_TABLE[(int) (tmp * RAD_TO_INDEX) & SIN_MASK];
    }

    public static double cos(final double rad) {
        final float tmp = (float) rad;
        return SIN_TABLE[(int) ((tmp + COS_TO_SIN) * RAD_TO_INDEX) & SIN_MASK];
    }

    public static double tan(final double rad) {
        return tan((float) rad);
    }

    public static float atan2(float y, float x) {
        final float add, mul;

        if (x < 0.0f) {
            if (y < 0.0f) {
                x = -x;
                y = -y;

                mul = 1.0f;
            } else {
                x = -x;
                mul = -1.0f;
            }

            add = -PI_F;
        } else {
            if (y < 0.0f) {
                y = -y;
                mul = -1.0f;
            } else {
                mul = 1.0f;
            }

            add = 0.0f;
        }

        final float invDiv = ATAN2_DIM_MINUS_1 / (Math.max(x, y));
        final int xi = (int) (x * invDiv);
        final int yi = (int) (y * invDiv);

        return (ATAN2_TABLE[yi * ATAN2_DIM + xi] + add) * mul;
    }

    public static double atan2(final double y, final double x) {
        final float tmp1 = (float) y;
        final float tmp2 = (float) x;
        return atan2(tmp1, tmp2);
    }

    // Misc functions

    public static float toRadians(final float degrees) {
        return degrees * DEG_TO_RAD;
    }

    public static float toDegrees(final float radians) {
        return radians * RAD_TO_DEG;
    }

    public static float wrapDegrees(float value) {
        return MathHelper.wrapDegrees(value);
    }

    public static double wrapDegrees(double value) {
        return MathHelper.wrapDegrees(value);
    }

    public static float abs(final float val) {
        return Math.abs(val);
    }

    public static double abs(final double val) {
        return Math.abs(val);
    }

    public static long abs(final long val) {
        return Math.abs(val);
    }

    public static int abs(final int val) {
        return Math.abs(val);
    }

    public static float sqrt(final float value) {
        return (float) Math.sqrt(value);
    }

    public static double sqrt(final double value) {
        return Math.sqrt(value);
    }

    public static int floor(final double value) {
        final int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static int floor(final float value) {
        final int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static double log(final double value) {
        return value < 0.03D ? Math.log(value) : 6 * (value - 1) / (value + 1 + 4 * (Math.sqrt(value)));
}

    public static double pow(final double a, final double b) {
        final long tmp = Double.doubleToRawLongBits(a);
        final long tmp2 = (long) (b * (tmp - 4606921280493453312L)) + 4606921280493453312L;
        return Double.longBitsToDouble(tmp2);
    }

    public static double exp(final double val) {
        final long tmp = (long) (1512775 * val + (1072693248 - 60801));
        return Double.longBitsToDouble(tmp << 32);
    }

    public static float clamp(final float num, final float min, final float max) {
        return num <= min ? min : min(num, max);
    }

    public static double clamp(final double num, final double min, final double max) {
        return num <= min ? min : min(num, max);
    }

    public static int clamp(final int num, final int min, final int max) {
        return num <= min ? min : min(num, max);
    }

    public static int min(final int v1, final int v2) {
        return v1 <= v2 ? v1 : v2;
    }

    public static int max(final int v1, final int v2) {
        return v1 <= v2 ? v2 : v1;
    }

    public static float min(final float v1, final float v2) {
        return v1 <= v2 ? v1 : v2;
    }

    public static float max(final float v1, final float v2) {
        return v1 <= v2 ? v2 : v1;
    }

    public static double min(final double v1, final double v2) {
        return v1 <= v2 ? v1 : v2;
    }

    public static double max(final double v1, final double v2) {
        return v1 <= v2 ? v2 : v1;
    }

    /**
     * Clamps the value between 0 and 1.
     *
     * @param num Number to clamp
     * @return Number clamped between 0 and 1
     */
    public static float clamp1(final float num) {
        return num <= 0 ? 0F : min(num, 1F);
    }

    /**
     * Clamps the value between 0 and 1.
     *
     * @param num Number to clamp
     * @return Number clamped between 0 and 1
     */
    public static double clamp1(final double num) {
        return num <= 0 ? 0F : min(num, 1F);
    }

    // Assumes center at origin.
    @Nonnull
    public static Vector2f rotateScale(@Nonnull final Vector2f coord, final float radians, final float scale) {
        final float f = cos(radians);
        final float f1 = sin(radians);
        final float d0 = coord.x * f + coord.y * f1;
        final float d1 = coord.y * f - coord.x * f1;
        return new Vector2f(d0 * scale, d1 * scale);
    }

    @Nonnull
    public static Vector2f rotate(@Nonnull final Vector2f coord, final float radians) {
        return rotateScale(coord, radians, 1F);
    }

    @Nonnull
    public static Vector3d getVectorForRotation(final float pitch, final float yaw) {
        final float f = cos(-yaw * 0.017453292F - PI_F);
        final float f1 = sin(-yaw * 0.017453292F - PI_F);
        final float f2 = -cos(-pitch * 0.017453292F);
        final float f3 = sin(-pitch * 0.017453292F);
        return new Vector3d(f1 * f2, f3, f * f2);
    }

    /**
     * Calculate the reflection of a vector based on a surface normal.
     *
     * @param vector        Incoming vector
     * @param surfaceNormal Surface normal
     * @return The reflected vector
     */
    @Nonnull
    public static Vector3d reflection(@Nonnull final Vector3d vector, @Nonnull final Vector3d surfaceNormal) {
        final double dot2 = vector.dotProduct(surfaceNormal) * 2;
        final double x = vector.x - dot2 * surfaceNormal.x;
        final double y = vector.y - dot2 * surfaceNormal.y;
        final double z = vector.z - dot2 * surfaceNormal.z;
        return new Vector3d(x, y, z);
    }

    public static boolean isValid(@Nonnull final Vector3d vec) {
        return !(Double.isNaN(vec.x) || Double.isNaN(vec.y) || Double.isNaN(vec.z));
    }

    /**
     * Simple method to add a scaled addened to a base.  Eliminates unecessary allocations.
     * @param base Base to add another scaled vector to
     * @param addened Vector to scale and add to the base
     * @param scale Scale to apply to the addened vector before adding to the base
     * @return Vector that is a sum of the base and the addened that has been scaled
     */
    @Nonnull
    public static Vector3d addScaled(@Nonnull final Vector3d base, @Nonnull final Vector3d addened, final double scale) {
        return new Vector3d(base.x + addened.x * scale, base.y + addened.y * scale, base.z + addened.z * scale);
    }

    /**
     * Calculates the normal vector between two points
     * @param origin Origin point
     * @param target Point where the normal will point
     * @return Normal vector between the two points
     */
    @Nonnull
    public static Vector3d normalize(@Nonnull final Vector3d origin, @Nonnull final Vector3d target) {
        final double x = target.x - origin.x;
        final double y = target.y - origin.y;
        final double z = target.z - origin.z;
        final double d0 = Math.sqrt(x * x + y * y + z * z);
        return d0 < 1.0E-4D ? Vector3d.ZERO : new Vector3d(x / d0, y / d0, z / d0);
    }
}

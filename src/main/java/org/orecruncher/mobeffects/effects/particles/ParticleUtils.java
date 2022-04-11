/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
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

package org.orecruncher.mobeffects.effects.particles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.orecruncher.lib.random.XorShiftRandom;

import javax.annotation.Nonnull;
import java.util.Random;

public final class ParticleUtils {
    private static final Random RANDOM = XorShiftRandom.current();

    private ParticleUtils() {

    }

    @Nonnull
    public static Vector3d getBreathOrigin(@Nonnull final LivingEntity entity) {
        final Vector3d eyePosition = eyePosition(entity).subtract(0D, entity.isChild() ? 0.1D : 0.2D, 0D);
        final Vector3d look = entity.getLook(1F); // Don't use the other look vector method!
        return eyePosition.add(look.scale(entity.isChild() ? 0.25D : 0.5D));
    }

    @Nonnull
    public static Vector3d getLookTrajectory(@Nonnull final LivingEntity entity) {
        return entity.getLook(1F).rotateYaw(RANDOM.nextFloat() * 2F).rotatePitch(RANDOM.nextFloat() * 2F)
                .normalize();
    }

    /*
     * Use some corrective lenses because the MC routine just doesn't lower the
     * height enough for our rendering purpose.
     */
    private static Vector3d eyePosition(final Entity e) {
        Vector3d t = e.getEyePosition(1F);
        if (e.isSneaking())
            t = t.subtract(0D, 0.25D, 0D);
        return t;
    }

}

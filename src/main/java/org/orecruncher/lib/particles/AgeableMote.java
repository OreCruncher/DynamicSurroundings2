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

package org.orecruncher.lib.particles;

import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;

public abstract class AgeableMote extends Mote {

    protected int age;
    protected int maxAge;

    protected AgeableMote(@Nonnull final IBlockReader world, final double x, final double y, final double z) {
        super(world, x, y, z);
        this.age = 0;
    }

    protected boolean advanceAge() {
        return this.age++ >= this.maxAge;
    }

    @Override
    public boolean tick() {

        // The mote reached it's life expectancy
        if (advanceAge())
            kill();

        return super.tick();
    }
}

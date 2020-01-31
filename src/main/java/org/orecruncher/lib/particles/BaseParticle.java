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

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.random.XorShiftRandom;

import javax.annotation.Nonnull;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public abstract class BaseParticle extends Particle {

    protected static final EntityRendererManager manager = GameUtils.getMC().getRenderManager();
    protected static final FontRenderer font = GameUtils.getMC().fontRenderer;
    protected static final Random RANDOM = XorShiftRandom.current();

    protected BaseParticle(@Nonnull final World worldIn, final double posXIn, final double posYIn,
                           final double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
    }

    public BaseParticle(@Nonnull final World worldIn, final double xCoordIn, final double yCoordIn,
                        final double zCoordIn, final double xSpeedIn, final double ySpeedIn, final double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
    }

    protected double interpX() {
        return Particle.interpPosX;
    }

    protected double interpY() {
        return Particle.interpPosY;
    }

    protected double interpZ() {
        return Particle.interpPosZ;
    }

}
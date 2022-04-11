/*
 *  Dynamic Surroundings
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

package org.orecruncher.environs.effects.emitters;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.IBlockReader;
import org.orecruncher.environs.effects.SteamJetEffect;
import org.orecruncher.environs.effects.particles.SteamCloudParticle;
import org.orecruncher.lib.GameUtils;

public class SteamJet extends Jet {

    private final BlockState source;

    public SteamJet(final int strength, final IBlockReader world, final double x, final double y, final double z) {
        super(strength, world, x, y, z);
        this.source = world.getBlockState(getPos());
    }

    @Override
    public boolean shouldDie() {
        return !SteamJetEffect.isValidSpawnBlock(GameUtils.getWorld(), getPos(), this.source);
    }

    @Override
    protected void spawnJetParticle() {
        final Particle particle = new SteamCloudParticle(GameUtils.getWorld(), this.posX, this.posY, this.posZ, 0.1D);
        GameUtils.getMC().particles.addEffect(particle);
    }

}
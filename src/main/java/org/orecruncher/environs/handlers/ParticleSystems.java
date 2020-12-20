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

package org.orecruncher.environs.handlers;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.effects.emitters.ParticleEmitter;
import org.orecruncher.lib.BlockPosUtil;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class ParticleSystems extends HandlerBase {

    private static final Predicate<ParticleEmitter> STANDARD = system -> {
        system.tick();
        return !system.isAlive();
    };

    ParticleSystems() {
        super("Particle Systems");
    }

    private static ParticleSystems _instance = null;

    private final Long2ObjectOpenHashMap<ParticleEmitter> systems = new Long2ObjectOpenHashMap<>(512);
    private BlockPos lastPos = BlockPos.ZERO;

    @Override
    public boolean doTick(final long tick) {
        return this.systems.size() > 0;
    }

    @Override
    public void process(@Nonnull final PlayerEntity player) {
        final BlockPos current = CommonState.getPlayerPosition();
        final boolean sittingStill = this.lastPos.equals(current);
        this.lastPos = current;

        Predicate<ParticleEmitter> pred = STANDARD;

        if (!sittingStill) {
            final double range = Config.CLIENT.effects.get_effectRange();
            final BlockPos min = new BlockPos(current.getX() - range, current.getY() - range, current.getZ() - range);
            final BlockPos max = new BlockPos(current.getX() + range, current.getY() + range, current.getZ() + range);

            pred = system -> {
                if (BlockPosUtil.notContains(system.getPos(), min, max)) {
                    system.setExpired();
                } else {
                    system.tick();
                }
                return !system.isAlive();
            };
        }

        this.systems.values().removeIf(pred);
    }

    @Override
    public void onConnect() {
        _instance = this;
        this.systems.clear();
    }

    @Override
    public void onDisconnect() {
        this.systems.clear();
        _instance = null;
    }

    // Determines if it is OK to spawn a particle system at the specified
    // location. Generally only a single system can occupy a block.
    public static boolean okToSpawn(@Nonnull final BlockPos pos) {
        return !_instance.systems.containsKey(pos.toLong());
    }

    public static void add(@Nonnull final ParticleEmitter system) {
        _instance.systems.put(system.getPos().toLong(), system);
    }

}

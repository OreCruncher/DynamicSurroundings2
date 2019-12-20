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

package org.orecruncher.lib.scripting.sets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.scripting.VariableSet;

import javax.annotation.Nonnull;

public class PlayerVariableSet extends VariableSet<IPlayerVariables> implements IPlayerVariables {

    private final LazyVariable<Boolean> isBurning = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getPlayer().isBurning());
    private final LazyVariable<Boolean> isSuffocating = new LazyVariable<>(() -> {
        if (GameUtils.isInGame()) {
            final PlayerEntity player = GameUtils.getPlayer();
            return !player.isCreative() && player.getAir() < 0;
        }
        return false;
    });
    private final LazyVariable<Boolean> isFlying = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getPlayer().isAirBorne);
    private final LazyVariable<Boolean> isSprintnig = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getPlayer().isSprinting());
    private final LazyVariable<Boolean> isInLava = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getPlayer().isInLava());
    private final LazyVariable<Boolean> isInvisible = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getPlayer().isInvisible());
    private final LazyVariable<Boolean> isBlind = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getPlayer().isPotionActive(Effects.BLINDNESS));
    private final LazyVariable<Boolean> isInWater = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getPlayer().isInvisible());
    private final LazyVariable<Boolean> isMoving = new LazyVariable<>(() -> {
        if (GameUtils.isInGame()) {
            final PlayerEntity player = GameUtils.getPlayer();
            return player.distanceWalkedModified != player.prevDistanceWalkedModified;
        }
        return false;
    });
    private final LazyVariable<Boolean> isWet = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getPlayer().isWet());
    private final LazyVariable<Boolean> isRiding = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getPlayer().isOnePlayerRiding());
    private final LazyVariable<Boolean> isOnGround = new LazyVariable<>(() -> GameUtils.isInGame() && GameUtils.getPlayer().onGround);
    private final LazyVariable<Boolean> canSeeSky = new LazyVariable<>(() -> {
        if (GameUtils.isInGame()) {
            final World world = GameUtils.getWorld();
            final PlayerEntity player = GameUtils.getPlayer();
            return world.canBlockSeeSky(player.getPosition().add(0, 2, 0));
        }
        return false;
    });
    private final LazyVariable<Boolean> canRainOn = new LazyVariable<>(() -> {
        if (GameUtils.isInGame()) {
            final World world = GameUtils.getWorld();
            final PlayerEntity player = GameUtils.getPlayer();
            if (world.canBlockSeeSky(player.getPosition().add(0, 2, 0)))
                return WorldUtils.getTopSolidOrLiquidBlock(world, player.getPosition()).getY() <= player.getPosition().getY();
        }
        return false;
    });
    private final LazyVariable<Float> health = new LazyVariable<>(() -> GameUtils.isInGame() ? GameUtils.getPlayer().getHealth() : 0F);
    private final LazyVariable<Float> maxHealth = new LazyVariable<>(() -> GameUtils.isInGame() ? GameUtils.getPlayer().getMaxHealth() : 0F);
    private final LazyVariable<Float> x = new LazyVariable<>(() -> GameUtils.isInGame() ? (float) GameUtils.getPlayer().posX : 0F);
    private final LazyVariable<Float> y = new LazyVariable<>(() -> GameUtils.isInGame() ? (float) GameUtils.getPlayer().posY : 0F);
    private final LazyVariable<Float> z = new LazyVariable<>(() -> GameUtils.isInGame() ? (float) GameUtils.getPlayer().posZ : 0F);

    public PlayerVariableSet() {
        super("player");
    }

    @Override
    public void update() {

        this.isBurning.reset();
        this.isSuffocating.reset();
        this.isFlying.reset();
        this.isSprintnig.reset();
        this.isInLava.reset();
        this.isInvisible.reset();
        this.isBlind.reset();
        this.isInWater.reset();
        this.isMoving.reset();
        this.x.reset();
        this.y.reset();
        this.z.reset();
        this.isWet.reset();
        this.isRiding.reset();
        this.isOnGround.reset();
        this.health.reset();
        this.maxHealth.reset();
        this.canRainOn.reset();
        this.canSeeSky.reset();

    }

    @Nonnull
    @Override
    public IPlayerVariables getInterface() {
        return this;
    }

    @Override
    public boolean isBurning() {
        return this.isBurning.get();
    }

    @Override
    public boolean isSuffocating() {
        return this.isSuffocating.get();
    }

    @Override
    public boolean isFlying() {
        return this.isFlying.get();
    }

    @Override
    public boolean isSprintnig() {
        return this.isSprintnig.get();
    }

    @Override
    public boolean isInLava() {
        return this.isInLava.get();
    }

    @Override
    public boolean isInvisible() {
        return this.isInvisible.get();
    }

    @Override
    public boolean isBlind() {
        return this.isBlind.get();
    }

    @Override
    public boolean isInWater() {
        return this.isInWater.get();
    }

    @Override
    public boolean isMoving() {
        return this.isMoving.get();
    }

    @Override
    public boolean isWet() {
        return this.isWet.get();
    }

    @Override
    public boolean isRiding() {
        return this.isRiding.get();
    }

    @Override
    public boolean isOnGround() {
        return this.isOnGround.get();
    }

    @Override
    public boolean canRainOn() {
        return this.canRainOn.get();
    }

    @Override
    public boolean canSeeSky() {
        return this.canSeeSky.get();
    }

    @Override
    public float getHealth() {
        return this.health.get();
    }

    @Override
    public float getMaxHealth() {
        return this.maxHealth.get();
    }

    @Override
    public float getX() {
        return this.x.get();
    }

    @Override
    public float getY() {
        return this.y.get();
    }

    @Override
    public float getZ() {
        return this.z.get();
    }
}

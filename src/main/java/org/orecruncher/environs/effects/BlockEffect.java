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

package org.orecruncher.environs.effects;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.environs.handlers.scripts.ConditionEvaluator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public abstract class BlockEffect {

    private int chance;
    protected String conditions = StringUtils.EMPTY;

    public BlockEffect() {
        this(100);
    }

    protected BlockEffect(final int chance) {
        this.chance = chance;
    }

    @Nonnull
    public abstract BlockEffectType getEffectType();

    public void setConditions(@Nullable final String conditions) {
        this.conditions = conditions == null ? StringUtils.EMPTY : conditions.intern();
    }

    @Nonnull
    public String getConditions() {
        return this.conditions;
    }

    public void setChance(final int chance) {
        this.chance = chance;
    }

    public int getChance() {
        return this.chance;
    }

    public boolean alwaysExecute() {
        return this.chance == 0;
    }

    /**
     * Determines if the effect can trigger. Classes that override this method
     * should make sure to call the parent last to avoid necessary CPU churn related
     * to the script check.
     */
    public boolean canTrigger(@Nonnull final IBlockReader provider, @Nonnull final BlockState state,
                              @Nonnull final BlockPos pos, @Nonnull final Random random) {
        if (!alwaysExecute() && random.nextInt(getChance()) != 0)
            return false;

        return ConditionEvaluator.INSTANCE.check(getConditions());
    }

    /**
     * Override to provide the body of the effect that is to take place.
     */
    public abstract void doEffect(@Nonnull final IBlockReader provider, @Nonnull final BlockState state,
                                  @Nonnull final BlockPos pos, @Nonnull final Random random);

    @Override
    @Nonnull
    public String toString() {
        return "type: " + getEffectType().getName() +
                " conditions: [" + getConditions() + ']' +
                "; chance:" + getChance() +
                ' ' + this.getClass().getSimpleName();
    }
}
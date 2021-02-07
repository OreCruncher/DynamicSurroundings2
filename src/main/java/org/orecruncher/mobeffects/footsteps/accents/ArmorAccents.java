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

package org.orecruncher.mobeffects.footsteps.accents;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.mobeffects.config.Config;
import org.orecruncher.mobeffects.library.ItemData;
import org.orecruncher.mobeffects.library.ItemLibrary;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
class ArmorAccents implements IFootstepAccentProvider {

    @Nullable
    protected IAcoustic resolveArmor(@Nonnull final ItemStack stack) {
        final ItemData id = ItemLibrary.getItemData(stack);
        if (id.isArmor()) {
            return ((ItemData.ArmorItemData) id).getArmorSound(stack);
        }
        return null;
    }

    protected IAcoustic resolveFootArmor(@Nonnull final ItemStack stack) {
        final ItemData id = ItemLibrary.getItemData(stack);
        if (id.isArmor()) {
            return ((ItemData.ArmorItemData) id).getFootArmorSound(stack);
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        return Config.CLIENT.footsteps.enableArmorAccents.get();
    }

    @Override
    public void provide(
            @Nonnull final LivingEntity entity,
            @Nonnull final BlockPos blockPos,
            @Nonnull final BlockState posState,
            @Nonnull final ObjectArray<IAcoustic> acoustics) {
        final ItemStack armor = ItemData.effectiveArmorItemStack(entity);
        final ItemStack foot = ItemData.footArmorItemStack(entity);
        final IAcoustic armorAddon = resolveArmor(armor);
        IAcoustic footAddon = resolveFootArmor(foot);

        if (armorAddon != null) {
            acoustics.add(armorAddon);
            if (armorAddon == footAddon)
                footAddon = null;
        }

        if (footAddon != null)
            acoustics.add(footAddon);
    }

}
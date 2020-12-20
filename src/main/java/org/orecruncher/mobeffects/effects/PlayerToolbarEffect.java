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

package org.orecruncher.mobeffects.effects;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.library.ItemData;
import org.orecruncher.mobeffects.library.ItemLibrary;
import org.orecruncher.sndctrl.api.effects.AbstractEntityEffect;
import org.orecruncher.sndctrl.api.effects.IEntityEffectManager;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class PlayerToolbarEffect extends AbstractEntityEffect {

    private static final ResourceLocation NAME = new ResourceLocation(MobEffects.MOD_ID, "toolbar");
    public static final FactoryHandler FACTORY = new FactoryHandler(
            PlayerToolbarEffect.NAME,
            entity -> new PlayerToolbarEffect());

    protected static class HandTracker {

        protected final IEntityEffectManager manager;
        protected final Hand hand;
        protected Item lastHeld;

        protected HandTracker(@Nonnull final IEntityEffectManager manager, @Nonnull final PlayerEntity player) {
            this(manager, player, Hand.OFF_HAND);
        }

        protected HandTracker(@Nonnull final IEntityEffectManager manager, @Nonnull final PlayerEntity player, @Nonnull final Hand hand) {
            this.manager = manager;
            this.hand = hand;
            this.lastHeld = getItemForHand(player, hand);
        }

        protected Item getItemForHand(final PlayerEntity player, final Hand hand) {
            final ItemStack stack = player.getHeldItem(hand);
            return stack.getItem();
        }

        protected boolean triggerNewEquipSound(@Nonnull final PlayerEntity player) {
            final Item heldItem = getItemForHand(player, this.hand);
            return heldItem != this.lastHeld;
        }

        public void update(@Nonnull final PlayerEntity player) {
            if (triggerNewEquipSound(player)) {
                final ItemStack currentStack = player.getHeldItem(this.hand);
                if (!currentStack.isEmpty()) {
                    final ItemData data = ItemLibrary.getItemData(currentStack);
                    if (this.manager.isActivePlayer(player))
                        data.playEquipSound();
                    else
                        data.playEquipSound(player.getPosition());
                }
                this.lastHeld = currentStack.getItem();
            }
        }
    }

    protected static class MainHandTracker extends HandTracker {

        protected int lastSlot;

        public MainHandTracker(@Nonnull final IEntityEffectManager manager, @Nonnull final PlayerEntity player) {
            super(manager, player, Hand.MAIN_HAND);
            this.lastSlot = player.inventory.currentItem;
        }

        @Override
        protected boolean triggerNewEquipSound(@Nonnull final PlayerEntity player) {
            return this.lastSlot != player.inventory.currentItem || super.triggerNewEquipSound(player);
        }

        @Override
        public void update(@Nonnull final PlayerEntity player) {
            super.update(player);
            this.lastSlot = player.inventory.currentItem;
        }
    }

    protected MainHandTracker mainHand;
    protected HandTracker offHand;

    public PlayerToolbarEffect() {
        super(NAME);
    }

    public void intitialize(@Nonnull final IEntityEffectManager manager) {
        super.intitialize(manager);
        final PlayerEntity player = (PlayerEntity) getEntity();
        this.mainHand = new MainHandTracker(manager, player);
        this.offHand = new HandTracker(manager, player);
    }

    @Override
    public void update() {
        final PlayerEntity player = (PlayerEntity) getEntity();
        this.mainHand.update(player);
        this.offHand.update(player);
    }

}
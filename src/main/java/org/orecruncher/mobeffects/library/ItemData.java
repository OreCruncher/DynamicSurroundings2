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

package org.orecruncher.mobeffects.library;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.IAcousticFactory;
import org.orecruncher.sndctrl.api.acoustics.Library;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.audio.PlayerCenteredSoundInstance;
import org.orecruncher.sndctrl.audio.acoustic.NullAcoustic;
import org.orecruncher.sndctrl.library.Primitives;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class ItemData {

    private static final int ACOUSTIC_TYPE_SWING = 0;
    private static final int ACOUSTIC_TYPE_USE = 1;
    private static final int ACOUSTIC_TYPE_EQUIP = 2;
    private static final int ACOUSTIC_TYPE_ARMOR = 3;
    private static final int ACOUSTIC_TYPE_ARMOR_FOOT = 4;

    private static final float OTHER_PLAYER_VOLUME_SCALE = 0.75F;

    private final String name;
    private final ResourceLocation swing;
    private final ResourceLocation use;
    private final ResourceLocation equip;

    protected final IAcoustic[] acoustics = new IAcoustic[5];

    ItemData(@Nonnull final String name, @Nonnull final ResourceLocation sound) {
        this(name, sound, sound, sound);
    }

    ItemData(@Nonnull final String name, @Nonnull final ResourceLocation swing, @Nonnull final ResourceLocation use,
             @Nonnull final ResourceLocation equip) {
        this.name = name;
        this.swing = swing;
        this.use = use;
        this.equip = equip;
    }

    private IAcoustic resolveToolbarAcoustic(final int type, @Nonnull final ResourceLocation loc) {
        IAcoustic acoustic = this.acoustics[type];
        if (acoustic == null) {
            acoustic = Library.resolve(loc);
            if (acoustic == NullAcoustic.INSTANCE) {
                acoustic = Primitives.getSound(loc, Constants.TOOLBAR);
            }
            this.acoustics[type] = acoustic;
        }
        return acoustic;
    }

    /**
     * Determines the effective armor class of the Entity. Chest and legs are used
     * to make the determination.
     */
    public static ItemStack effectiveArmorItemStack(@Nonnull final LivingEntity entity) {
        final ItemStack chest = entity.getItemStackFromSlot(EquipmentSlotType.CHEST);
        final ItemStack legs = entity.getItemStackFromSlot(EquipmentSlotType.LEGS);
        final ItemData chestItemData = ItemLibrary.getItemData(chest);
        final ItemData legsItemData = ItemLibrary.getItemData(legs);
        final int chestPriority = chestItemData.isArmor() ? ((ArmorItemData) chestItemData).getPriority() : -1;
        final int legPriority = legsItemData.isArmor() ? ((ArmorItemData) legsItemData).getPriority() : -1;
        return chestPriority > legPriority ? chest.copy() : legs.copy();
    }

    /**
     * Gets the armor class of the entities feet.
     */
    public static ItemStack footArmorItemStack(@Nonnull final LivingEntity entity) {
        return entity.getItemStackFromSlot(EquipmentSlotType.FEET);
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    public void playSwingSound() {
        playSwingSound(null);
    }

    public void playSwingSound(@Nullable final BlockPos pos) {
        play(ACOUSTIC_TYPE_SWING, this.swing, pos);
    }

    public void playUseSound() {
        playUseSound(null);
    }

    public void playUseSound(@Nullable final BlockPos pos) {
        play(ACOUSTIC_TYPE_USE, this.use, pos);
    }

    public void playEquipSound() {
        playEquipSound(null);
    }

    public void playEquipSound(@Nullable final BlockPos pos) {
        play(ACOUSTIC_TYPE_EQUIP, this.equip, pos);
    }

    public boolean isArmor() {
        return this instanceof ArmorItemData;
    }

    protected void play(final int type, @Nonnull final ResourceLocation loc, @Nullable final BlockPos pos) {
        IAcoustic acoustic = resolveToolbarAcoustic(type, loc);
        final IAcousticFactory factory = acoustic.getFactory(Constants.WALK);
        if (factory != null) {
            final ISoundInstance sound;
            if (pos == null) {
                sound = new PlayerCenteredSoundInstance(factory.createSound(), Constants.TOOLBAR);
            } else {
                sound = factory.createSoundAt(pos);
                sound.scaleVolume(OTHER_PLAYER_VOLUME_SCALE);
            }
            AudioEngine.play(sound);
        }
    }

    public static class ArmorItemData extends ItemData {

        private final ResourceLocation armor;
        private final ResourceLocation foot;
        private final int priority;

        ArmorItemData(@Nonnull final String name, @Nonnull final ResourceLocation item, @Nonnull final ResourceLocation armor, @Nonnull final ResourceLocation foot, final int priority) {
            super(name, item);
            this.armor = armor;
            this.foot = foot;
            this.priority = priority;
        }

        @Nonnull
        public IAcoustic getArmorSound(@Nonnull final ItemStack stack) {
            return resolveArmorAcoustic(ACOUSTIC_TYPE_ARMOR, this.armor, stack);
        }

        @Nonnull
        public IAcoustic getFootArmorSound(@Nonnull final ItemStack stack) {
            return resolveArmorAcoustic(ACOUSTIC_TYPE_ARMOR_FOOT, this.foot, stack);
        }

        public int getPriority() {
            return this.priority;
        }

        private IAcoustic resolveArmorAcoustic(final int type, @Nonnull final ResourceLocation loc, @Nonnull final ItemStack stack) {
            IAcoustic acoustic = NullAcoustic.INSTANCE;
            final Item item = stack.getItem();
            if (item instanceof ArmorItem) {
                acoustic = this.acoustics[type];
                if (acoustic == null) {
                    acoustic = Library.resolve(loc);
                    if (acoustic == NullAcoustic.INSTANCE) {
                        acoustic = Primitives.getArmorAccentAcoustic(((ArmorItem) item).getArmorMaterial());
                    }
                    this.acoustics[type] = acoustic;
                }
            }
            return acoustic;
        }
    }

}

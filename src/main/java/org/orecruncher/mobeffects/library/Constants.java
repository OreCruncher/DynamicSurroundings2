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

package org.orecruncher.mobeffects.library;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.mobeffects.Config;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.sndctrl.api.acoustics.AcousticEvent;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.audio.acoustic.NullAcoustic;

@OnlyIn(Dist.CLIENT)
public final class Constants {
    private Constants() {

    }

    public static final ISoundCategory FOOTSTEPS = new Category("footsteps", Config.CLIENT.footsteps::get_footstepVolumeScale);
    public static final ISoundCategory TOOLBAR = new Category("toolbar", Config.CLIENT.effects::get_toolbarVolumeScale);

    public static final ResourceLocation NONE = new ResourceLocation(MobEffects.MOD_ID, "empty");

    public static final IAcoustic EMPTY = new NullAcoustic(NONE);
    public static final IAcoustic NOT_EMITTER = new NullAcoustic(new ResourceLocation(MobEffects.MOD_ID,"not_emitter"));
    public static final IAcoustic MESSY_GROUND = new NullAcoustic(new ResourceLocation(MobEffects.MOD_ID,"messy_ground"));

    public static final AcousticEvent WALK = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "walk"), null);
    public static final AcousticEvent WANDER = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "wander"), null);
    public static final AcousticEvent SWIM = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "swim"), null);
    public static final AcousticEvent RUN = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "run"), WALK);
    public static final AcousticEvent JUMP = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "jump"), WANDER);
    public static final AcousticEvent LAND = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "land"), RUN);
    public static final AcousticEvent CLIMB = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "climb"), WALK);
    public static final AcousticEvent CLIMB_RUN = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "climb_run"), RUN);
    public static final AcousticEvent DOWN = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "down"), WALK);
    public static final AcousticEvent DOWN_RUN = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "down_run"), RUN);
    public static final AcousticEvent UP = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "up"), WALK);
    public static final AcousticEvent UP_RUN = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "up_run"), RUN);

    public static final ResourceLocation LIGHT_ARMOR = new ResourceLocation(MobEffects.MOD_ID, "armor_light");
    public static final ResourceLocation MEDIUM_ARMOR = new ResourceLocation(MobEffects.MOD_ID, "armor_medium");
    public static final ResourceLocation HEAVY_ARMOR = new ResourceLocation(MobEffects.MOD_ID, "armor_heavy");
    public static final ResourceLocation CRYSTAL_ARMOR = new ResourceLocation(MobEffects.MOD_ID, "armor_crystal");
    public static final ResourceLocation LIGHT_FOOT_ARMOR = LIGHT_ARMOR;
    public static final ResourceLocation MEDIUM_FOOT_ARMOR = new ResourceLocation(MobEffects.MOD_ID, "medium_foot");
    public static final ResourceLocation HEAVY_FOOT_ARMOR = new ResourceLocation(MobEffects.MOD_ID, "heavy_foot");
    public static final ResourceLocation CRYSTAL_FOOT_ARMOR = new ResourceLocation(MobEffects.MOD_ID, "crystal_foot");

    public static final ResourceLocation LEATHER_ARMOR_EQUIP = LIGHT_ARMOR;
    public static final ResourceLocation CHAIN_ARMOR_EQUIP = MEDIUM_ARMOR;
    public static final ResourceLocation CRYSTAL_ARMOR_EQUIP = CRYSTAL_ARMOR;
    public static final ResourceLocation PLATE_ARMOR_EQUIP = HEAVY_ARMOR;
    public static final ResourceLocation UTILITY_EQUIP = new ResourceLocation(MobEffects.MOD_ID, "utility.equip");
    public static final ResourceLocation TOOL_EQUIP = new ResourceLocation(MobEffects.MOD_ID, "tool.equip");
    public static final ResourceLocation TOOL_SWING = new ResourceLocation(MobEffects.MOD_ID, "tool.swing");
    public static final ResourceLocation SHIELD_USE = new ResourceLocation(MobEffects.MOD_ID, "shield.use");
    public static final ResourceLocation SHIELD_EQUIP = new ResourceLocation(MobEffects.MOD_ID, "shield.equip");
    public static final ResourceLocation SWORD_SWING = new ResourceLocation(MobEffects.MOD_ID, "sword.swing");
    public static final ResourceLocation SWORD_EQUIP = new ResourceLocation(MobEffects.MOD_ID, "sword.equip");
    public static final ResourceLocation AXE_SWING = new ResourceLocation(MobEffects.MOD_ID, "blunt.swing");
    public static final ResourceLocation AXE_EQUIP = new ResourceLocation(MobEffects.MOD_ID, "blunt.equip");
    public static final ResourceLocation BOW_PULL = new ResourceLocation(MobEffects.MOD_ID, "bow.pull");
    public static final ResourceLocation BOW_EQUIP = new ResourceLocation(MobEffects.MOD_ID, "bow.equip");
    public static final ResourceLocation BOOK_EQUIP = new ResourceLocation(MobEffects.MOD_ID, "pageflip");
    public static final ResourceLocation POTION_EQUIP = new ResourceLocation(MobEffects.MOD_ID, "potion.equip");

}

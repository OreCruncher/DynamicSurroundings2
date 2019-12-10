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

package org.orecruncher.sndctrl.audio;

import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.JsonUtils;
import org.orecruncher.lib.MaterialUtils;
import org.orecruncher.lib.TagUtils;
import org.orecruncher.lib.blockstate.BlockStateMatcherMap;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.sndctrl.SoundControl;

import javax.annotation.Nonnull;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class EffectRegistry {

    private static final String MATERIAL_PREFIX = "+";
    private static final String TAG_PREFIX = "#";

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(EffectRegistry.class);

    // Occlusion data
    private static final Object2FloatOpenHashMap<Material> materialOcclusion = new Object2FloatOpenHashMap<>();
    private static final BlockStateMatcherMap<Float> blockStateOcclusionMap = new BlockStateMatcherMap<>();

    // Reflection data
    private static final Object2FloatOpenHashMap<Material> materialReflect = new Object2FloatOpenHashMap<>();
    private static final BlockStateMatcherMap<Float> blockStateReflectMap = new BlockStateMatcherMap<>();

    // Lowpass Data
    private static final Object2FloatOpenHashMap<ResourceLocation> fluidCoefficient = new Object2FloatOpenHashMap<>();

    static {
        // Occlusion setup
        materialOcclusion.defaultReturnValue(-1F);
        for (final Material mat : MaterialUtils.getMaterials())
            materialOcclusion.put(mat, mat.isOpaque() ? 1F : 0.15F);
        blockStateOcclusionMap.setDefaultValue(() -> -1F);

        // Reflection setup
        materialReflect.defaultReturnValue(-1F);
        blockStateReflectMap.setDefaultValue(() -> -1F);

        // Default lowpass state is essentially normal air
        fluidCoefficient.defaultReturnValue(0);
        fluidCoefficient.put(new ResourceLocation("sndctrl:default"), 0);

        final ResourceLocation res = new ResourceLocation(SoundControl.MOD_ID, "effects.json");
        try {
            final EffectOptions options = JsonUtils.load(res, EffectOptions.class);
            // Occlusions values determine how effectively a sound gets blocked from the listener
            processOcclusions(options);
            // Refelectivity governs how much reverb will be generated when a sound bounces off it's surface
            processReflectivity(options);
            // Lowpass filter gets applied when a player head is inside the block - think fluids.
            processLowpass(options);
        } catch (@Nonnull final Throwable t) {
            LOGGER.error(t, "Unable to load %s", res.toString());
        }
    }

    /**
     * Gets the occlusion value for the given BlockState.  This value determines how effective sound is transmitted
     * through the block.  Low values indicate more efficient transmission, and higher values indicate more absorption.
     *
     * @param state BlockState to obtain the occlusion coefficient.
     * @return The coeeficient that has been configured, or default of 1 for opaque blocks, and 0.15F for non-solid.
     */
    public static float getOcclusion(@Nonnull final BlockState state) {
        Float result = blockStateOcclusionMap.get(state);
        if (result == null || result < 0) {
            result = materialOcclusion.getFloat(state.getMaterial());
            if (result < 0) {
                result = state.getMaterial().isOpaque() ? 1F : 0.15F;
            }
        }

        return result;
    }

    /**
     * Gets the reflectivity value for the given BlockState.  Low values indicates sounds are absorbed by the block,
     * whereas high values indicate the sounds bounce off the block.
     *
     * @param state BlockState to obtain the reflectivity coefficient.
     * @return The coefficient that has been configured, or the default value of 0.5 if it hasn't
     */
    public static float getReflectivity(@Nonnull final BlockState state) {
        Float result = blockStateReflectMap.get(state);
        if (result == null || result < 0)
            result = materialReflect.getFloat(state.getMaterial());
        return result < 0 ? 0.5F : result;
    }

    /**
     * Gets the low pass filter effect information for the specified resource.  Normally used for water but can
     * apply to any resource.  For example, water and lava will have a filter effect, and those would be called
     * "minecraft:water", and "minecraft:lava".
     *
     * @param res Resource to lookup
     * @return Coefficient for dampening sounds for the specified resource
     */
    public static float getFluidCoeffcient(@Nonnull final ResourceLocation res) {
        return fluidCoefficient.getFloat(res);
    }

    private static void processOcclusions(@Nonnull final EffectOptions options) {

        for (final Map.Entry<String, Float> kvp : options.occlusions.entrySet()) {
            final String name = kvp.getKey();
            if (name.startsWith(MATERIAL_PREFIX)) {
                // Material entry
                final Material mat = MaterialUtils.getMaterial(name.substring(1));
                if (mat != null) {
                    materialOcclusion.put(mat, kvp.getValue().floatValue());
                } else {
                    LOGGER.warn("Unrecognized material name: %s", name);
                }
            } else if (name.startsWith(TAG_PREFIX)) {
                // Tag entry
                final Tag<Block> tag = TagUtils.getBlockTag(name.substring(1));
                if (tag != null) {
                    for (final Block block : tag.getAllElements()) {
                        for (final BlockState state : block.getStateContainer().getValidStates())
                            blockStateOcclusionMap.put(state, kvp.getValue());
                    }
                } else {
                    LOGGER.warn("Unrecognized block tag: %s", name);
                }
            } else {
                try {
                    blockStateOcclusionMap.put(name, kvp.getValue());
                } catch (@Nonnull final Throwable t) {
                    LOGGER.error(t, "Unable to insert entry into block state map: %s", t.getMessage());
                }
            }
        }

        // Air never occludes
        blockStateOcclusionMap.put(Blocks.AIR, 0F);
        blockStateOcclusionMap.put(Blocks.CAVE_AIR, 0F);
        blockStateOcclusionMap.put(Blocks.VOID_AIR, 0F);
    }

    private static void processReflectivity(@Nonnull final EffectOptions options) {

        for (final Map.Entry<String, Float> kvp : options.refectitivity.entrySet()) {
            final String name = kvp.getKey();
            final float val = MathStuff.clamp(kvp.getValue(), 0, 2F);
            if (name.startsWith(MATERIAL_PREFIX)) {
                // Material entry
                final Material mat = MaterialUtils.getMaterial(name.substring(1));
                if (mat != null) {
                    materialReflect.put(mat, val);
                } else {
                    LOGGER.warn("Unrecognized material name: %s", name);
                }
            } else if (name.startsWith(TAG_PREFIX)) {
                // Tag entry
                final Tag<Block> tag = TagUtils.getBlockTag(name.substring(1));
                if (tag != null) {
                    for (final Block block : tag.getAllElements()) {
                        for (final BlockState state : block.getStateContainer().getValidStates())
                            blockStateReflectMap.put(state, val);
                    }
                } else {
                    LOGGER.warn("Unrecognized block tag: %s", name);
                }
            } else {
                try {
                    blockStateReflectMap.put(name, val);
                } catch (@Nonnull final Throwable t) {
                    LOGGER.error(t, "Unable to insert entry into block state map: %s", t.getMessage());
                }
            }
        }

        // Air never reflects
        blockStateReflectMap.put(Blocks.AIR, 0F);
        blockStateReflectMap.put(Blocks.CAVE_AIR, 0F);
        blockStateReflectMap.put(Blocks.VOID_AIR, 0F);
    }

    private static void processLowpass(@Nonnull final EffectOptions options) {
        for (final Map.Entry<String, Float> kvp : options.fluid.entrySet()) {
            fluidCoefficient.put(new ResourceLocation(kvp.getKey()), kvp.getValue().floatValue());
        }
    }

    public static class EffectOptions {
        @SerializedName("occlusions")
        public Map<String, Float> occlusions = ImmutableMap.of();
        @SerializedName("reflectivity")
        public Map<String, Float> refectitivity = ImmutableMap.of();
        @SerializedName("fluid")
        public Map<String, Float> fluid = ImmutableMap.of();
    }

}

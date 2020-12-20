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

package org.orecruncher.environs.library;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.effects.BlockEffectType;
import org.orecruncher.environs.library.config.AcousticConfig;
import org.orecruncher.environs.library.config.BlockConfig;
import org.orecruncher.environs.library.config.EffectConfig;
import org.orecruncher.environs.library.config.ModConfig;
import org.orecruncher.lib.TagUtils;
import org.orecruncher.lib.blockstate.BlockStateMatcher;
import org.orecruncher.lib.blockstate.BlockStateMatcherMap;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.Library;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public final class BlockStateLibrary {

    private static final String TAG_SPECIFIER = "#";

    private static final IModLog LOGGER = Environs.LOGGER.createChild(BlockStateLibrary.class);
    private static final BlockStateMatcherMap<BlockStateData> registry = new BlockStateMatcherMap<>();

    private BlockStateLibrary() {

    }

    static void initialize() {
        ForgeUtils.getBlockStates().forEach(state -> BlockStateUtil.setData(state, null));
        BlockStateUtil.setData(Blocks.AIR.getDefaultState(), BlockStateData.DEFAULT);
        BlockStateUtil.setData(Blocks.CAVE_AIR.getDefaultState(), BlockStateData.DEFAULT);
        BlockStateUtil.setData(Blocks.VOID_AIR.getDefaultState(), BlockStateData.DEFAULT);
    }

    static void initFromConfig(@Nonnull final ModConfig config) {
        config.blocks.forEach(BlockStateLibrary::register);
    }

    static void complete() {
        final int blockStates = (int) ForgeUtils.getBlockStates().stream().map(BlockStateUtil::getData).count();
        LOGGER.info("%d block states processed, %d registry entries", blockStates, registry.size());
        ForgeUtils.getBlockStates().stream().map(BlockStateUtil::getData).forEach(BlockStateData::trim);
    }

    @Nonnull
    static BlockStateData get(@Nonnull final BlockState state) {
        BlockStateData profile = registry.get(state);
        return profile == null ? BlockStateData.DEFAULT : profile;
    }

    @Nonnull
    private static BlockStateData getOrCreateProfile(@Nonnull final BlockStateMatcher info) {
        BlockStateData profile = registry.get(info);
        if (profile == null) {
            profile = new BlockStateData();
            registry.put(info, profile);
        }

        return profile;
    }

    private static void register(@Nonnull final BlockConfig entry) {
        if (entry.blocks.isEmpty())
            return;

        for (final String blockName : entry.blocks) {
            final Collection<BlockStateMatcher> list = expand(blockName);

            for (final BlockStateMatcher blockInfo : list) {
                final BlockStateData blockData = getOrCreateProfile(blockInfo);

                // Reset of a block clears all registry
                if (entry.soundReset != null && entry.soundReset)
                    blockData.clearSounds();
                if (entry.effectReset != null && entry.effectReset)
                    blockData.clearEffects();

                if (entry.chance != null)
                    blockData.setChance(entry.chance);

                for (final AcousticConfig sr : entry.acoustics) {
                    if (sr.acoustic != null) {
                        final ResourceLocation res = Library.resolveResource(Environs.MOD_ID, sr.acoustic);
                        final IAcoustic acoustic = Library.resolve(res, sr.acoustic);
                        final int weight = sr.weight;
                        final WeightedAcousticEntry acousticEntry = new WeightedAcousticEntry(acoustic, sr.conditions, weight);
                        blockData.addSound(acousticEntry);
                    }
                }

                for (final EffectConfig e : entry.effects) {
                    if (StringUtils.isEmpty(e.effect))
                        continue;
                    final BlockEffectType type = BlockEffectType.get(e.effect);
                    if (type == BlockEffectType.UNKNOWN) {
                        LOGGER.warn("Unknown block effect type in configuration: [%s]", e.effect);
                    } else if (type.isEnabled()) {
                        final int chance = e.chance != null ? e.chance : 100;
                        type.getInstance(chance).ifPresent(
                                be -> {
                                    if (e.conditions != null)
                                        be.setConditions(e.conditions);
                                    blockData.addEffect(be);
                                });
                    }
                }
            }
        }
    }

    private static Collection<BlockStateMatcher> expand(@Nonnull final String blockName) {
        if (blockName.startsWith(TAG_SPECIFIER)) {
            final String tagName = blockName.substring(1);
            final Tag<Block> tag = TagUtils.getBlockTag(tagName);
            if (tag != null) {
                return tag.getAllElements().stream().map(BlockStateMatcher::create).filter(m -> !m.isEmpty()).collect(Collectors.toList());
            }
            LOGGER.warn("Unknown block tag '%s' in Block specification", tagName);
        } else {
            final BlockStateMatcher matcher = BlockStateMatcher.create(blockName);
            if (!matcher.isEmpty()) {
                return ImmutableList.of(matcher);
            }
            LOGGER.warn("Unknown block name '%s' in Block Specification", blockName);
        }
        return ImmutableList.of();
    }

}

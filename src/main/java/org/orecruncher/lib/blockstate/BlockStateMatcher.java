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

package org.orecruncher.lib.blockstate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.lib.Lib;
import org.orecruncher.lib.blockstate.BlockStateParser.ParseResult;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class BlockStateMatcher {

    public static final BlockStateMatcher AIR = new BlockStateMatcher(Blocks.AIR.getDefaultState());

    // All instances will have this defined
    @Nonnull
    protected final Block block;

    // Sometimes an exact match of state is needed. The state being compared
    // would have to match all these properties.
    @Nonnull
    private final BlockStateProperties props;

    BlockStateMatcher(@Nonnull final BlockState state) {
        this(state.getBlock(), state.getValues());
    }

    BlockStateMatcher(@Nonnull final Block block) {
        this.block = block;
        this.props = BlockStateProperties.NONE;
    }

    BlockStateMatcher(@Nonnull final Block block,
                              @Nonnull final Map<IProperty<?>, Comparable<?>> props) {
        this.block = block;
        this.props = props.size() > 0 ? new BlockStateProperties(props) : BlockStateProperties.NONE;
    }

    @Nonnull
    public static BlockStateMatcher asGeneric(@Nonnull final BlockState state) {
        return new BlockStateMatcher(state.getBlock());
    }

    @Nonnull
    public static BlockStateMatcher create(@Nonnull final BlockState state) {
        return new BlockStateMatcher(state);
    }

    @Nonnull
    public static BlockStateMatcher create(@Nonnull final Block block) {
        return new BlockStateMatcher(block);
    }

    @Nonnull
    public static BlockStateMatcher create(@Nonnull final String blockId) {
        return BlockStateParser.parseBlockState(blockId).map(BlockStateMatcher::create).orElse(BlockStateMatcher.AIR);
    }

    @Nonnull
    public static BlockStateMatcher create(@Nonnull final ParseResult result) {
        final Block block = result.getBlock();
        final BlockState defaultState = block.getDefaultState();
        final StateContainer<Block, BlockState> container = block.getStateContainer();
        if (container.getValidStates().size() == 1) {
            // Easy case - it's always an identical match because there are no other properties
            return new BlockStateMatcher(defaultState);
        }

        if (!result.hasProperties()) {
            // No property specification so this is a generic
            return new BlockStateMatcher(block);
        }

        final Map<String, String> properties = result.getProperties();
        final Map<IProperty<?>, Comparable<?>> props = new IdentityHashMap<>(properties.size());

        // Blow out the property list
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            final String s = entry.getKey();
            final IProperty<?> prop = container.getProperty(s);
            if (prop != null) {
                final Optional<?> optional = prop.parseValue(entry.getValue());
                if (optional.isPresent()) {
                    props.put(prop, (Comparable<?>) optional.get());
                } else {
                    final String allowed = getAllowedValues(block, s);
                    Lib.LOGGER.warn("Property value '%s' for property '%s' not found for block '%s'",
                            entry.getValue(), s, result.getBlockName());
                    Lib.LOGGER.warn("Allowed values: %s", allowed);
                }
            } else {
                Lib.LOGGER.warn("Property %s not found for block %s", s, result.getBlockName());
            }
        }

        return new BlockStateMatcher(defaultState.getBlock(), props);
    }

    @Nonnull
    private static <T extends Comparable<T>> String getAllowedValues(@Nonnull final Block block, @Nonnull final String propName) {
        @SuppressWarnings("unchecked")
        final IProperty<T> prop = (IProperty<T>) block.getStateContainer().getProperty(propName);
        if (prop != null) {
            return prop.getAllowedValues().stream().map(prop::getName).collect(Collectors.joining(","));
        }
        return "Invalid property " + propName;
    }

    public boolean isEmpty() {
        return this.block == Blocks.AIR || this.block == Blocks.CAVE_AIR || this.block == Blocks.VOID_AIR;
    }

    @Nonnull
    public Block getBlock() {
        return this.block;
    }

    @Override
    public int hashCode() {
        // Only do the block hash code.  Reason is that BlockStateMatcher does not honor the equality contract set
        // forth by Object.  Equals can perform a partial match.
        return this.block.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof BlockStateMatcher) {
            final BlockStateMatcher m = (BlockStateMatcher) obj;
            return this.block == m.block && m.props.matches(this.props);
        }
        return false;
    }

    @Override
    @Nonnull
    public String toString() {
        return ForgeRegistries.BLOCKS.getKey(this.block) + this.props.getFormattedProperties();
    }

}

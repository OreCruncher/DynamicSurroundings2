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

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.lib.BlockNameUtil;
import org.orecruncher.lib.BlockNameUtil.NameResult;
import org.orecruncher.lib.Lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class BlockStateMatcher {

    private static final ImmutableMap<IProperty<?>, Object> EMPTY = ImmutableMap.of();

    // All instances will have this defined
    @Nonnull
    protected final Block block;

    // Sometimes an exact match of state is needed. The state being compared
    // would have to match all these properties.
    @Nonnull
    private final ImmutableMap<IProperty<?>, Object> props;

    private BlockStateMatcher(@Nonnull final BlockState state) {
        this.block = state.getBlock();
        this.props = getPropsFromState(state);
    }

    private BlockStateMatcher(@Nonnull final Block block) {
        this(block, EMPTY);
    }

    private BlockStateMatcher(@Nonnull final Block block,
                              @Nonnull final ImmutableMap<IProperty<?>, Object> props) {
        this.block = block;
        this.props = props;
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

    @Nullable
    public static BlockStateMatcher create(@Nonnull final String blockId) {
        return BlockNameUtil.parseBlockName(blockId).map(BlockStateMatcher::create).orElse(null);
    }

    @Nonnull
    public static BlockStateMatcher create(@Nonnull final NameResult result) {
        final Block block = result.getBlock();
        final BlockState defaultState = block.getDefaultState();
        final StateContainer<Block, BlockState> container = block.getStateContainer();
        if (container.getValidStates().size() == 1) {
            // Easy case - it's always an identical match because there are no other
            // properties
            return new BlockStateMatcher(defaultState);
        }

        if (!result.hasProperties()) {
            // No NBT specification so this is a generic
            return new BlockStateMatcher(block);
        }

        final Map<String, String> properties = result.getProperties();
        final Map<IProperty<?>, Object> props = new IdentityHashMap<>(properties.size());

        // Blow out the property list
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            final String s = entry.getKey();
            final IProperty<?> prop = container.getProperty(s);
            if (prop != null) {
                final Optional<?> optional = prop.parseValue(entry.getValue());
                if (optional.isPresent()) {
                    props.put(prop, optional.get());
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

        // If we have properties it will be a partial generic type
        // match. Otherwise it will be an exact match on the default
        // state.
        if (props.size() > 0) {
            return new BlockStateMatcher(defaultState.getBlock(), ImmutableMap.copyOf(props));
        } else {
            return new BlockStateMatcher(defaultState);
        }

    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getValue(@Nonnull final Block block,
                                                             @Nonnull final String propName, @Nonnull final Object val) {
        final StateContainer<Block, BlockState> container = block.getStateContainer();
        final IProperty<T> prop = (IProperty<T>) container.getProperty(propName);
        assert prop != null;
        return prop.getName((T) val);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getAllowedValues(final Block block, final String propName) {
        final List<String> result = new ArrayList<>();
        final StateContainer<Block, BlockState> container = block.getStateContainer();
        final IProperty<T> prop = (IProperty<T>) container.getProperty(propName);
        if (prop != null) {
            final Collection<T> vals = prop.getAllowedValues();
            for (final T v : vals) {
                result.add(prop.getName(v));
            }
        }
        return String.join(",", result);
    }

    @Nonnull
    public Block getBlock() {
        return this.block;
    }

    @Nonnull
    public List<BlockState> getMatchingBlockStates() {
        //@formatter:off
        return this.block.getStateContainer().getValidStates().stream()
                .filter(this::matchProps)
                .collect(Collectors.toList());
        //@formatter:on
    }

    private boolean matchProps(@Nonnull final BlockState state) {
        if (this.props.isEmpty())
            return true;
        for (final Map.Entry<IProperty<?>, Object> entry : this.props.entrySet()) {
            final Object result = state.get(entry.getKey());
            if (!entry.getValue().equals(result))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.block.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof BlockStateMatcher) {
            final BlockStateMatcher m = (BlockStateMatcher) obj;
            // If the block types don't match, there will be no match
            if (this.block != m.block)
                return false;

            // If both lists are empty its a match
            if (this.props.isEmpty() && m.props.isEmpty())
                return true;

            // If the other list is larger there isn't a way it's going
            // to match us.
            if (this.props.size() < m.props.size())
                return false;

            // Run 'em down doing compares
            for (final Map.Entry<IProperty<?>, Object> entry : m.props.entrySet()) {
                final Object v = this.props.get(entry.getKey());
                if (v == null || !v.equals(entry.getValue()))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Nonnull
    private ImmutableMap<IProperty<?>, Object> getPropsFromState(@Nonnull final BlockState state) {
        final ImmutableMap<IProperty<?>, Comparable<?>> source = state.getValues();
        return source.size() == 0 ? EMPTY : ImmutableMap.copyOf(source);
    }

    @Override
    @Nonnull
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(ForgeRegistries.BLOCKS.getKey(this.block));
        if (!this.props.isEmpty()) {
            final String txt = this.props.entrySet().stream()
                    .map(e -> e.getKey().getName() + "=" + getValue(this.block, e.getKey().getName(), e.getValue()))
                    .collect(Collectors.joining(","));
            builder.append('[').append(txt).append(']');
        }

        return builder.toString();
    }

}

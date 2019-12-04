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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Special Map implementation that is implemented with BlockStateMatcher as a key type.  It handles any special
 * processing that may occur because of the fuzzyness of BlockState matching.
 *
 * @param <T> Value type of the Map
 */
public final class BlockStateMatcherMap<T> implements Map<BlockStateMatcher, T> {

    private final Object2ObjectOpenHashMap<BlockStateMatcher, T> map = new Object2ObjectOpenHashMap<>();
    @Nullable
    private Supplier<T> defaultValue;

    @Nullable
    public T get(@Nonnull final BlockState state) {
        T result = this.map.get(BlockStateMatcher.create(state));
        if (result == null)
            result = this.map.get(BlockStateMatcher.asGeneric(state));
        return result != null ? result : this.defaultValue != null ? this.defaultValue.get() : null;
    }

    public void setDefaultValue(@Nullable final Supplier<T> s) {
        this.defaultValue = s;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    @Nullable
    public T get(Object key) {
        return get((BlockState) key);
    }

    @Nullable
    public T put(@Nonnull final String block, @Nonnull final T val) {
        final BlockStateMatcher matcher = BlockStateMatcher.create(block);
        Objects.requireNonNull(matcher, String.format("Invalid block name '%s'", block));
        return put(matcher, val);
    }

    @Nullable
    public T put(@Nonnull final BlockState state, @Nonnull final T val) {
        final BlockStateMatcher matcher = BlockStateMatcher.create(state);
        Objects.requireNonNull(matcher, String.format("Invalid block state '%s'", state.toString()));
        return put(matcher, val);
    }

    @Nullable
    public T put(@Nonnull final Block block, @Nonnull final T val) {
        final BlockStateMatcher matcher = BlockStateMatcher.create(block);
        Objects.requireNonNull(matcher, String.format("Invalid block '%s'", block.toString()));
        return put(matcher, val);
    }

    @Override
    @Nullable
    public T put(@Nonnull final BlockStateMatcher matcher, @Nonnull final T val) {
        return this.map.put(matcher, val);
    }

    @Override
    @Nullable
    public T remove(Object key) {
        return this.map.remove(key);
    }

    @Override
    public void putAll(Map<? extends BlockStateMatcher, ? extends T> m) {
        this.map.putAll(m);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    @Nonnull
    public Set<BlockStateMatcher> keySet() {
        return this.map.keySet();
    }

    @Override
    @Nonnull
    public Collection<T> values() {
        return this.map.values();
    }

    @Override
    @Nonnull
    public Set<Entry<BlockStateMatcher, T>> entrySet() {
        return this.map.entrySet();
    }
}

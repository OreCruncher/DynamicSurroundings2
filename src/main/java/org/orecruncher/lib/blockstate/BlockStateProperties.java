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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.Lib;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Special property collection that can be used to perform fuzzy matching against other
 * property collections.  Used for partial matching.
 */
public class BlockStateProperties {

    public static final BlockStateProperties NONE = new BlockStateProperties();

    private final Map<Property<?>, Comparable<?>> props;

    private BlockStateProperties() {
        this.props = ImmutableMap.of();
    }

    public BlockStateProperties(@Nonnull final BlockState state) {
        this(state.getValues());
    }

    public BlockStateProperties(@Nonnull final Map<Property<?>, Comparable<?>> props) {
        this.props = props;
    }

    /**
     * Determines if the property values of this collection are a subset of the properties of the provided
     * BlockState value.
     *
     * @param state The BlockState that is to be evaluated
     * @return true if all the property values in the collection match the BlockState; false otherwise
     */
    public boolean matches(@Nonnull final BlockState state) {
        try {
            for (final Map.Entry<Property<?>, Comparable<?>> kvp : this.props.entrySet()) {
                final Comparable<?> comp = state.get(kvp.getKey());
                if (!comp.equals(kvp.getValue()))
                    return false;
            }
            return true;
        } catch (@Nonnull final Throwable ignored) {
            // A property in this list does not exist in the target list.  This is highly unsual because it is
            // expected that this list is a subset of what could be found in a blockstate for the same block instance.
            Lib.LOGGER.warn("Property list %s does not correspond the properties in %s", this.toString(), new BlockStateMatcher(state).toString());
        }
        return false;
    }

    /**
     * Determines if the property values are a subset of the values specifed in the target BlockStateProperties
     * collection.
     *
     * @param props Target BlockStateProperties collection to evaluate
     * @return true if all the property values in the collection match BlockStateProperties; false otherwise
     */
    public boolean matches(@Nonnull final BlockStateProperties props) {
        return matches(props.props);
    }

    /**
     * Determines if the property values are a subset of the specified properties map.
     *
     * @param m Property map to evaluate
     * @return true if all the property values in the collection are present in the map; false otherwise
     */
    public boolean matches(@Nonnull final Map<Property<?>, Comparable<?>> m) {
        try {
            if (this.props == m)
                return true;
            if (this.props.size() > m.size())
                return false;
            for (final Map.Entry<Property<?>, Comparable<?>> kvp : this.props.entrySet()) {
                final Comparable<?> comp = m.get(kvp.getKey());
                if (!comp.equals(kvp.getValue()))
                    return false;
            }
            return true;
        } catch (@Nonnull final Throwable ignored) {
            // This is probable in that a property in this list does not exist in the target list.  Can happen if
            // the two lists are for fuzzy matching against blockstate and the sets are disjoint.
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 0;
        for (final Map.Entry<Property<?>, Comparable<?>> kvp : this.props.entrySet()) {
            code = code * 31 + kvp.getKey().hashCode();
            code = code * 31 + kvp.getValue().hashCode();
        }
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof BlockStateProperties))
            return false;
        final BlockStateProperties e = (BlockStateProperties) obj;
        return this.props.size() == e.props.size() && matches(e.props);
    }

    @Nonnull
    public String getFormattedProperties() {
        if (this.props.size() == 0)
            return StringUtils.EMPTY;
        final String txt = this.props.entrySet().stream()
                .map(kvp -> kvp.getKey().getName() + "=" + kvp.getValue().toString())
                .collect(Collectors.joining(","));
        return "[" + txt + "]";
    }

    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(getFormattedProperties()).toString();
    }

}

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
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.Lib;
import org.orecruncher.lib.logging.IModLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility functions for parsing and handling string based block names.  Handles things like properties, both fully
 * and partially described.
 */
@SuppressWarnings("unused")
public final class BlockStateParser {

    private static final IModLog LOGGER = Lib.LOGGER;

    private BlockStateParser() {

    }

    /**
     * Parses the block state string passed in and returns the result of that parsing.  If null is returned it means
     * there was some sort of error.
     */
    @Nonnull
    public static Optional<ParseResult> parseBlockState(@Nonnull final String blockName) {

        String temp = blockName;
        int idx = temp.indexOf('+');

        String extras = null;

        if (idx > 0) {
            extras = temp.substring(idx + 1);
            temp = temp.substring(0, idx);
        }

        Map<String, String> properties = ImmutableMap.of();

        idx = temp.indexOf('[');
        if (idx > 0) {
            try {
                int end = temp.indexOf(']');
                String propString = temp.substring(idx + 1, end);
                properties = Arrays.stream(propString.split(","))
                        .map(elem -> elem.split("="))
                        .collect(Collectors.toMap(e -> e[0], e -> e[1]));
                temp = temp.substring(0, idx);
            } catch(@Nonnull final Throwable ignore) {
                LOGGER.warn("Unable to parse properties of '%s'", blockName);
                return Optional.empty();
            }
        }

        if (!ResourceLocation.isResouceNameValid(temp)) {
            LOGGER.warn("Invalid blockname '%s' for entry '%s'", temp, blockName);
            return Optional.empty();
        }

        final ResourceLocation resource = new ResourceLocation(temp);
        final Block block = ForgeRegistries.BLOCKS.getValue(resource);
        if (block == null || (block == Blocks.AIR && !"mincraft:air".equals(temp))) {
            LOGGER.warn("Unknown block '%s' for entry '%s'", temp, blockName);
            return Optional.empty();
        }

        return Optional.of(new ParseResult(temp, block, properties, extras));
    }

    public final static class ParseResult {

        /**
         * Name of the blockName in standard domain:path form.
         */
        @Nonnull
        private final String blockName;

        /**
         * The block from the registries
         */
        @Nonnull
        private final Block block;

        /**
         * The parsed properties after the blockName name, if present
         */
        @Nonnull
        private final Map<String, String> properties;

        /**
         * Extra information that may have been appended at the end
         */
        @Nullable
        private final String extras;

        private ParseResult(@Nonnull final String blockName, @Nonnull final Block block, @Nonnull final Map<String, String> props, @Nullable final String extras) {
            this.blockName = blockName;
            this.block = block;
            this.properties = props;
            this.extras = extras;
        }

        @Nonnull
        public String getBlockName() {
            return this.blockName;
        }

        @Nonnull
        public Block getBlock() {
            return this.block;
        }

        public boolean hasProperties() {
            return this.properties.size() > 0;
        }

        @Nonnull
        public Map<String, String> getProperties() {
            return this.properties;
        }

        public boolean hasExtras() {
            return !StringUtils.isEmpty(this.extras);
        }

        @Nullable
        public String getExtras() {
            return this.extras;
        }

        @Override
        @Nonnull
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(getBlockName());
            if (hasProperties()) {
                builder.append('[');
                final String props = getProperties()
                        .entrySet()
                        .stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining(","));
                builder.append(props);
                builder.append(']');
            }

            if (!StringUtils.isEmpty(this.extras)) {
                builder.append('+').append(this.extras);
            }

            return builder.toString();
        }
    }

}

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
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.Lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility functions for parsing and handling string based block names.  Handles things like properties, both fully
 * and partially described.
 */
@SuppressWarnings("unused")
public final class BlockStateParser {

    // https://www.regexplanet.com/advanced/java/index.html
    private static final Pattern pattern = Pattern
            .compile("([a-z0-9_.-]+:[a-z0-9/._-]+)\\[?((?:\\w+=\\w+)?(?:,\\w+=\\w+)*)]?\\+?(\\w+)?");

    private BlockStateParser() {

    }

    /**
     * Parses the block state string passed in and returns the result of that parsing.  If null is returned it means
     * there was some sort of error.
     */
    @Nonnull
    public static Optional<ParseResult> parseBlockState(@Nonnull final String blockName) {
        try {
            final Matcher matcher = pattern.matcher(blockName);
            return matcher.matches() ? Optional.of(new ParseResult(matcher)) : Optional.empty();
        } catch (final Exception ex) {
            Lib.LOGGER.error(ex, "Unable to parse '%s'", blockName);
        }
        return Optional.empty();
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

        private ParseResult(@Nonnull final Matcher matcher) {
            this.blockName = matcher.group(1);
            this.block = Objects.requireNonNull(
                    ForgeRegistries.BLOCKS.getValue(new ResourceLocation(this.blockName)),
                    String.format("The blockName '%s' is invalid", this.blockName));

            final String temp = matcher.group(2);
            if (!StringUtils.isEmpty(temp)) {
                this.properties = Arrays.stream(temp.split(","))
                        .map(elem -> elem.split("="))
                        .collect(Collectors.toMap(e -> e[0], e -> e[1]));
            } else {
                this.properties = ImmutableMap.of();
            }

            this.extras = matcher.group(3);
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
                        .map(e -> e.getKey() + '=' + e.getValue())
                        .collect(Collectors.joining(","));
                builder.append(props);
                builder.append(']');
            }
            return builder.toString();
        }
    }

}

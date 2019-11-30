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

package org.orecruncher.lib.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class ModLog implements IModLog {

    private static final Pattern REGEX_SPLIT = Pattern.compile("\\n");

    @Nonnull
    private final Marker marker;
    @Nonnull
    private final Logger logger;

    private boolean debugging;
    private int traceMask;

    public ModLog(@Nonnull final Class<?> clazz) {
        this(Objects.requireNonNull(clazz).getSimpleName());
    }

    public ModLog(@Nonnull final String name) {
        this.logger = LogManager.getLogger(Objects.requireNonNull(name));
        this.marker = MarkerManager.getMarker("MOD");
    }

    private static void outputLines(@Nullable final Marker marker, @Nonnull final ILoggit out, @Nonnull final String format, @Nullable final Object... parms) {
        for (final String l : REGEX_SPLIT.split(String.format(format, parms)))
            out.log(marker, l);
    }

    @Nonnull
    public IModLog createChild(@Nonnull final Class<?> child) {
        return new ChildLog(this, Objects.requireNonNull(child));
    }

    public void setDebug(final boolean flag) {
        this.debugging = flag;
    }

    public void setTraceMask(final int mask) {
        this.traceMask = mask;
    }

    public boolean testTrace(final int mask) {
        return (this.traceMask & mask) != 0;
    }

    public boolean isDebugging() {
        return this.debugging;
    }

    @Override
    public void info(@Nonnull final String msg, @Nullable final Object... parms) {
        info(this.marker, msg, parms);
    }

    private void info(@Nonnull final Marker marker, @Nonnull final String msg, @Nullable final Object... parms) {
        outputLines(marker, logger::info, msg, parms);
    }

    @Override
    public void info(@Nonnull final Supplier<String> message) {
        info(this.marker, message);
    }

    private void info(@Nonnull final Marker marker, @Nonnull final Supplier<String> message) {
        outputLines(marker, logger::info, message.get());
    }

    @Override
    public void warn(@Nonnull final String msg, @Nullable final Object... parms) {
        warn(this.marker, msg, parms);
    }

    private void warn(@Nonnull final Marker marker, @Nonnull final String msg, @Nullable final Object... parms) {
        outputLines(marker, logger::warn, msg, parms);
    }

    @Override
    public void warn(@Nonnull final Supplier<String> message) {
        warn(this.marker, message);
    }

    private void warn(@Nonnull final Marker marker, @Nonnull final Supplier<String> message) {
        outputLines(marker, logger::warn, message.get());
    }

    @Override
    public void debug(@Nonnull final String msg, @Nullable final Object... parms) {
        debug(this.marker, msg, parms);
    }

    private void debug(@Nonnull final Marker marker, @Nonnull final String msg, @Nullable final Object... parms) {
        if (isDebugging())
            outputLines(marker, logger::info, msg, parms);
    }

    @Override
    public void debug(@Nonnull final Supplier<String> message) {
        debug(this.marker, message);
    }

    private void debug(@Nonnull final Marker marker, @Nonnull final Supplier<String> message) {
        if (isDebugging())
            outputLines(marker, logger::info, message.get());
    }

    @Override
    public void debug(final int mask, @Nonnull final String msg, @Nullable final Object... parms) {
        debug(this.marker, msg, parms);
    }

    private void debug(@Nonnull final Marker marker, final int mask, @Nonnull final String msg, @Nullable final Object... parms) {
        if (isDebugging() && testTrace(mask))
            outputLines(marker, logger::info, msg, parms);
    }

    @Override
    public void debug(final int mask, @Nonnull final Supplier<String> message) {
        debug(this.marker, message);
    }

    private void debug(@Nonnull final Marker marker, final int mask, @Nonnull final Supplier<String> message) {
        if (isDebugging() && testTrace(mask))
            outputLines(marker, logger::info, message.get());
    }

    @Override
    public void error(@Nonnull final Throwable e, @Nonnull final String msg, @Nullable final Object... parms) {
        error(this.marker, e, msg, parms);
    }

    private void error(@Nonnull final Marker marker, @Nonnull final Throwable e, @Nonnull final String msg, @Nullable final Object... parms) {
        outputLines(marker, logger::error, msg, parms);
        e.printStackTrace();
    }

    @Override
    public void error(@Nonnull final Throwable e, @Nonnull final Supplier<String> message) {
        error(this.marker, e, message);
    }

    private void error(@Nonnull final Marker marker, @Nonnull final Throwable e, @Nonnull final Supplier<String> message) {
        error(marker, e, message.get());
    }

    @FunctionalInterface
    private interface ILoggit {
        void log(Marker m, String s, Object... params);
    }

    private static class ChildLog implements IModLog {

        @Nonnull
        private final ModLog parent;
        @Nonnull
        private final Marker marker;

        ChildLog(@Nonnull final ModLog parent, @Nonnull final Class<?> child) {
            this.parent = parent;
            this.marker = MarkerManager.getMarker(child.getSimpleName());
        }

        @Override
        public void info(@Nonnull String msg, @Nullable Object... parms) {
            this.parent.info(this.marker, msg, parms);
        }

        @Override
        public void info(@Nonnull Supplier<String> message) {
            this.parent.info(this.marker, message);
        }

        @Override
        public void warn(@Nonnull String msg, @Nullable Object... parms) {
            this.parent.warn(this.marker, msg, parms);
        }

        @Override
        public void warn(@Nonnull Supplier<String> message) {
            this.parent.warn(this.marker, message);
        }

        @Override
        public void debug(@Nonnull String msg, @Nullable Object... parms) {
            this.parent.debug(this.marker, msg, parms);
        }

        @Override
        public void debug(@Nonnull Supplier<String> message) {
            this.parent.debug(this.marker, message);
        }

        @Override
        public void debug(int mask, @Nonnull String msg, @Nullable Object... parms) {
            this.parent.debug(this.marker, mask, msg, parms);
        }

        @Override
        public void debug(int mask, @Nonnull Supplier<String> message) {
            this.parent.debug(this.marker, mask, message);
        }

        @Override
        public void error(@Nonnull Throwable e, @Nonnull String msg, @Nullable Object... parms) {
            this.parent.error(this.marker, e, msg, parms);
        }

        @Override
        public void error(@Nonnull Throwable e, @Nonnull Supplier<String> message) {
            this.parent.error(this.marker, e, message);
        }
    }
}

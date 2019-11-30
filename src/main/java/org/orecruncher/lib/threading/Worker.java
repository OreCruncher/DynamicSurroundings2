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

package org.orecruncher.lib.threading;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.math.TimerEMA;

import javax.annotation.Nonnull;

public class Worker {

    @Nonnull
    private final Thread thread;
    @Nonnull
    private final Runnable task;
    private final int frequency;
    @Nonnull
    private final IModLog logger;
    @Nonnull
    private String diagnosticString;

    public Worker(@Nonnull final String name, @Nonnull final Runnable task, final int frequency, @Nonnull final IModLog logger) {
        this.thread = new Thread(this::run);
        this.thread.setName(name);
        this.thread.setDaemon(true);
        this.task = task;
        this.frequency = frequency;
        this.logger = logger;
        this.diagnosticString = StringUtils.EMPTY;
    }

    public void run() {
        final TimerEMA timeTrack = new TimerEMA(this.thread.getName());
        final StopWatch sw = new StopWatch();
        for (; ; ) {
            sw.start();
            try {
                task.run();
            } catch (@Nonnull final Throwable t) {
                logger.error(t, "Error processing %s!", this.thread.getName());
            }
            timeTrack.update(sw.getNanoTime());
            this.diagnosticString = timeTrack.toString();
            sw.stop();
            long duration = (sw.getNanoTime() / 1000000L);
            long sleepTime = this.frequency - duration;
            sw.reset();
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (@Nonnull final Throwable ignore) {
                    logger.warn("Terminating %s thread", this.thread.getName());
                    return;
                }
            } else {
                logger.warn("%s is lagging; behind %d msecs", this.thread.getName(), Math.abs(sleepTime));
            }
        }

    }

    public void start() {
        this.thread.start();
    }

    @Nonnull
    public String getDiagnosticString() {
        return this.diagnosticString;
    }
}

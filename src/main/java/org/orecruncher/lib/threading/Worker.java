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
import java.util.concurrent.TimeUnit;

public final class Worker {

    @Nonnull
    private final Thread thread;
    @Nonnull
    private final Runnable task;
    @Nonnull
    private final IModLog logger;
    private final int frequency;
    @Nonnull
    private String diagnosticString;

    /**
     * Instantiates a worker thread to execute a task on a repeating basis.
     *
     * @param threadName     Name of the worker thread
     * @param task           The task to be executed
     * @param frequencyMsecs The frequency of execution in msecs
     * @param logger         The logger to use when logging is needed
     */
    public Worker(@Nonnull final String threadName, @Nonnull final Runnable task, final int frequencyMsecs, @Nonnull final IModLog logger) {
        this.thread = new Thread(this::run);
        this.thread.setName(threadName);
        this.thread.setDaemon(true);
        this.task = task;
        this.frequency = frequencyMsecs;
        this.logger = logger;
        this.diagnosticString = StringUtils.EMPTY;
    }

    private void run() {
        final TimerEMA timeTrack = new TimerEMA(this.thread.getName());
        final StopWatch sw = new StopWatch();
        for (; ; ) {
            sw.start();
            try {
                task.run();
            } catch (@Nonnull final Throwable t) {
                logger.error(t, "Error processing %s!", this.thread.getName());
            }
            sw.stop();
            timeTrack.update(sw.getNanoTime());
            this.diagnosticString = String.format("%s (deadline %d)", timeTrack.toString(), this.frequency);
            long sleepTime = this.frequency - sw.getTime(TimeUnit.MILLISECONDS);
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

    /**
     * Starts up the worker.  Execution will start immediately.
     */
    public void start() {
        this.thread.start();
    }

    /**
     * Gathers a diagnostic string to display or log.
     *
     * @return String for logging or display
     */
    @Nonnull
    public String getDiagnosticString() {
        return this.diagnosticString;
    }
}

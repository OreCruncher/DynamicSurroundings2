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

package org.orecruncher.lib.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import org.orecruncher.lib.math.TimerEMA;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DiagnosticEvent extends Event {

    private final List<String> left = new ArrayList<>();
    private final List<String> right = new ArrayList<>();
    private final List<TimerEMA> timers = new ArrayList<>();
    private final List<TimerEMA> renderTimers = new ArrayList<>();

    public DiagnosticEvent() {}

    public Collection<String> getLeft() {
        return this.left;
    }

    public Collection<String> getRight() {
        return this.right;
    }

    public Collection<TimerEMA> getTimers() {
        return this.timers;
    }

    public Collection<TimerEMA> getRenderTimers() {
        return this.renderTimers;
    }

    public void addLeft(@Nonnull final String... msgs) {
        this.left.addAll(Arrays.asList(msgs));
    }

    public void addRight(@Nonnull final String... msgs) {
        this.right.addAll(Arrays.asList(msgs));
    }

    public void addTimer(@Nonnull final TimerEMA timer) {
        this.timers.add(timer);
    }

    public void addRenderTimer(@Nonnull final TimerEMA timer) {
        this.renderTimers.add(timer);
    }

}

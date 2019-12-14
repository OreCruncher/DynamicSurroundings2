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

package org.orecruncher.sndctrl.audio.acoustic;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class AcousticEvent {

    private static final Map<String, AcousticEvent> mapping = new HashMap<>();
    @Nonnull
    public static final AcousticEvent NONE = new AcousticEvent("none", null).register();
    private final String name;
    private final AcousticEvent transition;
    public AcousticEvent(@Nonnull final String name, @Nullable final AcousticEvent transition) {
        this.name = name;
        this.transition = transition;
    }

    @Nullable
    public static AcousticEvent getEvent(@Nonnull final String name) {
        return mapping.get(name);
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    public boolean canTransition() {
        return this.transition != null;
    }

    @Nullable
    public AcousticEvent getTransition() {
        return this.transition;
    }

    @Nonnull
    public AcousticEvent register() {
        mapping.put(this.name, this);
        return this;
    }

}

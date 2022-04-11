/*
 *  Dynamic Surroundings
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.fog;

import net.minecraftforge.client.event.EntityViewRenderEvent;

import javax.annotation.Nonnull;

public final class FogResult {

    public static final float DEFAULT_PLANE_SCALE = 0.75F;

    private float start;
    private float end;

    public FogResult() {
        this(0, 0);
    }

    public FogResult(final float distance, final float scale) {
        this.set(distance, scale);
    }

    public FogResult(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        this.set(event);
    }

    public void setScaled(final float distance, final float scale) {
        this.start = distance * scale;
        this.end = distance;
    }

    public void set(final float start, final float end) {
        this.start = start;
        this.end = end;
    }

    public void set(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        this.setScaled(event.getFarPlaneDistance(), DEFAULT_PLANE_SCALE);
    }

    public float getStart() {
        return this.start;
    }

    public float getEnd() {
        return this.end;
    }

    public boolean isValid(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        return this.end > this.start;
    }

    @Override
    public String toString() {
        return String.format("[start: %f, end: %f]", this.start, this.end);
    }

}

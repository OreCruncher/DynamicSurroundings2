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

package org.orecruncher.sndctrl.audio;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * An EntitySoundInstance is one that is attached to an entity and will change position with that entity
 * every tick.  If the sound is a global sound (like music), it will stay around as long as it is repeatable
 * and the entity is alive.
 */
@OnlyIn(Dist.CLIENT)
public class EntitySoundInstance extends WrappedSoundInstance {

    @Nonnull
    private final Entity entity;

    private float x;
    private float y;
    private float z;

    public EntitySoundInstance(@Nonnull final Entity entity, @Nonnull final ISoundInstance sound) {
        super(sound);

        this.entity = Objects.requireNonNull(entity);

        updatePosition();
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Override
    public boolean isDonePlaying() {
        return !this.entity.isAlive() || super.isDonePlaying();
    }

    @Override
    public int getPlayDelay() {
        return this.sound.getPlayDelay();
    }

    @Override
    public void setPlayDelay(final int delay) {
        this.sound.setPlayDelay(delay);
    }

    private void updatePosition() {
        final Vector3d box = this.entity.getBoundingBox().getCenter();
        this.x = (float) box.x;
        this.y = (float) box.y;
        this.z = (float) box.z;
    }

    @Override
    public void tick() {

        super.tick();

        // If we are not done playing, and the sound is not global, we
        // update the sound's position.
        if (!isDonePlaying() && !isGlobal()) {
            updatePosition();
        }
    }

    @Override
    @Nonnull
    public String toString() {
        //@formatter:off
        return MoreObjects.toStringHelper(this)
                .addValue(this.entity.toString())
                .addValue(getSoundLocation().toString())
                .addValue(getCategory().toString())
                .addValue(getAttenuationType().toString())
                .addValue(getState().toString())
                .add("v", getVolume())
                .add("p", getPitch())
                .add("x", getX())
                .add("y", getY())
                .add("z", getZ())
                .toString();
        //@formatter:on
    }
}

/*
 *  Dynamic Surroundings: Environs
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

package org.orecruncher.environs.library;

import com.google.common.base.MoreObjects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.environs.handlers.scripts.ConditionEvaluator;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class AcousticEntry {

    private final IAcoustic acoustic;
    private final String conditions;

    public AcousticEntry(@Nonnull final IAcoustic acoustic, @Nullable final String condition) {
        this.acoustic = acoustic;
        this.conditions = condition != null ? condition : StringUtils.EMPTY;
    }

    @Nonnull
    public IAcoustic getAcoustic() {
        return this.acoustic;
    }

    @Nonnull
    public String getConditions() {
        return this.conditions;
    }

    public boolean matches() {
        return ConditionEvaluator.INSTANCE.check(this.conditions);
    }

    protected String getConditionsForLogging() {
        final String cond = getConditions();
        return cond.length() > 0 ? cond : "No Conditions";
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(getAcoustic().toString())
                .addValue(getConditionsForLogging())
                .toString();
    }
}

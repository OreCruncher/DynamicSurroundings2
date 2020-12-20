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

package org.orecruncher.environs.fog;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.handlers.CommonState;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.random.XorShiftRandom;
import org.orecruncher.lib.seasons.Season;

import javax.annotation.Nonnull;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MorningFogRangeCalculator extends VanillaFogRangeCalculator {

    private static final float START = 0.630F;
    private static final float END = 0.830F;
    private static final float RESERVE = 10F;
    private static final Map<Season, FogType> MAPPING = new Reference2ReferenceOpenHashMap<>();

    static {
        MAPPING.put(Season.EARLY_AUTUMN, FogType.NORMAL);
        MAPPING.put(Season.MID_AUTUMN, FogType.MEDIUM);
        MAPPING.put(Season.LATE_AUTUMN, FogType.HEAVY);

        MAPPING.put(Season.EARLY_WINTER, FogType.MEDIUM);
        MAPPING.put(Season.MID_WINTER, FogType.LIGHT);
        MAPPING.put(Season.LATE_WINTER, FogType.NORMAL);

        MAPPING.put(Season.EARLY_SPRING, FogType.MEDIUM);
        MAPPING.put(Season.MID_SPRING, FogType.HEAVY);
        MAPPING.put(Season.LATE_SPRING, FogType.NORMAL);

        MAPPING.put(Season.EARLY_SUMMER, FogType.LIGHT);
        MAPPING.put(Season.MID_SUMMER, FogType.NONE);
        MAPPING.put(Season.LATE_SUMMER, FogType.LIGHT);
    }

    protected final FogResult cache = new FogResult();
    protected int fogDay = -1;
    protected FogType type = FogType.NORMAL;

    public MorningFogRangeCalculator() {
        super("MorningFogRangeCalculator");
    }

    @Override
    @Nonnull
    public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        this.cache.set(event);
        if (this.type != FogType.NONE && this.cache.getStart() > this.type.getReserve()) {
            final float ca = GameUtils.getWorld().getCelestialAngle((float) event.getRenderPartialTicks());
            if (ca >= this.type.getStart() && ca <= this.type.getEnd()) {
                final float mid = (this.type.getStart() + this.type.getEnd()) / 2F;
                final float factor = 1F - MathStuff.abs(ca - mid) / (mid - this.type.getStart());
                final float shift = this.cache.getStart() * factor;
                final float newEnd = this.cache.getEnd() - shift;
                final float newStart = MathStuff.clamp(this.cache.getStart() - shift * 2, this.type.getReserve() + 1,
                        newEnd);
                this.cache.set(newStart, newEnd);
            }
        }
        return this.cache;
    }

    @Override
    public void tick() {
        // Determine if fog is going to be done this Minecraft day
        final int day = CommonState.getClock().getDay();
        if (this.fogDay != day) {
            final int dim = CommonState.getDimensionId();
            final int morningFogChance = Config.CLIENT.fog.get_morningFogChance();
            this.fogDay = day;
            final boolean doFog = (dim != -1 && dim != 1) && (morningFogChance < 2 || XorShiftRandom.current().nextInt(morningFogChance) == 0);
            this.type = doFog ? getFogType() : FogType.NONE;
        }
    }

    @Nonnull
    protected FogType getFogType() {
        final Season season = CommonState.getSeason();
        if (season != Season.NONE) {
            final FogType type = MAPPING.get(season);
            if (type != null)
                return type;
        }
        return FogType.NORMAL;
    }

    public enum FogType {
        NONE(0F, 0F, 0F),
        NORMAL(START, END, RESERVE),
        LIGHT(START + 0.1F, END - 0.1F, RESERVE + 5F),
        MEDIUM(START - 0.1F, END + 0.1F, RESERVE),
        HEAVY(START - 0.1F, END + 0.2F, RESERVE - 5F);

        private final float start;
        private final float end;
        private final float reserve;

        FogType(final float start, final float end, final float reserve) {
            this.start = start;
            this.end = end;
            this.reserve = reserve;
        }

        public float getStart() {
            return this.start;
        }

        public float getEnd() {
            return this.end;
        }

        public float getReserve() {
            return this.reserve;
        }
    }

}

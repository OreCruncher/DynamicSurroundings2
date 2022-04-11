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

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.orecruncher.environs.config.Config;
import org.orecruncher.environs.library.BiomeInfo;
import org.orecruncher.environs.library.BiomeUtil;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;

/**
 * Scans the biome area around the player to determine the fog parameters.
 */
public class BiomeFogRangeCalculator extends VanillaFogRangeCalculator {

    protected final FogResult cached = new FogResult();

    public BiomeFogRangeCalculator() {
        super("BiomeFogRangeCalculator");
    }

    @Override
    public boolean enabled() {
        return Config.CLIENT.fog.enableBiomeFog.get();
    }

    @Override
    @Nonnull
    public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {

        final ClientWorld world = GameUtils.getWorld();
        assert world != null;

        final BiomeManager biomemanager = world.getBiomeManager();
        final Vector3d origin = GameUtils.getMC().gameRenderer.getActiveRenderInfo().getProjectedView().subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
        final Vector3d visibilitySurvey = CubicSampler.func_240807_a_(origin, (x, y, z) -> {
            final Biome b = biomemanager.getBiomeAtPosition(x, y, z);
            final BiomeInfo info = BiomeUtil.getBiomeData(b);
            return new Vector3d(info.getVisibility(), 0, 0);
        });

        // Lower values means less visibility
        final double visibility = visibilitySurvey.getX();
        final double farPlaneDistance = visibility * event.getFarPlaneDistance();
        final double farPlaneDistanceScaleBiome = 0.1D * (1D - visibility) + FogResult.DEFAULT_PLANE_SCALE * visibility;

        this.cached.setScaled((float) farPlaneDistance, (float) farPlaneDistanceScaleBiome);

        return cached;
    }

}

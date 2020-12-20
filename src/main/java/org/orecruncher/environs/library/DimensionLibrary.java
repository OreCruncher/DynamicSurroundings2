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

import java.util.Optional;

import javax.annotation.Nonnull;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.library.config.DimensionConfig;
import org.orecruncher.environs.library.config.ModConfig;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.logging.IModLog;

@OnlyIn(Dist.CLIENT)
public final class DimensionLibrary {

	private static final IModLog LOGGER = Environs.LOGGER.createChild(DimensionLibrary.class);

	private DimensionLibrary() {

	}

	private static final ObjectArray<DimensionConfig> cache = new ObjectArray<>();
	private static final Int2ObjectOpenHashMap<DimensionInfo> configs = new Int2ObjectOpenHashMap<>();

	static void initialize() {

	}

	static void initFromConfig(@Nonnull final ModConfig cfg) {
		cfg.dimensions.forEach(DimensionLibrary::register);
	}

	static void complete() {
		if (Config.CLIENT.logging.get_enableLogging()) {
			LOGGER.info("*** DIMENSION REGISTRY (cache) ***");
			cache.stream().map(Object::toString).forEach(LOGGER::info);
		}
	}

	@Nonnull
	private static DimensionConfig getData(@Nonnull final DimensionConfig entry) {
		final Optional<DimensionConfig> result = cache.stream().filter(e -> e.equals(entry)).findFirst();
		if (result.isPresent())
			return result.get();
		cache.add(entry);
		return entry;
	}

	private static void register(@Nonnull final DimensionConfig entry) {
		if (entry.dimensionId != null || entry.name != null) {
			final DimensionConfig data = getData(entry);
			if (data == entry)
				return;
			if (data.dimensionId == null)
				data.dimensionId = entry.dimensionId;
			if (data.name == null)
				data.name = entry.name;
			if (entry.hasAurora != null)
				data.hasAurora = entry.hasAurora;
			if (entry.hasHaze != null)
				data.hasHaze = entry.hasHaze;
			if (entry.hasWeather != null)
				data.hasWeather = entry.hasWeather;
			if (entry.cloudHeight != null)
				data.cloudHeight = entry.cloudHeight;
			if (entry.seaLevel != null)
				data.seaLevel = entry.seaLevel;
			if (entry.skyHeight != null)
				data.skyHeight = entry.skyHeight;
		}
	}

	@Nonnull
	public static DimensionInfo getData(@Nonnull final World world) {
		DimensionInfo dimInfo = configs.get(world.getDimension().getType().getId());
		if (dimInfo == null) {
			DimensionConfig config = null;
			for (final DimensionConfig e : cache)
				if ((e.dimensionId != null && e.dimensionId == world.getDimension().getType().getId())
						|| (e.name != null && e.name.equals(world.getDimension().getType().getRegistryName().toString()))) {
					config = e;
					break;
				}

			configs.put(world.getDimension().getType().getId(), dimInfo = new DimensionInfo(world, config));
		}
		return dimInfo;
	}
}

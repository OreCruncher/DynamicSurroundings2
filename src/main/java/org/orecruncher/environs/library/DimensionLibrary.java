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

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.library.config.DimensionConfig;
import org.orecruncher.environs.library.config.ModConfig;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.resource.IResourceAccessor;
import org.orecruncher.lib.resource.ResourceUtils;
import org.orecruncher.lib.service.ClientServiceManager;
import org.orecruncher.lib.service.IClientService;

@OnlyIn(Dist.CLIENT)
public final class DimensionLibrary {

	private static final IModLog LOGGER = Environs.LOGGER.createChild(DimensionLibrary.class);

	private DimensionLibrary() {

	}

	private static final ObjectArray<DimensionConfig> cache = new ObjectArray<>();
	// TODO:  What type of hash map?
	private static final HashMap<RegistryKey<World>, DimensionInfo> configs = new HashMap<>();

	static void initialize() {
		ClientServiceManager.instance().add(new DimensionLibraryService());
	}

	static void initFromConfig(@Nonnull final ModConfig cfg) {
		cfg.dimensions.forEach(DimensionLibrary::register);
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
		if (entry.dimensionId != null) {
			final DimensionConfig data = getData(entry);
			if (data == entry)
				return;
			if (data.dimensionId == null)
				data.dimensionId = entry.dimensionId;
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
		RegistryKey<World> key = world.getDimensionKey();
		DimensionInfo dimInfo = configs.get(key);

		if (dimInfo == null) {
			DimensionConfig config = null;
			ResourceLocation location = key.getLocation();
			for (final DimensionConfig e : cache)
				if (e.dimensionId.equals(location.toString())) {
					config = e;
					break;
				}

			configs.put(key, dimInfo = new DimensionInfo(world, config));
		}
		return dimInfo;
	}

	private static class DimensionLibraryService implements IClientService {

		@Override
		public void start() {

			final Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "dimensions.json");

			for (final IResourceAccessor accessor : configs) {
				LOGGER.debug("Loading configuration %s", accessor.location());
				try {
					initFromConfig(accessor.as(ModConfig.class));
				} catch (@Nonnull final Throwable t) {
					LOGGER.error(t, "Unable to load %s", accessor.location());
				}
			}

			if (Config.CLIENT.logging.get_enableLogging()) {
				LOGGER.info("*** DIMENSION REGISTRY (cache) ***");
				cache.stream().map(Object::toString).forEach(LOGGER::info);
			}
		}

		@Override
		public void stop() {
			cache.clear();
			configs.clear();
		}
	}
}

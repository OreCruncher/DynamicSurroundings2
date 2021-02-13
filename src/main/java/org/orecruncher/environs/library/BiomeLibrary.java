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

package org.orecruncher.environs.library;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.environs.config.Config;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.library.config.BiomeConfig;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.math.MathStuff;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.orecruncher.lib.resource.IResourceAccessor;
import org.orecruncher.lib.resource.ResourceUtils;
import org.orecruncher.lib.service.ModuleServiceManager;
import org.orecruncher.lib.service.IModuleService;

@OnlyIn(Dist.CLIENT)
public final class BiomeLibrary {

	private static final IModLog LOGGER = Environs.LOGGER.createChild(BiomeLibrary.class);

	private static final int INSIDE_Y_ADJUST = 3;

	public static final FakeBiomeAdapter UNDERGROUND = new FakeBiomeAdapter("Underground");
	public static final FakeBiomeAdapter PLAYER = new FakeBiomeAdapter("Player");
	public static final FakeBiomeAdapter UNDERWATER = new FakeBiomeAdapter("Underwater");
	public static final FakeBiomeAdapter UNDEROCEAN = new FakeBiomeAdapter("UnderOCN");
	public static final FakeBiomeAdapter UNDERDEEPOCEAN = new FakeBiomeAdapter("UnderDOCN");
	public static final FakeBiomeAdapter UNDERRIVER = new FakeBiomeAdapter("UnderRVR");
	public static final FakeBiomeAdapter OUTERSPACE = new FakeBiomeAdapter("OuterSpace");
	public static final FakeBiomeAdapter CLOUDS = new FakeBiomeAdapter("Clouds");
	public static final FakeBiomeAdapter VILLAGE = new FakeBiomeAdapter("Village");

	// This is for cases when the biome coming in doesn't make sense
	// and should default to something to avoid crap.
	private static final FakeBiomeAdapter WTF = new WTFFakeBiomeAdapter();

	public static final BiomeInfo UNDERGROUND_INFO = UNDERGROUND.getBiomeData();
	public static final BiomeInfo PLAYER_INFO = PLAYER.getBiomeData();
	public static final BiomeInfo UNDERRIVER_INFO = UNDERRIVER.getBiomeData();
	public static final BiomeInfo UNDEROCEAN_INFO = UNDEROCEAN.getBiomeData();
	public static final BiomeInfo UNDERDEEPOCEAN_INFO = UNDERDEEPOCEAN.getBiomeData();
	public static final BiomeInfo UNDERWATER_INFO = UNDERWATER.getBiomeData();
	public static final BiomeInfo OUTERSPACE_INFO = OUTERSPACE.getBiomeData();
	public static final BiomeInfo CLOUDS_INFO = CLOUDS.getBiomeData();
	public static final BiomeInfo VILLAGE_INFO = VILLAGE.getBiomeData();
	public static final BiomeInfo WTF_INFO = WTF.getBiomeData();

	private static final ObjectOpenHashSet<FakeBiomeAdapter> theFakes = new ObjectOpenHashSet<>();

	static {
		theFakes.add(UNDERGROUND);
		theFakes.add(PLAYER);
		theFakes.add(UNDERWATER);
		theFakes.add(UNDEROCEAN);
		theFakes.add(UNDERDEEPOCEAN);
		theFakes.add(UNDERRIVER);
		theFakes.add(OUTERSPACE);
		theFakes.add(CLOUDS);
		theFakes.add(VILLAGE);
		theFakes.add(WTF);
	}

	private BiomeLibrary() {

	}

	static void initialize() {
		ModuleServiceManager.instance().add(new BiomeLibraryService());
	}

	static void initFromConfig(@Nonnull final List<BiomeConfig> cfg) {

		if (cfg.size() > 0) {
			final BiomeEvaluator evaluator = new BiomeEvaluator();
			for (final BiomeInfo bi : getCombinedStream()) {
				evaluator.update(bi);
				for (final BiomeConfig c : cfg) {
					if (evaluator.matches(c.conditions)) {
						try {
							bi.update(c);
						} catch (@Nonnull final Throwable t) {
							LOGGER.warn("Unable to process biome sound configuration [%s]", c.toString());
						}
					}
				}
			}
		}
	}

	@Nonnull
	public static BiomeInfo getPlayerBiome(@Nonnull final PlayerEntity player, final boolean getTrue) {
		final Biome biome = player.getEntityWorld().getBiome(new BlockPos(player.getPosX(), 0, player.getPosZ()));
		BiomeInfo info = BiomeUtil.getBiomeData(biome);

		if (!getTrue) {
			if (player.areEyesInFluid(FluidTags.WATER)) {
				if (info.isRiver())
					info = UNDERRIVER_INFO;
				else if (info.isDeepOcean())
					info = UNDERDEEPOCEAN_INFO;
				else if (info.isOcean())
					info = UNDEROCEAN_INFO;
				else
					info = UNDERWATER_INFO;
			} else {
				final DimensionInfo dimInfo = DimensionLibrary.getData(player.getEntityWorld());
				final int theY = MathStuff.floor(player.getPosY());
				if ((theY + INSIDE_Y_ADJUST) <= dimInfo.getSeaLevel())
					info = UNDERGROUND_INFO;
				else if (theY >= dimInfo.getSpaceHeight())
					info = OUTERSPACE_INFO;
				else if (theY >= dimInfo.getCloudHeight())
					info = CLOUDS_INFO;
			}
		}

		return info;
	}

	private static Collection<BiomeInfo> getCombinedStream() {
		return Stream.concat(
				ForgeUtils.getBiomes().stream().map(BiomeUtil::getBiomeData),
				theFakes.stream().map(FakeBiomeAdapter::getBiomeData)
		).collect(Collectors.toCollection(ArrayList::new));
	}

	static class BiomeLibraryService implements IModuleService {

		private static final Type biomeType = TypeToken.getParameterized(List.class, BiomeConfig.class).getType();

		@Override
		public String name() {
			return "BiomeLibrary";
		}

		@Override
		public void start() {

			ForgeUtils.getBiomes().forEach(b -> {
				final BiomeAdapter handler = new BiomeAdapter(b);
				BiomeUtil.setBiomeData(b, new BiomeInfo(handler));
			});

			// Make sure the default biomes are set
			BiomeUtil.getBiomeData(BiomeRegistry.PLAINS);
			BiomeUtil.getBiomeData(BiomeRegistry.THE_VOID);

			final Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "biomes.json");

			IResourceAccessor.process(configs, accessor -> {
				initFromConfig(accessor.as(biomeType));
			});
		}

		@Override
		public void log() {
			if (Config.CLIENT.logging.enableLogging.get()) {
				LOGGER.info("*** BIOME REGISTRY ***");
				getCombinedStream().stream().sorted().map(Object::toString).forEach(LOGGER::info);
			}

			getCombinedStream().forEach(BiomeInfo::trim);
		}

		@Override
		public void stop() {
			ForgeUtils.getBiomes().forEach(b -> BiomeUtil.setBiomeData(b, null));
			BiomeUtil.setBiomeData(BiomeRegistry.PLAINS, null);
			BiomeUtil.setBiomeData(BiomeRegistry.THE_VOID, null);
		}
	}

}

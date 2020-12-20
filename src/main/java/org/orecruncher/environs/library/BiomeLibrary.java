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

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.library.config.BiomeConfig;
import org.orecruncher.environs.library.config.ModConfig;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.math.MathStuff;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

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
		ForgeUtils.getBiomes().forEach(BiomeUtil::getBiomeData);
	}

	static void initFromConfig(@Nonnull final ModConfig cfg) {

		if (cfg.biomes.size() > 0) {
			final BiomeEvaluator evaluator = new BiomeEvaluator();
			for (final BiomeInfo bi : getCombinedStream()) {
				evaluator.update(bi);
				for (final BiomeConfig c : cfg.biomes) {
					if (evaluator.matches(c.conditions)) {
						try {
							bi.update(c);
						} catch(@Nonnull final Throwable t) {
							LOGGER.warn("Unable to process biome sound configuration [%s]", c.toString());
						}
					}
				}
			}

			// Make sure the default PLAINS biome is set. OTG can do some strange things.
			final ResourceLocation plainsLoc = new ResourceLocation("plains");
			final Biome plains = ForgeRegistries.BIOMES.getValue(plainsLoc);
			final BiomeInfo info = BiomeUtil.getBiomeData(plains);
			BiomeUtil.setBiomeData(Biomes.PLAINS, info);
		}
	}

	static void complete() {
		if (Config.CLIENT.logging.get_enableLogging()) {
			LOGGER.info("*** BIOME REGISTRY ***");
			getCombinedStream().stream().sorted().map(Object::toString).forEach(LOGGER::info);
		}
		getCombinedStream().forEach(BiomeInfo::trim);
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
}

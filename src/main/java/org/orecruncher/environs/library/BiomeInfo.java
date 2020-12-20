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

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.library.config.BiomeConfig;
import org.orecruncher.environs.library.config.AcousticConfig;
import org.orecruncher.lib.Utilities;
import org.orecruncher.lib.WeightTable;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary.Type;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.Library;

@OnlyIn(Dist.CLIENT)
public final class BiomeInfo implements Comparable<BiomeInfo> {

	private final static float DEFAULT_FOG_DENSITY = 0.4F;
	private final static Color DEFAULT_FOG_COLOR = new Color(64, 96, 64);
	private final static Color DEFAULT_DUST_COLOR = new Color(255, 234, 151);

	public final static int DEFAULT_SPOT_CHANCE = 1000 / 4;

	protected final IBiome biome;

	protected boolean hasDust;
	protected boolean hasAurora;
	protected boolean hasFog;

	private Color dustColor = DEFAULT_DUST_COLOR;
	private Color fogColor = DEFAULT_FOG_COLOR;
	private float fogDensity = DEFAULT_FOG_DENSITY;

	protected int spotSoundChance = DEFAULT_SPOT_CHANCE;

	protected final ObjectArray<AcousticEntry> sounds = new ObjectArray<>();
	protected final ObjectArray<WeightedAcousticEntry> spotSounds = new ObjectArray<>();
	protected ObjectArray<String> comments = new ObjectArray<>();

	protected final boolean isRiver;
	protected final boolean isOcean;
	protected final boolean isDeepOcean;
	
	protected final String traits;

	public BiomeInfo(@Nonnull final IBiome biome) {
		this.biome = biome;

		this.isRiver = this.biome.getTypes().contains(Type.RIVER);
		this.isOcean = this.biome.getTypes().contains(Type.OCEAN);
		this.isDeepOcean = this.isOcean && getBiomeName().matches("(?i).*deep.*ocean.*|.*abyss.*");
	
		this.traits = getBiomeTypes().stream().map(Type::getName).collect(Collectors.joining(" "));
	}

	public boolean isRiver() {
		return this.isRiver;
	}

	public boolean isOcean() {
		return this.isOcean;
	}

	public boolean isDeepOcean() {
		return this.isDeepOcean;
	}

	public ResourceLocation getKey() {
		return this.biome.getKey();
	}

	public Biome getBiome() {
		return this.biome.getBiome();
	}

	public Set<Type> getBiomeTypes() {
		return this.biome.getTypes();
	}
	
	public String getBiomeTraits() {
		return this.traits;
	}

	void addComment(@Nonnull final String comment) {
		if (!StringUtils.isEmpty(comment))
			this.comments.add(comment);
	}

	public String getBiomeName() {
		return this.biome.getName();
	}

	public Biome.RainType getPrecipitationType() {
		return this.biome.getPrecipitationType();
	}

	public boolean getHasDust() {
		return this.hasDust;
	}

	void setHasDust(final boolean flag) {
		this.hasDust = flag;
	}

	public boolean getHasAurora() {
		return this.hasAurora;
	}

	void setHasAurora(final boolean flag) {
		this.hasAurora = flag;
	}

	public boolean getHasFog() {
		return this.hasFog;
	}

	void setHasFog(final boolean flag) {
		this.hasFog = flag;
	}

	public Color getDustColor() {
		return this.dustColor;
	}

	void setDustColor(final Color color) {
		this.dustColor = color;
	}

	public Color getFogColor() {
		return this.fogColor;
	}

	void setFogColor(@Nonnull final Color color) {
		this.fogColor = color;
	}

	public float getFogDensity() {
		return this.fogDensity;
	}

	void setFogDensity(final float density) {
		this.fogDensity = density;
	}

	void setSpotSoundChance(final int chance) {
		this.spotSoundChance = chance;
	}

	public boolean isFake() {
		return this.biome instanceof FakeBiomeAdapter;
	}

	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return this.biome.getFloatTemperature(pos);
	}

	public float getTemperature() {
		return this.biome.getTemperature();
	}

	public TempCategory getTempCategory() {
		return this.biome.getTempCategory();
	}

	public boolean isHighHumidity() {
		return this.biome.isHighHumidity();
	}

	public float getRainfall() {
		return this.biome.getDownfall();
	}

	@Nonnull
	public Collection<IAcoustic> findSoundMatches() {
		return findSoundMatches(new ObjectArray<>());
	}

	@Nonnull
	public Collection<IAcoustic> findSoundMatches(@Nonnull final Collection<IAcoustic> results) {
		for (final AcousticEntry sound : this.sounds) {
			if (sound.matches())
				results.add(sound.getAcoustic());
		}
		return results;
	}

	@Nullable
	public IAcoustic getSpotSound(@Nonnull final Random random) {
		if (this.spotSounds.size() == 0 || random.nextInt(this.spotSoundChance) != 0)
			return null;
		return new WeightTable<>(this.spotSounds.stream().filter(AcousticEntry::matches).collect(Collectors.toList())).next();
	}

	void resetSounds() {
		this.sounds.clear();
		this.spotSounds.clear();
		this.spotSoundChance = DEFAULT_SPOT_CHANCE;
	}

	public void update(@Nonnull final BiomeConfig entry) {
		addComment(entry.comment);

		if (entry.hasAurora != null)
			setHasAurora(entry.hasAurora);
		if (entry.hasDust != null)
			setHasDust(entry.hasDust);
		if (entry.hasFog != null)
			setHasFog(entry.hasFog);
		if (entry.fogDensity != null)
			setFogDensity(entry.fogDensity);

		if (entry.fogColor != null) {
			final int[] rgb = Utilities.splitToInts(entry.fogColor, ',');
			if (rgb.length == 3)
				setFogColor(new Color(rgb[0], rgb[1], rgb[2]));
		}

		if (entry.dustColor != null) {
			final int[] rgb = Utilities.splitToInts(entry.dustColor, ',');
			if (rgb.length == 3)
				setDustColor(new Color(rgb[0], rgb[1], rgb[2]));
		}

		if (entry.soundReset != null && entry.soundReset) {
			addComment("> Sound Reset");
			resetSounds();
		}

		if (entry.spotSoundChance != null)
			setSpotSoundChance(entry.spotSoundChance);

		for (final AcousticConfig sr : entry.acoustics) {
			final ResourceLocation res = Library.resolveResource(Environs.MOD_ID, sr.acoustic);
			final IAcoustic acoustic = Library.resolve(res, sr.acoustic);

			if (sr.type.equalsIgnoreCase("spot")) {
				final int weight = sr.weight;
				final WeightedAcousticEntry acousticEntry = new WeightedAcousticEntry(acoustic, sr.conditions, weight);
				this.spotSounds.add(acousticEntry);
			} else {
				final AcousticEntry acousticEntry = new AcousticEntry(acoustic, sr.conditions);
				this.sounds.add(acousticEntry);
			}
		}
	}

	public void trim() {
		this.sounds.trim();
		this.spotSounds.trim();
		this.comments = null;
	}

	@Override
	@Nonnull
	public String toString() {
		final ResourceLocation rl = this.biome.getKey();
		final String registryName = rl == null ? (isFake() ? "FAKE" : "UNKNOWN") : rl.toString();

		final StringBuilder builder = new StringBuilder();
		builder.append("Biome [").append(getBiomeName()).append('/').append(registryName).append("] (");
		if (!isFake()) {
			builder.append("\n+ ").append('<');
			builder.append(getBiomeTraits());
			builder.append('>').append('\n');
			builder.append("+ temp: ").append(getTemperature());
			builder.append(" rain: ").append(getRainfall());
		}

		if (this.hasDust)
			builder.append(" DUST");
		if (this.hasAurora)
			builder.append(" AURORA");
		if (this.hasFog)
			builder.append(" FOG");
		if (this.hasDust && this.dustColor != null)
			builder.append(" dustColor:").append(this.dustColor.toString());
		if (this.hasFog && this.fogColor != null) {
			builder.append(" fogColor:").append(this.fogColor.toString());
			builder.append(" fogDensity:").append(this.fogDensity);
		}

		if (this.sounds.size() > 0) {
			builder.append("\n+ sounds [\n");
			builder.append(this.sounds.stream().map(c -> "+   " + c.toString()).collect(Collectors.joining("\n")));
			builder.append("\n+ ]");
		}

		if (this.spotSounds.size() > 0) {
			builder.append("\n+ spot sound chance:").append(this.spotSoundChance);
			builder.append("\n+ spot sounds [\n");
			builder.append(this.spotSounds.stream().map(c -> "+   " + c.toString()).collect(Collectors.joining("\n")));
			builder.append("\n+ ]");
		}

		if (this.comments != null && this.comments.size() > 0) {
			builder.append("\n+ comments:\n");
			builder.append(this.comments.stream().map(c -> "+   " + c).collect(Collectors.joining("\n")));
			builder.append('\n');
		}

		return builder.toString();
	}

	@Override
	public int compareTo(@Nonnull final BiomeInfo o) {
		return getBiomeName().compareTo(o.getBiomeName());
	}
}

/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2019  OreCruncher
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

package org.orecruncher.environs.effects.emitters;

import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.effects.JetEffect;
import org.orecruncher.environs.effects.WaterSplashJetEffect;
import org.orecruncher.environs.effects.particles.Collections;
import org.orecruncher.environs.handlers.CommonState;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.util.math.BlockPos;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.Library;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.audio.LoopingSoundInstance;
import org.orecruncher.sndctrl.audio.SoundUtils;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public class WaterSplashJet extends Jet {

	private static final ResourceLocation[] waterfallAcoustics = new ResourceLocation[JetEffect.MAX_STRENGTH + 1];
	static {
		final ResourceLocation defaultAcoustic = new ResourceLocation(Environs.MOD_ID, "waterfall/0");
		Arrays.fill(waterfallAcoustics, defaultAcoustic);
		waterfallAcoustics[2] = waterfallAcoustics[3] = new ResourceLocation(Environs.MOD_ID, "waterfall/1");
		waterfallAcoustics[4] = new ResourceLocation(Environs.MOD_ID, "waterfall/2");
		waterfallAcoustics[5] = waterfallAcoustics[6] = new ResourceLocation(Environs.MOD_ID, "waterfall/3");
		waterfallAcoustics[7] = waterfallAcoustics[8] = new ResourceLocation(Environs.MOD_ID, "waterfall/4");
		waterfallAcoustics[9] = waterfallAcoustics[10] = new ResourceLocation(Environs.MOD_ID, "waterfall/5");
	}

	protected LoopingSoundInstance sound;
	protected int particleLimit;
	protected final double deltaY;

	public WaterSplashJet(final int strength, final IBlockReader world, final BlockPos loc, final double dY) {
		super(0, strength, world, loc.getX() + 0.5D, loc.getY() + 0.5D, loc.getZ() + 0.5D, 4);
		this.deltaY = loc.getY() + dY;
		setSpawnCount((int) (strength * 2.5F));
	}

	public void setSpawnCount(final int limit) {
		this.particleLimit = MathStuff.clamp(limit, 5, 20);
	}

	public int getSpawnCount() {
		final ParticleStatus state = GameUtils.getGameSettings().particles;
		switch (state) {
			case MINIMAL:
				return 0;
			case ALL:
				return this.particleLimit;
			default:
				return this.particleLimit / 2;
		}
	}

	@Override
	public boolean shouldDie() {
		// Check every half second
		return (this.particleAge % 10) == 0
				&& !WaterSplashJetEffect.isValidSpawnBlock(this.world, this.position);
	}

	@Override
	protected void soundUpdate() {
		if (!isAlive())
			return;

		if (this.sound == null) {
			final int idx = MathStuff.clamp(this.jetStrength, 0, waterfallAcoustics.length - 1);
			final IAcoustic acoustic = Library.resolve(waterfallAcoustics[idx]);
			this.sound = new LoopingSoundInstance(acoustic.getFactory().createSoundAt(this.position));
		}

		final boolean inRange = SoundUtils.inRange(CommonState.getPlayerEyePosition(), this.sound, 4);
		final boolean isActive = this.sound.getState().isActive();

		if (inRange && !isActive) {
			AudioEngine.play(this.sound);
		} else if (!inRange && isActive) {
			AudioEngine.stop(this.sound);
		}
	}

	@Override
	protected void cleanUp() {
		if (this.sound != null) {
			AudioEngine.stop(this.sound);
			this.sound = null;
		}
		super.cleanUp();
	}

	@Override
	protected void spawnJetParticle() {
		if (Collections.canFitWaterSpray()) {
			final int splashCount = getSpawnCount();

			for (int j = 0; (float) j < splashCount; ++j) {
				final double xOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
				final double zOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);

				if (WorldUtils.isBlockSolid(this.world, new BlockPos(this.posX + xOffset, this.deltaY, this.posZ + zOffset)))
					continue;

				final int motionStr = this.jetStrength + 3;
				final double motionX = xOffset * (motionStr / 20.0D);
				final double motionZ = zOffset * (motionStr / 20.0D);
				final double motionY = 0.1D + RANDOM.nextFloat() * motionStr / 20.0D;
				final boolean added = Collections.addWaterSpray(this.world, this.posX + xOffset,
						this.deltaY, this.posZ + zOffset, motionX, motionY, motionZ);
				// If we could not add the collection is full. No sense beating a dead horse.
				if (!added)
					break;
			}
		}
	}

}

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

package org.orecruncher.environs.shaders.aurora;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.orecruncher.environs.config.Config;
import org.orecruncher.environs.handlers.CommonState;
import org.orecruncher.environs.library.DimensionInfo;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.random.XorShiftRandom;

@OnlyIn(Dist.CLIENT)
public abstract class AuroraBase implements IAurora {

	protected final Random random;
	protected final AuroraBand band;
	protected final int bandCount;
	protected final float offset;
	protected final AuroraLifeTracker tracker;
	protected final AuroraColor colors;

	protected final PlayerEntity player;
	protected final DimensionInfo dimInfo;

	public AuroraBase(final long seed) {
		this(new XorShiftRandom(seed));
	}

	public AuroraBase(final Random rand) {
		this.random = rand;
		this.bandCount = Math.min(this.random.nextInt(3) + 1, Config.CLIENT.aurora.get_maxBands());
		this.offset = this.random.nextInt(20) + 20;
		this.colors = AuroraColor.get(this.random);

		final AuroraFactory.AuroraGeometry geo = AuroraFactory.AuroraGeometry.get(this.random);
		this.band = new AuroraBand(this.random, geo);
		this.tracker = new AuroraLifeTracker(AuroraUtils.AURORA_PEAK_AGE, AuroraUtils.AURORA_AGE_RATE);

		this.player = GameUtils.getPlayer();
		this.dimInfo = CommonState.getDimensionInfo();
	}

	@Override
	public boolean isAlive() {
		return this.tracker.isAlive();
	}

	@Override
	public void setFading(final boolean flag) {
		this.tracker.setFading(flag);
	}

	@Override
	public boolean isDying() {
		return this.tracker.isFading();
	}

	@Override
	public void update() {
		this.tracker.update();
	}

	@Override
	public boolean isComplete() {
		return !isAlive();
	}

	protected float getAlpha() {
		return (this.tracker.ageRatio() * this.band.getAlphaLimit()) / 255;
	}

	protected double getTranslationX(final float partialTick) {
		return MathHelper.lerp(partialTick, this.player.lastTickPosX, this.player.getPosX());
	}

	protected double getTranslationZ(final float partialTick) {
		return MathHelper.lerp(partialTick, this.player.lastTickPosZ, this.player.getPosZ()) - 3 * AuroraUtils.PLAYER_FIXED_Z_OFFSET;
	}

	protected double getTranslationY(final float partialTick) {
		final double posY = MathHelper.lerp(partialTick, this.player.lastTickPosY, this.player.getPosY());
		if (posY > dimInfo.getSeaLevel()) {
			final double limit = (this.dimInfo.getSkyHeight() + this.dimInfo.getCloudHeight()) / 2D;
			final double result = posY + AuroraUtils.PLAYER_FIXED_Y_OFFSET;
			return Math.min(result, limit);
		}

		return AuroraUtils.PLAYER_FIXED_Y_OFFSET;
	}

	@Nonnull
	protected Color getBaseColor() {
		return this.colors.baseColor;
	}

	@Nonnull
	protected Color getFadeColor() {
		return this.colors.fadeColor;
	}

	@Nonnull
	protected Color getMiddleColor() {
		return this.colors.middleColor;
	}

	@Override
	public abstract void render(@Nonnull final RenderWorldLastEvent event);

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("bands: ").append(this.bandCount);
		builder.append(", off: ").append(this.offset);
		builder.append(", len: ").append(this.band.length);
		builder.append(", base: ").append(getBaseColor().toString());
		builder.append(", fade: ").append(getFadeColor().toString());
		builder.append(", alpha: ").append((int) (getAlpha() * 255));
		if (!this.tracker.isAlive())
			builder.append(", DEAD");
		else if (this.tracker.isFading())
			builder.append(", FADING");
		return builder.toString();
	}

}

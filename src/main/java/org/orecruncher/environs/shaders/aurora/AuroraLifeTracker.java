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

package org.orecruncher.environs.shaders.aurora;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.math.MathStuff;

@OnlyIn(Dist.CLIENT)
public class AuroraLifeTracker {

	protected final int peakAge;
	protected final int ageDelta;
	protected int timer;
	protected boolean isAlive = true;
	protected boolean isFading = false;

	public AuroraLifeTracker(final int peakAge, final int ageDelta) {
		this.peakAge = peakAge;
		this.ageDelta = ageDelta;
	}

	public boolean isAlive() {
		return this.isAlive;
	}

	public boolean isFading() {
		return this.isFading;
	}

	public void setFading(final boolean f) {
		this.isFading = f;
	}

	public void kill() {
		this.isAlive = false;
		this.timer = 0;
	}

	public float ageRatio() {
		return (float) this.timer / (float) this.peakAge;
	}

	public void update() {

		if (!this.isAlive)
			return;

		if (this.isFading) {
			this.timer -= this.ageDelta;
		} else {
			this.timer += this.ageDelta;
		}

		this.timer = MathStuff.clamp(this.timer, 0, this.peakAge);

		if (this.timer == 0 && this.isFading)
			this.isAlive = false;
	}
}

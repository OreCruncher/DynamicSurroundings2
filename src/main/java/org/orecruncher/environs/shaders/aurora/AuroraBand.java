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
import javax.annotation.Nullable;

import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.math.MathStuff;

@OnlyIn(Dist.CLIENT)
public class AuroraBand {

	protected static final float AURORA_SPEED = 0.75F;
	public static final float AURORA_AMPLITUDE = 18.0F;
	public static final float AURORA_HEIGHT = AURORA_AMPLITUDE * 4;

	protected final Random random;

	protected Panel[] nodes;
	protected float cycle = 0.0F;
	protected int alphaLimit = 128;
	protected int length;
	protected float nodeLength;
	protected float nodeWidth;

	public AuroraBand(final Random random,
					  final AuroraFactory.AuroraGeometry geo) {
		this.random = random;
		preset(geo);
		generateBands();
		translate(0);
	}

	public int getAlphaLimit() {
		return this.alphaLimit;
	}

	@Nonnull
	public Panel[] getNodeList() {
		return this.nodes;
	}

	public float getNodeWidth() {
		return this.nodeWidth;
	}

	public int getPanelCount() {
		return this.nodes.length - 1;
	}

	@Nullable
	public Vector3f[] getPanelQuad(final int panelNumber) {
		if (panelNumber < 0 || panelNumber >= getPanelCount())
			return null;

		final Vector3f[] nodes = new Vector3f[4];
		final Panel panelA = this.nodes[panelNumber];
		final Panel panelB = this.nodes[panelNumber + 1];

		nodes[0] = new Vector3f(panelA.tetX, 0, panelA.tetZ);
		nodes[1] = new Vector3f(panelB.tetX, 0, panelB.tetZ);
		nodes[2] = new Vector3f(panelB.tetX, panelB.getModdedY(), panelB.tetZ);
		nodes[3] = new Vector3f(panelA.tetX, panelA.getModdedY(), panelA.tetZ);

		return nodes;
	}

	public void update() {
		if ((this.cycle += AURORA_SPEED) >= 360.0F)
			this.cycle -= 360.0F;
	}

	/*
	 * Calculates the next "frame" of the aurora if it is being animated.
	 */
	public void translate(final float partialTick) {
		final float c = this.cycle + AURORA_SPEED * partialTick;
		for (int i = 0; i < this.nodes.length; i++) {
			// Travelling sine wave: https://en.wikipedia.org/wiki/Wavelength
			final float f = MathStuff.cos(MathStuff.toRadians((i << 3) + c));
			this.nodes[i].translate(f * 3.0F, f * AURORA_AMPLITUDE);
		}
	}

	protected void preset(final AuroraFactory.AuroraGeometry geo) {
		this.length = geo.length;
		this.nodeLength = geo.nodeLength;
		this.nodeWidth = geo.nodeWidth;
		this.alphaLimit = geo.alphaLimit;
	}

	protected void generateBands() {
		this.nodes = populate();

		for (int i = 0; i < this.length; i++) {
			this.nodes[i].setWidth(this.nodeWidth);
		}
	}

	@Nonnull
	protected Panel[] populate() {

		final Panel[] nodeList = new Panel[this.length];
		final float[] angles = new float[this.length];

		final int bound = this.length / 2 - 1;

		float angleTotal = 0.0F;
		for (int i = this.length / 8 / 2 - 1; i >= 0; i--) {
			float angle = (this.random.nextFloat() - 0.5F) * 8.0F;
			angleTotal += angle;
			if (MathStuff.abs(angleTotal) > 180.0F) {
				angle = -angle;
				angleTotal += angle;
			}

			for (int k = 7; k >= 0; k--) {
				final int idx = i * 8 + k;
				if (idx == bound) {
					nodeList[idx] = new Panel(0.0F, AURORA_HEIGHT, 0.0F);
					angles[idx] = angle;
				} else {
					final Panel node = nodeList[idx + 1];
					final float subAngle = angles[idx + 1] + angle;
					final float subAngleRads = MathStuff.toRadians(subAngle);
					final float z = node.posZ - (MathStuff.sin(subAngleRads) * this.nodeLength);
					final float x = node.posX - (MathStuff.cos(subAngleRads) * this.nodeLength);

					nodeList[idx] = new Panel(x, AURORA_HEIGHT, z);
					angles[idx] = subAngle;
				}
			}
		}

		angleTotal = 0.0F;
		for (int j = this.length / 8 / 2; j < this.length / 8; j++) {
			float angle = (this.random.nextFloat() - 0.5F) * 8.0F;
			angleTotal += angle;
			if (MathStuff.abs(angleTotal) > 180.0F) {
				angle = -angle;
				angleTotal += angle;
			}
			for (int h = 0; h < 8; h++) {
				final int idx = j * 8 + h - 1;
				final Panel node = nodeList[idx];
				final float subAngle = angles[idx] + angle;
				final float subAngleRads = MathStuff.toRadians(subAngle);
				final float z = node.posZ + (MathStuff.sin(subAngleRads) * this.nodeLength);
				final float x = node.posX + (MathStuff.cos(subAngleRads) * this.nodeLength);
				nodeList[idx + 1] = new Panel(x, AURORA_HEIGHT, z);
				angles[idx + 1] = subAngle;
			}
		}

		return nodeList;
	}

}

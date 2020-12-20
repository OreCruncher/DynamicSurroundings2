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

/*
 * Implemented by an aurora so that it can go through it's life cycle.
 */
@OnlyIn(Dist.CLIENT)
public interface IAurora {

	/*
	 * Indicates if the aurora can be considered active
	 */
	boolean isAlive();

	/*
	 * Instructs the aurora to start the process of decay (i.e. start to fade)
	 */
	void setFading(final boolean flag);

	/*
	 * Indicates if the aurora is in the process of dying
	 */
	boolean isDying();

	/*
	 * Perform the necessary housekeeping for the aurora. Occurs once a tick.
	 */
	void update();

	/*
	 * Indicates if an aurora as completed it's life cycle and can be removed.
	 */
	boolean isComplete();

	/*
	 * Render the aurora to the client screen. It is possible that other updates can
	 * occur to the state, such as doing the transformations to animate.
	 */
	void render(final float partialTick);

}

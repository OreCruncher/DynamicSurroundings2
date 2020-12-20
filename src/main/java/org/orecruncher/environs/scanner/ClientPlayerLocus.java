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

package org.orecruncher.environs.scanner;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.orecruncher.environs.Environs;
import org.orecruncher.environs.handlers.CommonState;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerLocus extends ScanContext {

	public ClientPlayerLocus() {
		super(
				CommonState::getBlockReader,
				CommonState::getPlayerPosition,
				() -> Environs.LOGGER,
				CommonState::getDimensionId
		);
	}

}

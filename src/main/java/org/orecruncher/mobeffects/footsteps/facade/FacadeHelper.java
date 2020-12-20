/*
 * Dynamic Surroundings: Mob Effects
 * Copyright (C) 2019  OreCruncher
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.mobeffects.footsteps.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.mobeffects.MobEffects;

@OnlyIn(Dist.CLIENT)
public final class FacadeHelper {

	private static final Map<Block, IFacadeAccessor> crackers = new Reference2ObjectOpenHashMap<>();

	private static void addAccessor(@Nonnull final List<IFacadeAccessor> accessors,
			@Nonnull final IFacadeAccessor accessor) {
		if (accessor.isValid()) {
			MobEffects.LOGGER.info("Facade Accessor: %s", accessor.getName());
			accessors.add(accessor);
		}
	}

	static {

		final List<IFacadeAccessor> accessors = new ArrayList<>();

		addAccessor(accessors, new EnderIOFacadeAccessor());
		addAccessor(accessors, new CoFHCoreCoverAccessor());

		// Last hail mary - is this even supported anymore?
		addAccessor(accessors, new ChiselFacadeAccessor());

		// Iterate through the block list filling out our cracker list.
		if (accessors.size() > 0) {
			for (Block b : ForgeRegistries.BLOCKS) {
				for (final IFacadeAccessor accessor : accessors) {
					if (accessor.instanceOf(b)) {
						crackers.put(b, accessor);
						break;
					}
				}
			}
		}

	}

	protected FacadeHelper() {

	}

	@Nonnull
	public static BlockState resolveState(@Nonnull final LivingEntity entity, @Nonnull final BlockState state,
										  @Nonnull final IWorldReader world, @Nonnull final Vec3d pos, @Nullable final Direction side) {
		if (crackers.size() > 0 && state.getMaterial() != Material.AIR) {
			final IFacadeAccessor accessor = crackers.get(state.getBlock());
			if (accessor != null) {
				final BlockState newState = accessor.getBlockState(entity, state, world, pos, side);
				if (newState != null)
					return newState;
			}
		}
		return state;
	}

}

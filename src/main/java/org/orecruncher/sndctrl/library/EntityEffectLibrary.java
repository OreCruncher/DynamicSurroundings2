/*
 * Dynamic Surroundings: Sound Control
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

package org.orecruncher.sndctrl.library;

import java.util.Map;

import javax.annotation.Nonnull;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.orecruncher.sndctrl.Config;
import org.orecruncher.sndctrl.SoundControl;

@OnlyIn(Dist.CLIENT)
public final class EntityEffectLibrary {

	private static final EntityEffectInfo DEFAULT = new EntityEffectInfo();
	private static final Object2ObjectOpenHashMap<ResourceLocation, EntityEffectInfo> myEffects = new Object2ObjectOpenHashMap<>();
	private static final Map<Class<? extends Entity>, EntityEffectInfo> effects = new Reference2ObjectOpenHashMap<>();
	private static EntityEffectInfo playerEffects = DEFAULT;

	private EntityEffectLibrary() {

	}

	public static void initialize() {

	}

	public static void complete() {
		if (Config.CLIENT.logging.enableLogging.get()) {
			SoundControl.LOGGER.info("Entity Effect Configuration");
			SoundControl.LOGGER.info("===========================");
			for (final Map.Entry<ResourceLocation, EntityEffectInfo> kvp : myEffects.entrySet()) {
				SoundControl.LOGGER.info("%s = %s", kvp.getKey().toString(), kvp.getValue().toString());
			}
		}
	}

	@Nonnull
	public static EntityEffectInfo getEffects(@Nonnull final Entity entity) {
		if (entity instanceof PlayerEntity)
			return playerEffects;

		EntityEffectInfo info = effects.get(entity.getClass());
		if (info == null) {
			info = myEffects.get(entity.getType().getRegistryName());
			if (info == null) {
				// Slow crawl through looking for aliasing
				for (final Map.Entry<Class<? extends Entity>, EntityEffectInfo> kvp : effects.entrySet()) {
					if (kvp.getKey().isAssignableFrom(entity.getClass())) {
						info = kvp.getValue();
						break;
					}
				}
				// If it is null we didn't find a class hit so assume default
				if (info == null)
					info = DEFAULT;
			}
			effects.put(entity.getClass(), info);
		}
		return info;
	}

}

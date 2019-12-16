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

package org.orecruncher.sndctrl.capabilities;

import javax.annotation.Nonnull;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.lib.capability.NullStorage;
import org.orecruncher.lib.capability.SerializableProvider;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.capabilities.entityfx.EntityFXData;
import org.orecruncher.sndctrl.capabilities.entityfx.IEntityFX;

public class CapabilityEntityFXData {

	@CapabilityInject(IEntityFX.class)
	public static final Capability<IEntityFX> FX_INFO = null;
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(SoundControl.MOD_ID, "entityfx");

	@OnlyIn(Dist.CLIENT)
	public static void register() {
		CapabilityManager.INSTANCE.register(IEntityFX.class, new NullStorage<IEntityFX>(), EntityFXData::new);
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static ICapabilityProvider createProvider(final IEntityFX data) {
		return new SerializableProvider<>(FX_INFO, null, data);
	}

	@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class EventHandler {
		@SubscribeEvent
		public static void attachCapabilities(@Nonnull final AttachCapabilitiesEvent<Entity> event) {
			final World world = event.getObject().getEntityWorld();
			if (world.isRemote && event.getObject() instanceof LivingEntity) {
				final EntityFXData info = new EntityFXData();
				event.addCapability(CAPABILITY_ID, createProvider(info));
			}
		}
	}

}

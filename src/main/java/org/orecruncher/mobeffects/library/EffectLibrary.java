/*
 *  Dynamic Surroundings: Mob Effects
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

package org.orecruncher.mobeffects.library;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.resource.IResourceAccessor;
import org.orecruncher.lib.resource.ResourceUtils;
import org.orecruncher.lib.service.ClientServiceManager;
import org.orecruncher.lib.service.IClientService;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.library.config.EntityConfig;
import org.orecruncher.sndctrl.api.acoustics.Library;
import org.orecruncher.sndctrl.api.sound.SoundBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(
        modid = MobEffects.MOD_ID,
        value = {Dist.CLIENT},
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EffectLibrary {

    private static final IModLog LOGGER = MobEffects.LOGGER.createChild(EffectLibrary.class);

    private static final ResourceLocation PLAYER = new ResourceLocation("minecraft:player");
    private static final EntityEffectInfo DEFAULT = new EntityEffectInfo();
    private static EntityEffectInfo playerEffects = DEFAULT;
    private static final Reference2ObjectOpenHashMap<Class<? extends Entity>, EntityEffectInfo> effects = new Reference2ObjectOpenHashMap<>();
    private static final Set<ResourceLocation> blockedSounds = new ObjectOpenHashSet<>();

    private static final Map<ResourceLocation, SoundEvent> soundReplace = new Object2ObjectOpenHashMap<>();

    private EffectLibrary() {

    }

    static void initialize() {
        ClientServiceManager.instance().add(new EffectLibraryService());
    }

    public static boolean hasEffect(@Nonnull final Entity entity, @Nonnull final ResourceLocation loc) {
        return getEffectInfo(entity).effects.contains(loc);
    }

    @Nonnull
    private static EntityEffectInfo getEffectInfo(@Nonnull final Entity entity) {
        if (entity instanceof PlayerEntity)
            return playerEffects;
        return effects.get(entity.getClass());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void soundPlay(@Nonnull PlaySoundEvent e) {
        final ISound theSound = e.getSound();
        if (theSound != null) {
            final ResourceLocation soundResource = theSound.getSoundLocation();
            if (blockedSounds.contains(soundResource)) {
                e.setResultSound(null);
            } else {
                final SoundEvent evt = soundReplace.get(soundResource);
                if (evt != null) {
                    e.setResultSound(SoundBuilder.builder(evt).from((LocatableSound) theSound).build());
                }
            }
        }
    }

    private static class EffectLibraryService implements IClientService {

        @Override
        public void start() {

            final Map<ResourceLocation, Class<? extends Entity>> entities = ForgeUtils.getRegisteredEntities();

            final Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "mobeffects.json");

            for (final IResourceAccessor accessor : configs) {
                LOGGER.debug("Loading configuration %s", accessor.location());
                try {
                    Map<String, EntityConfig> cfg = accessor.as(EffectLibrary.entityConfigType);
                    for (final Map.Entry<String, EntityConfig> kvp : cfg.entrySet()) {
                        final EntityEffectInfo eei = new EntityEffectInfo(kvp.getValue());
                        final ResourceLocation loc = Library.resolveResource(MobEffects.MOD_ID, kvp.getKey());

                        if (loc.equals(PLAYER)) {
                            playerEffects = eei;
                        } else if (entities.containsKey(loc)) {
                            final Class<? extends Entity> clazz = entities.get(loc);
                            effects.put(clazz, eei);
                            entities.remove(loc);
                        }

                        for (final String r : kvp.getValue().blockedSounds) {
                            try {
                                blockedSounds.add(new ResourceLocation(r));
                            } catch (@Nonnull final Throwable t) {
                                MobEffects.LOGGER.error(t, "Not a valid sound resource location: %s", r);
                            }
                        }
                    }
                } catch (@Nonnull final Throwable t) {
                    LOGGER.error(t, "Unable to load %s", accessor.location());
                }
            }

            // Process the other entries in the entity list looking for fuzzy matches
            for(final Map.Entry<ResourceLocation, Class<? extends Entity>> kvp : entities.entrySet()) {
                if (!effects.containsKey(kvp.getValue())) {
                    // Not found. Scan our list looking for those that can be assigned
                    for (final Map.Entry<Class<? extends Entity>, EntityEffectInfo> e : effects.entrySet()) {
                        if (e.getKey().isAssignableFrom(kvp.getValue())) {
                            effects.put(kvp.getValue(), e.getValue());
                            break;
                        }
                    }
                }
            }

            // The default
            effects.defaultReturnValue(DEFAULT);

            // Replace our bow loose sounds
            final ResourceLocation bowLoose = new ResourceLocation(MobEffects.MOD_ID, "bow.loose");
            Library.getSound(bowLoose).ifPresent(se -> {
                soundReplace.put(new ResourceLocation("minecraft:entity.arrow.shoot"), se);
                soundReplace.put(new ResourceLocation("minecraft:entity.skeleton.shoot"), se);
            });
        }

        @Override
        public void stop() {
            effects.clear();
            blockedSounds.clear();
            soundReplace.clear();
        }
    }

    private static final ParameterizedType entityConfigType = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{String.class, EntityConfig.class};
        }

        @Override
        public Type getRawType() {
            return Map.class;
        }

        @Override
        @Nullable
        public Type getOwnerType() {
            return null;
        }
    };

}
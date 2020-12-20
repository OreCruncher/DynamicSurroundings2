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

package org.orecruncher.environs.handlers;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.library.BiomeInfo;
import org.orecruncher.environs.library.BiomeLibrary;
import org.orecruncher.environs.scanner.BiomeScanner;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.audio.AudioEngine;

import javax.annotation.Nonnull;
import java.util.Collection;

@OnlyIn(Dist.CLIENT)
public class BiomeSoundEffects extends HandlerBase {

    public static final int SCAN_INTERVAL = 4;

    // Reusable map for biome acoustic work
    private static final Reference2FloatOpenHashMap<IAcoustic> WORK_MAP = new Reference2FloatOpenHashMap<>(8, 1F);

    static {
        WORK_MAP.defaultReturnValue(0F);
    }

    private final BiomeScanner biomes = new BiomeScanner();
    private final ObjectArray<BackgroundAcousticEmitter> emitters = new ObjectArray<>(8);

    BiomeSoundEffects() {
        super("Biome Sounds");
    }

    @Override
    public boolean doTick(final long tick) {
        return CommonState.getDimensionInfo().playBiomeSounds();
    }

    private boolean doBiomeSounds() {
        return CommonState.isUnderground()
                || !CommonState.isInside()
                || CommonState.getDimensionInfo().alwaysOutside();
    }

    private void generateBiomeSounds() {
        final float area = this.biomes.getBiomeArea();
        for (final Reference2IntMap.Entry<BiomeInfo> kvp : this.biomes.getBiomes().reference2IntEntrySet()) {
            final Collection<IAcoustic> acoustics = kvp.getKey().findSoundMatches();
            final float volume = 0.05F + 0.95F * (kvp.getIntValue() / area);
            for (final IAcoustic acoustic : acoustics) {
                WORK_MAP.addTo(acoustic, volume);
            }
        }
    }

    @Override
    public void process(@Nonnull final PlayerEntity player) {
        this.emitters.forEach(BackgroundAcousticEmitter::tick);
        if ((TickCounter.getTickCount() % SCAN_INTERVAL) == 0) {
            this.biomes.tick();
            handleBiomeSounds(player);
        }
    }

    @Override
    public void onConnect() {
        clearSounds();
    }

    @Override
    public void onDisconnect() {
        clearSounds();
    }

    private void handleBiomeSounds(@Nonnull final PlayerEntity player) {
        this.biomes.tick();
        WORK_MAP.clear();

        // Only gather data if the player is alive. If the player is dead the biome sounds will cease playing.
        if (player.isAlive()) {

            final boolean biomeSounds = doBiomeSounds();

            if (biomeSounds)
                generateBiomeSounds();

            final ObjectArray<IAcoustic> playerSounds = new ObjectArray<>();
            BiomeLibrary.PLAYER_INFO.findSoundMatches(playerSounds);
            BiomeLibrary.VILLAGE_INFO.findSoundMatches(playerSounds);
            playerSounds.forEach(fx -> WORK_MAP.put(fx, 1.0F));

            if (biomeSounds) {
                final BiomeInfo playerBiome = CommonState.getPlayerBiome();
                final IAcoustic sound = playerBiome.getSpotSound(RANDOM);
                if (sound != null)
                    sound.playNear(player);
            }

            final IAcoustic sound = BiomeLibrary.PLAYER_INFO.getSpotSound(RANDOM);
            if (sound != null)
                sound.playNear(player);
        }

        queueAmbientSounds();
    }

    private void queueAmbientSounds() {
        // Iterate through the existing emitters:
        // * If done, remove
        // * If not in the incoming list, fade
        // * If it does exist, update volume throttle and unfade if needed
        this.emitters.removeIf(entry -> {
            if (entry.isDonePlaying()) {
                return true;
            }
            final float volume = WORK_MAP.getFloat(entry.getSource());
            if (volume > 0) {
                entry.setVolumeThrottle(volume);
                if (entry.isFading())
                    entry.unfade();
                WORK_MAP.removeFloat(entry.getSource());
            } else if (!entry.isFading()) {
                entry.fade();
            }
            return false;
        });

        // Any sounds left in the list are new and need an emitter created.
        WORK_MAP.forEach((fx, volume) -> {
            final BackgroundAcousticEmitter e = new BackgroundAcousticEmitter(fx);
            e.setVolumeThrottle(volume);
            this.emitters.add(e);
        });
    }

    public void clearSounds() {
        this.emitters.forEach(BackgroundAcousticEmitter::stop);
        this.emitters.clear();
        WORK_MAP.clear();
        AudioEngine.stopAll();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void diagnostics(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.get_enableLogging())
            this.emitters.forEach(backgroundAcousticEmitter -> event.getLeft().add("EMITTER: " + backgroundAcousticEmitter.toString()));
    }
}

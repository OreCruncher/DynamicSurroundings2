/*
 * Dynamic Surroundings: Environs
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

package org.orecruncher.environs;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Environs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    @Nonnull
    public static final Client CLIENT;
    private static final String CLIENT_CONFIG = Environs.MOD_ID + File.separator + Environs.MOD_ID + "-client.toml";
    @Nonnull
    private static final ForgeConfigSpec clientSpec;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    private Config() {
    }

    private static void applyConfig() {
        CLIENT.update();
        Environs.LOGGER.setDebug(Config.CLIENT.logging.get_enableLogging());
        Environs.LOGGER.setTraceMask(Config.CLIENT.logging.get_flagMask());
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        applyConfig();
        Environs.LOGGER.debug("Loaded config file %s", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading configEvent) {
        Environs.LOGGER.debug("Config file changed %s", configEvent.getConfig().getFileName());
        applyConfig();
    }

    public static void setup() {
        // The subdir with the mod ID name should have already been created
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec, CLIENT_CONFIG);
    }

    public static class Client {

        public final Logging logging;
        public final Biome biome;
        public final Effects effects;
        public final Aurora aurora;
        public final Fog fog;
        public final Sound sound;

        Client(@Nonnull final ForgeConfigSpec.Builder builder) {
            this.logging = new Logging(builder);
            this.biome = new Biome(builder);
            this.effects = new Effects(builder);
            this.aurora = new Aurora(builder);
            this.fog = new Fog(builder);
            this.sound = new Sound(builder);
        }

        void update() {
            this.logging.update();
            this.biome.update();
            this.effects.update();
            this.aurora.update();
            this.fog.update();
            this.sound.update();
        }

        public static class Logging {

            private final BooleanValue enableLogging;
            private final BooleanValue onlineVersionCheck;
            private final IntValue flagMask;

            private boolean _enableLogging;
            private boolean _onlineVersionCheck;
            private int _flagMask;

            Logging(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines how logging will behave")
                        .push("Logging Options");

                this.enableLogging = builder
                        .comment("Enables/disables debug logging of the mod")
                        .translation("environs.cfg.logging.EnableDebug")
                        .define("Debug Logging", false);

                this.onlineVersionCheck = builder
                        .comment("Enables/disables display of version check information")
                        .translation("environs.cfg.logging.VersionCheck")
                        .define("Online Version Check Result", true);

                this.flagMask = builder
                        .comment("Bitmask for toggling various debug traces")
                        .translation("environs.cfg.logging.FlagMask")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);

                builder.pop();
            }

            void update() {
                this._enableLogging = this.enableLogging.get();
                this._onlineVersionCheck = this.onlineVersionCheck.get();
                this._flagMask = this.flagMask.get();
            }

            public boolean get_enableLogging() {
                return this._enableLogging;
            }

            public boolean get_onlineVersionCheck() {
                return this._onlineVersionCheck;
            }

            public int get_flagMask() {
                return this._flagMask;
            }
        }

        public static class Biome {

            private final IntValue worldSealevelOverride;
            private final ForgeConfigSpec.ConfigValue<List<? extends Integer>> biomeSoundBlacklist;

            private int _worldSealevelOverride;
            private IntOpenHashSet _biomeSoundBlacklist = new IntOpenHashSet();

            Biome(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Options for controlling biome sound/effects")
                        .push("Biome Options");

                this.worldSealevelOverride = builder
                        .comment("Sealevel to set for Overworld (0 use default for World)")
                        .translation("environs.cfg.biome.Sealevel")
                        .defineInRange("Overworld Sealevel Override", 0, 0, 256);

                this.biomeSoundBlacklist = builder
                        .comment("Dimension IDs where biome sounds will not be played")
                        .translation("environs.cfg.biome.DimBlackList")
                        .defineList("Dimension Blacklist", ArrayList::new, s -> true);

                builder.pop();
            }

            public void update() {
                this._worldSealevelOverride = this.worldSealevelOverride.get();
                this._biomeSoundBlacklist = new IntOpenHashSet(this.biomeSoundBlacklist.get());
            }

            public int get_worldSealevelOverride() {
                return this._worldSealevelOverride;
            }

            public IntOpenHashSet get_biomeSoundBlacklist() {
                return this._biomeSoundBlacklist;
            }
        }

        public static class Effects {

            private final BooleanValue enableFireFlies;
            private final BooleanValue enableSteamJets;
            private final BooleanValue enableFireJets;
            private final BooleanValue enableBubbleJets;
            private final BooleanValue enableDustJets;
            private final BooleanValue enableFountainJets;
            private final BooleanValue enableWaterSplashJets;
            private final BooleanValue disableUnderwaterParticles;

            private boolean _enableFireFlies;
            private boolean _enableSteamJets;
            private boolean _enableFireJets;
            private boolean _enableBubbleJets;
            private boolean _enableDustJets;
            private boolean _enableFountainJets;
            private boolean _enableWaterSplashJets;
            private boolean _disableUnderwaterParticles;

            Effects(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Options for controlling various effects")
                        .push("Effect Options");

                this.enableFireFlies = builder
                        .worldRestart()
                        .comment("Enable/disable Firefly effect around plants")
                        .translation("environs.cfg.effects.Fireflies")
                        .define("Fireflies", true);

                this.enableSteamJets = builder
                        .worldRestart()
                        .comment("Enable/disable Steam Jets where lava meets water")
                        .translation("environs.cfg.effects.Steam")
                        .define("Steam Jets", true);

                this.enableFireJets = builder
                        .worldRestart()
                        .comment("Enable/disable Fire Jets in lava")
                        .translation("environs.cfg.effects.Fire")
                        .define("Fire Jets", true);

                this.enableBubbleJets = builder
                        .worldRestart()
                        .comment("Enable/disable Bubble Jets under water")
                        .translation("environs.cfg.effects.Bubble")
                        .define("Bubble Jets", true);

                this.enableDustJets = builder
                        .worldRestart()
                        .comment("Enable/disable Dust Motes dropping from under blocks")
                        .translation("environs.cfg.effects.Dust")
                        .define("Dust Jets", true);

                this.enableFountainJets = builder
                        .worldRestart()
                        .comment("Enable/disable Fountain Jets spraying")
                        .translation("environs.cfg.effects.Fountain")
                        .define("Fountain Jets", true);

                this.enableWaterSplashJets = builder
                        .worldRestart()
                        .comment("Enable/disable Water Splash effects when water spills down")
                        .translation("environs.cfg.effects.Splash")
                        .define("Water Splash", true);

                this.disableUnderwaterParticles = builder
                        .worldRestart()
                        .comment("Enable/disable Minecrafts Underwater particle effect")
                        .translation("environs.cfg.effects.Underwater")
                        .define("Disable Underwater Particles", false);

                builder.pop();
            }

            public void update() {
                this._enableFireFlies = this.enableFireFlies.get();
                this._enableBubbleJets = this.enableBubbleJets.get();
                this._enableFireJets = this.enableFireJets.get();
                this._enableSteamJets = this.enableSteamJets.get();
                this._enableDustJets = this.enableDustJets.get();
                this._enableFountainJets = this.enableFountainJets.get();
                this._enableWaterSplashJets = this.enableWaterSplashJets.get();
                this._disableUnderwaterParticles = this.disableUnderwaterParticles.get();
            }

            // Reach over and grab from SoundControl
            public int get_effectRange() {
                return org.orecruncher.sndctrl.Config.CLIENT.effects.get_effectRange();
            }

            public boolean get_enableFireFlies() {
                return false;
                //return this._enableFireFlies;
            }

            public boolean get_enableSteamJets() {
                return this._enableSteamJets;
            }

            public boolean get_enableFireJets() {
                return this._enableFireJets;
            }

            public boolean get_enableBubbleJets() {
                return this._enableBubbleJets;
            }

            public boolean get_enableDustJets() {
                return false;
                //return this._enableDustJets;
            }

            public boolean get_enableFountainJets() {
                return this._enableFountainJets;
            }

            public boolean get_enableWaterSplashJets() {
                return false;
                //return this._enableWaterSplashJets;
            }

            public boolean get_disableUnderwaterParticles() {
                return this._disableUnderwaterParticles;
            }

        }

        public static class Aurora {

            private final BooleanValue auroraEnabled;
            private final IntValue maxBands;

            private boolean _auroraEnabled;
            private int _maxBands;

            Aurora(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Options for controlling various effects")
                        .push("Effect Options");

                this.auroraEnabled = builder
                        .worldRestart()
                        .comment("Enable/disable Aurora processing")
                        .translation("environs.cfg.aurora.Enable")
                        .define("Auroras", true);

                this.maxBands = builder
                        .worldRestart()
                        .comment("Cap the maximum bands that will be rendered")
                        .translation("environs.cfg.aurora.MaxBands")
                        .defineInRange("Maximum Bands", 3, 0, 3);

                builder.pop();
            }

            public void update() {
                this._auroraEnabled = this.auroraEnabled.get();
                this._maxBands = this.maxBands.get();
            }

            public boolean get_auroraEnabled() {
                return false;
                //return this._auroraEnabled;
            }

            public int get_maxBands() {
                return this._maxBands;
            }
        }

        public static class Fog {

            private final BooleanValue enableFog;
            private final BooleanValue enableBiomeFog;
            private final BooleanValue enableElevationHaze;
            private final BooleanValue enableMorningFog;
            private final BooleanValue enableBedrockFog;
            private final BooleanValue enableWeatherFog;
            private final IntValue morningFogChance;

            private boolean _enableFog;
            private boolean _enableBiomeFog;
            private boolean _enableElevationHaze;
            private boolean _enableMorningFog;
            private boolean _enableBedrockFog;
            private boolean _enableWeatherFog;
            private int _morningFogChance;

            Fog(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Options that control the various fog effects in the client")
                        .push("Fog Options");

                this.enableFog = builder
                        .worldRestart()
                        .comment("Enable/disable fog processing")
                        .translation("environs.cfg.fog.Enable")
                        .define("Enable Fog Processing", true);

                this.enableBiomeFog = builder
                        .worldRestart()
                        .comment("Enable biome specific fog density and color")
                        .translation("environs.cfg.fog.Biome")
                        .define("Biome Fog", true);

                this.enableElevationHaze = builder
                        .worldRestart()
                        .comment("Higher the player elevation the more haze that is experienced")
                        .translation("environs.cfg.fog.Haze")
                        .define("Elevation Haze", true);

                this.enableMorningFog = builder
                        .worldRestart()
                        .comment("Show morning fog that eventually burns off")
                        .translation("environs.cfg.fog.Morning")
                        .define("Morning Fog", true);

                this.enableBedrockFog = builder
                        .worldRestart()
                        .comment("Increase fog at bedrock layers")
                        .translation("environs.cfg.fog.Bedrock")
                        .define("Bedrock Fog", true);

                this.enableWeatherFog = builder
                        .worldRestart()
                        .comment("Increase fog based on the strength of rain")
                        .translation("environs.cfg.fog.Weather")
                        .define("Weather Fog", true);

                this.morningFogChance = builder
                        .worldRestart()
                        .comment("Chance morning fog will occurs expressed as 1 in N")
                        .translation("environs.cfg.fog.MorningChance")
                        .defineInRange("Morning Fog Chance", 1, 0, Integer.MAX_VALUE);

                builder.pop();
            }

            public void update() {
                this._enableFog = this.enableFog.get();
                this._enableBiomeFog = this.enableBiomeFog.get();
                this._enableElevationHaze = this.enableElevationHaze.get();
                this._enableMorningFog = this.enableMorningFog.get();
                this._enableBedrockFog = this.enableBedrockFog.get();
                this._enableWeatherFog = this.enableWeatherFog.get();
                this._morningFogChance = this.morningFogChance.get();
            }

            public boolean get_enableFog() {
                return this._enableFog;
            }

            public boolean get_enableBiomeFog() {
                return this._enableBiomeFog;
            }

            public boolean get_enableElevationHaze() {
                return this._enableElevationHaze;
            }

            public boolean get_enableMorningFog() {
                return this._enableMorningFog;
            }

            public boolean get_enableBedrockFog() {
                return this._enableBedrockFog;
            }

            public boolean get_enableWeatherFog() {
                return this._enableWeatherFog;
            }

            public int get_morningFogChance() {
                return this._morningFogChance;
            }
        }

        public static class Sound {

            private final IntValue biomeSoundVolume;
            private final IntValue spotSoundVolume;
            private float _biomeSoundVolume;
            private float _spotSoundVolume;

            Sound(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Options for defining sound behavior")
                        .push("Sound Options");

                this.biomeSoundVolume = builder
                        .comment("Scaling factor to apply to biome sounds")
                        .translation("environs.cfg.sound.BiomeVolume")
                        .defineInRange("Biome Sound Volume", 100, 0, 100);

                this.spotSoundVolume = builder
                        .comment("Scaling factor to apply to spot sounds")
                        .translation("environs.cfg.sound.SpotVolume")
                        .defineInRange("Spot Sound Volume", 100, 0, 100);

                builder.pop();
            }

            public void update() {
                this._biomeSoundVolume = this.biomeSoundVolume.get() / 100F;
                this._spotSoundVolume = this.spotSoundVolume.get() / 100F;
            }

            public float get_biomeSoundVolume() {
                return this._biomeSoundVolume;
            }

            public float get_spotSoundVolume() {
                return this._spotSoundVolume;
            }
        }
    }
}

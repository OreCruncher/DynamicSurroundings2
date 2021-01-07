/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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

package org.orecruncher.environs.config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.environs.Environs;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Environs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    public static final Client CLIENT;
    private static final String CLIENT_CONFIG = DynamicSurroundings.MOD_ID + File.separator + Environs.MOD_ID + "-client.toml";
    public static final ForgeConfigSpec SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    private Config() {
    }

    private static void applyConfig() {
        Environs.LOGGER.setDebug(Config.CLIENT.logging.enableLogging.get());
        Environs.LOGGER.setTraceMask(Config.CLIENT.logging.flagMask.get());
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
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC, CLIENT_CONFIG);
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

        public static class Logging {

            public final BooleanValue enableLogging;
            public final IntValue flagMask;

            Logging(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines how logging will behave")
                        .push("Logging Options");

                this.enableLogging = builder
                        .comment("Enables/disables debug logging of the mod")
                        .translation("environs.cfg.logging.EnableDebug")
                        .define("Debug Logging", false);

                this.flagMask = builder
                        .comment("Bitmask for toggling various debug traces")
                        .translation("environs.cfg.logging.FlagMask")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);

                builder.pop();
            }
        }

        public static class Biome {

            public final IntValue worldSealevelOverride;
            public final ForgeConfigSpec.ConfigValue<List<? extends String>> biomeSoundBlacklist;

            Biome(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Options for controlling biome sound/effects")
                        .push("Biome Options");

                this.worldSealevelOverride = builder
                        .comment("Sealevel to set for Overworld (0 use default for World)")
                        .translation("environs.cfg.biomes.Sealevel")
                        .defineInRange("Overworld Sealevel Override", 0, 0, 256);

                this.biomeSoundBlacklist = builder
                        .comment("Dimension IDs where biome sounds will not be played")
                        .translation("environs.cfg.biomes.DimBlacklist")
                        .defineList("Dimension Blacklist", ArrayList::new, s -> true);

                builder.pop();
            }
        }

        public static class Effects {

            public final BooleanValue enableFireFlies;
            public final BooleanValue enableSteamJets;
            public final BooleanValue enableFireJets;
            public final BooleanValue enableBubbleJets;
            public final BooleanValue enableDustJets;
            public final BooleanValue enableFountainJets;
            public final BooleanValue enableWaterfalls;
            public final BooleanValue disableUnderwaterParticles;

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

                this.enableWaterfalls = builder
                        .worldRestart()
                        .comment("Enable/disable Water Splash effects when water spills down")
                        .translation("environs.cfg.effects.Splash")
                        .define("Waterfall Splash", true);

                this.disableUnderwaterParticles = builder
                        .worldRestart()
                        .comment("Enable/disable Minecraft's Underwater particle effect")
                        .translation("environs.cfg.effects.Underwater")
                        .define("Disable Underwater Particles", false);

                builder.pop();
            }

            // Reach over and grab from SoundControl
            public int get_effectRange() {
                return org.orecruncher.sndctrl.config.Config.CLIENT.effects.effectRange.get();
            }

        }

        public static class Aurora {

            public final BooleanValue auroraEnabled;
            public final IntValue maxBands;

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
        }

        public static class Fog {

            public final BooleanValue enableFog;
            public final BooleanValue enableBiomeFog;
            public final BooleanValue enableElevationHaze;
            public final BooleanValue enableMorningFog;
            public final BooleanValue enableBedrockFog;
            public final BooleanValue enableWeatherFog;
            public final IntValue morningFogChance;

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
        }

        public static class Sound {

            public final IntValue biomeSoundVolume;
            public final IntValue spotSoundVolume;
            public final IntValue waterfallSoundVolume;

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

                this.waterfallSoundVolume = builder
                        .comment("Scaling factor to apply to waterfall sounds")
                        .translation("environs.cfg.sound.WaterfallVolume")
                        .defineInRange("Waterfall Volume", 100, 0, 100);

                builder.pop();
            }
        }
    }
}

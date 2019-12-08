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

package org.orecruncher.sndctrl;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    @Nonnull
    public static final Client CLIENT;
    private static final String CLIENT_CONFIG = SoundControl.MOD_ID + File.separator + SoundControl.MOD_ID + "-client.toml";
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
        SoundControl.LOGGER.setDebug(Config.CLIENT.logging.enableLogging.get());
        SoundControl.LOGGER.setTraceMask(Config.CLIENT.logging.flagMask.get());
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        applyConfig();
        SoundControl.LOGGER.debug("Loaded config file %s", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.ConfigReloading configEvent) {
        SoundControl.LOGGER.debug("Config file changed %s", configEvent.getConfig().getFileName());
        applyConfig();
    }

    public static void setup() {
        // The subdir with the mod ID name should have already been created
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec, CLIENT_CONFIG);
    }

    public static class Trace {
        public static final int SOUND_PLAY = 0x1;
        public static final int BASIC_SOUND_PLAY = 0x2;
    }

    public static class Client {

        //@formatter:off
        private static final List<String> defaultSoundConfig = ImmutableList.<String>builder()
                .add("minecraft:block.water.ambient cull")
                .add("minecraft:block.lava.ambient cull")
                .add("minecraft:entity.sheep.ambient cull")
                .add("minecraft:entity.chicken.ambient cull")
                .add("minecraft:entity.cow.ambient cull")
                .add("minecraft:entity.pig.ambient cull")
                .add("minecraft:entity.llama.ambient cull")
                .add("minecraft:entity.wither.death cull 25")
                .add("minecraft:entity.ender_dragon.death cull 25")
                .build();

        private static final List<String> defaultStartupSounds = ImmutableList.<String>builder()
                .add("minecraft:entity.experience_orb.pickup")
                .add("minecraft:entity.chicken.egg")
                .add("minecraft:ambient.underwater.exit")
                .build();
        //@formatter:on

        @Nonnull
        public final Sound sound;
        @Nonnull
        public final Logging logging;

        Client(@Nonnull final ForgeConfigSpec.Builder builder) {
            this.sound = new Sound(builder);
            this.logging = new Logging(builder);
        }

        public static class Sound {

            public final BooleanValue enableEnhancedSounds;
            public final BooleanValue muteInBackground;
            public final IntValue cullInterval;
            public final IntValue backgroundThreadWorkers;
            public final ConfigValue<List<? extends String>> individualSounds;
            public final ConfigValue<List<? extends String>> startupSoundList;

            Sound(@Nonnull final ForgeConfigSpec.Builder builder) {
                //@formatter:off
                builder.comment("General options for defining sound effects")
                        .push("Sound Options");

                this.enableEnhancedSounds = builder
                        .worldRestart()
                        .comment("Enable sound reverb and filtering")
                        .translation("sndctrl.cfg.sound.EnhancedSounds")
                        .define("Enable Enhanced Sounds", true);

                this.individualSounds = builder
                        .comment("Options to configure sounds on an individual basis")
                        .translation("sndctrl.cfg.sound.Individual")
                        .defineList("Individual Sound Config", defaultSoundConfig, s -> true);

                this.startupSoundList = builder
                        .comment("Possible sounds to play when client reaches main game menu")
                        .translation("sndctrl.cfg.sound.StartupSounds")
                        .defineList("Startup Sound List", defaultStartupSounds, s -> true);

                this.muteInBackground = builder
                        .comment("Mute sound when Minecraft is in the background")
                        .translation("sndctrl.cfg.sound.Mute")
                        .define("Mute when in Background", true);

                this.cullInterval = builder
                        .comment("Ticks between culled sound events (0 to disable culling)")
                        .translation("sndctrl.cfg.sound.CullInterval")
                        .defineInRange("Sound Culling Interval", 20, 0, Integer.MAX_VALUE);

                this.backgroundThreadWorkers = builder
                        .comment("Number of background threads to handle sound effect calculations (0 is default)")
                        .translation("sndctrl.cfg.sound.threads")
                        .worldRestart()
                        .defineInRange("Background Workers", 0, 0, 8);

                builder.pop();
                //@formatter:on
            }
        }

        public static class Logging {

            public final BooleanValue enableLogging;
            public final BooleanValue onlineVersionCheck;
            public final IntValue flagMask;

            Logging(@Nonnull final ForgeConfigSpec.Builder builder) {
                //@formatter:off
                builder.comment("Defines how Sound Control logging will behave")
                        .push("Logging Options");

                this.enableLogging = builder
                        .comment("Enables/disables debug logging of the mod")
                        .translation("sndctrl.cfg.logging.EnableDebug")
                        .define("Debug Logging", false);

                this.onlineVersionCheck = builder
                        .comment("Enables/disables display of version check information")
                        .translation("sndctrl.cfg.logging.VersionCheck")
                        .define("Online Version Check Result", true);

                this.flagMask = builder
                        .comment("Bitmask for toggling various debug traces")
                        .translation("sndctrl.cfg.logging.FlagMask")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);

                builder.pop();
                //@formatter:on
            }
        }
    }
}

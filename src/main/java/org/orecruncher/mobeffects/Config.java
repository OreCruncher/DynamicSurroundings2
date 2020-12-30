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

package org.orecruncher.mobeffects;

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
import org.orecruncher.mobeffects.footsteps.FootprintStyle;

import javax.annotation.Nonnull;
import java.io.File;

@Mod.EventBusSubscriber(modid = MobEffects.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    public static final Client CLIENT;
    private static final String CLIENT_CONFIG = DynamicSurroundings.MOD_ID + File.separator + MobEffects.MOD_ID + "-client.toml";
    public static final ForgeConfigSpec SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    private Config() {
    }

    private static void applyConfig() {
        CLIENT.update();
        MobEffects.LOGGER.setDebug(Config.CLIENT.logging.get_enableLogging());
        MobEffects.LOGGER.setTraceMask(Config.CLIENT.logging.get_flagMask());
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        applyConfig();
        MobEffects.LOGGER.debug("Loaded config file %s", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading configEvent) {
        MobEffects.LOGGER.debug("Config file changed %s", configEvent.getConfig().getFileName());
        applyConfig();
    }

    public static void setup() {
        // The subdir with the mod ID name should have already been created
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC, CLIENT_CONFIG);
    }

    public static class Client {

        public final Logging logging;
        public final Footsteps footsteps;
        public final Effects effects;

        Client(@Nonnull final ForgeConfigSpec.Builder builder) {
            this.logging = new Logging(builder);
            this.footsteps = new Footsteps(builder);
            this.effects = new Effects(builder);
        }

        void update() {
            this.logging.update();
            this.footsteps.update();
            this.effects.update();
        }

        public static class Logging {

            public final BooleanValue enableLogging;
            public final IntValue flagMask;

            private boolean _enableLogging;
            private int _flagMask;

            Logging(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines how logging will behave")
                        .push("Logging Options");

                this.enableLogging = builder
                        .comment("Enables/disables debug logging of the mod")
                        .translation("mobeffects.cfg.logging.EnableDebug")
                        .define("Debug Logging", false);

                this.flagMask = builder
                        .comment("Bitmask for toggling various debug traces")
                        .translation("mobeffects.cfg.logging.FlagMask")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);

                builder.pop();
            }

            void update() {
                this._enableLogging = this.enableLogging.get();
                this._flagMask = this.flagMask.get();
            }

            public boolean get_enableLogging() {
                return this._enableLogging;
            }

            public int get_flagMask() {
                return this._flagMask;
            }
        }

        public static class Footsteps {

            public final BooleanValue enableFootprintParticles;
            public final BooleanValue firstPersonFootstepCadence;
            public final ForgeConfigSpec.EnumValue<FootprintStyle> playerFootprintStyle;
            public final BooleanValue footstepsAsQuadruped;
            public final IntValue footstepVolume;
            public final BooleanValue enableFootstepAccents;

            private boolean _enableFootprintParticles;
            private boolean _firstPersonFootstepCadence;
            private FootprintStyle _playerFootprintStyle = FootprintStyle.LOWRES_SQUARE;
            private boolean _footstepsAsQuadruped;
            private float _footstepVolumeScale;
            private boolean _enableFootstepAccents;

            public Footsteps(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines footstep effect generation parameters")
                        .push("Footstep Options");

                this.enableFootprintParticles = builder
                        .comment("Enable Footprint particle effects")
                        .translation("mobeffects.cfg.footsteps.Enable")
                        .define("Enable Footprint Particles", true);

                this.firstPersonFootstepCadence = builder
                        .comment("Use first person footstep cadence")
                        .translation("mobeffects.cfg.footsteps.Cadence")
                        .define("First Person Cadence", true);

                this.playerFootprintStyle = builder
                        .comment("Style of footprint to display for a player")
                        .translation("mobeffects.cfg.footsteps.PlayerStyle")
                        .defineEnum("Player Footprint Style", FootprintStyle.LOWRES_SQUARE);

                this.footstepsAsQuadruped = builder
                        .comment("Generate footsteps as a quadruped (horse)")
                        .translation("mobeffects.cfg.footsteps.Quadruped")
                        .define("Footsteps as Quadruped", false);

                this.footstepVolume = builder
                        .comment("Footstep master volume scale")
                        .translation("mobeffects.cfg.footsteps.Volume")
                        .defineInRange("Footstep Volume Scale", 75, 0, 100);

                this.enableFootstepAccents = builder
                        .comment("Enable armor rustle and rain splash accents for footstep acoustics")
                        .translation("mobeffects.cfg.footsteps.Accents")
                        .define("Enable Footstep Accents", true);

                builder.pop();
            }

            void update() {
                this._enableFootprintParticles = this.enableFootprintParticles.get();
                this._firstPersonFootstepCadence = this.firstPersonFootstepCadence.get();
                this._footstepsAsQuadruped = this.footstepsAsQuadruped.get();
                this._playerFootprintStyle = this.playerFootprintStyle.get();
                this._footstepVolumeScale = this.footstepVolume.get() / 100F;
                this._enableFootstepAccents = this.enableFootstepAccents.get();
            }

            public boolean get_enableFootprintParticles() {
                return this._enableFootprintParticles;
            }

            public boolean get_firstPersonFootstepCadence() {
                return this._firstPersonFootstepCadence;
            }

            public boolean get_footstepsAsQuadruped() {
                return this._footstepsAsQuadruped;
            }

            public FootprintStyle get_playerFootprintStyle() {
                return this._playerFootprintStyle;
            }

            public float get_footstepVolumeScale() {
                return this._footstepVolumeScale;
            }

            public boolean get_enableFootstepAccents() {
                return this._enableFootstepAccents;
            }
        }

        public static class Effects {

            public final BooleanValue hidePlayerPotionParticles;
            public final BooleanValue showBreath;
            public final BooleanValue showArrowTrail;
            public final BooleanValue enableToolbarEffect;
            public final BooleanValue enableBowEffect;
            public final BooleanValue enableSwingEffect;
            public final IntValue toolbarVolume;

            private boolean _hidePlayerPotionParticles;
            private boolean _showBreath;
            private boolean _showArrowTrail;
            private boolean _enableToolbarEffect;
            private boolean _enableBowEffect;
            private boolean _enableSwingEffect;
            private float _toolbarVolume;

            public Effects(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Options for mob effect generation")
                        .push("Mob Effect Options");

                this.hidePlayerPotionParticles = builder
                        .comment("Hides the player's potion particles to avoid cluttering display")
                        .translation("mobeffects.cfg.effects.PotionParticles")
                        .define("Hide Player Potion Particles", true);

                this.showBreath = builder
                        .worldRestart()
                        .comment("Show breath effect in cold regions and underwater")
                        .translation("mobeffects.cfg.effects.Breath")
                        .define("Show Breath Effect", true);

                this.showArrowTrail = builder
                        .comment("Show arrow particle trail during flight")
                        .translation("mobeffects.cfg.effects.Arrow")
                        .define("Show Arrow Particle Trail", false);

                this.enableToolbarEffect = builder
                        .worldRestart()
                        .comment("Enable toolbar item sound effects")
                        .translation("mobeffects.cfg.effects.Toolbar")
                        .define("Enable Toolbar Sound Effects", true);

                this.enableBowEffect = builder
                        .worldRestart()
                        .comment("Enable bow/crossbow sound effects")
                        .translation("mobeffects.cfg.effects.Bow")
                        .define("Enable Bow Sound Effects", true);

                this.enableSwingEffect = builder
                        .worldRestart()
                        .comment("Enable item swing sound effects")
                        .translation("mobeffects.cfg.effects.Swing")
                        .define("Enable Item Swing Sound Effects", true);

                this.toolbarVolume = builder
                        .comment("Toolbar master volume scale")
                        .translation("mobeffects.cfg.effects.ToolbarVolume")
                        .defineInRange("Toolbar Volume Scale", 35, 0, 100);

                builder.pop();
            }

            void update() {
                this._hidePlayerPotionParticles = this.hidePlayerPotionParticles.get();
                this._showBreath = this.showBreath.get();
                this._showArrowTrail = this.showArrowTrail.get();
                this._enableToolbarEffect = this.enableToolbarEffect.get();
                this._enableBowEffect = this.enableBowEffect.get();
                this._enableSwingEffect = this.enableSwingEffect.get();
                this._toolbarVolume = this.toolbarVolume.get() / 100F;
            }

            public boolean get_hidePlayerPotionParticles() {
                return this._hidePlayerPotionParticles;
            }

            public boolean get_showBreath() {
                return this._showBreath;
            }

            public boolean get_showArrowTrail() {
                return this._showArrowTrail;
            }

            public boolean get_enableToolbarEffect() {
                return this._enableToolbarEffect;
            }

            public boolean get_enableBowEffect() {
                return this._enableBowEffect;
            }

            public boolean get_enableSwingEffect() {
                return this._enableSwingEffect;
            }

            public float get_toolbarVolumeScale() {
                return this._toolbarVolume;
            }
        }
    }
}

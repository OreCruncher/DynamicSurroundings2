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

package org.orecruncher.environs.effects;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Config;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Describes the various types of block effects that can be generated.
 */
@OnlyIn(Dist.CLIENT)
public enum BlockEffectType {

    UNKNOWN("UNKNOWN", ignored -> null, () -> false),
    FIREFLY("firefly", FireFlyEffect::new, Config.CLIENT.effects::get_enableFireFlies),
    STEAM_JET("steam", SteamJetEffect::new, Config.CLIENT.effects::get_enableSteamJets),
    FIRE_JET("fire", FireJetEffect::new, Config.CLIENT.effects::get_enableFireJets),
    BUBBLE_JET("bubble", BubbleJetEffect::new, Config.CLIENT.effects::get_enableBubbleJets),
    DUST_JET("dust", DustJetEffect::new, Config.CLIENT.effects::get_enableDustJets),
    FOUNTAIN_JET("fountain", FountainJetEffect::new, Config.CLIENT.effects::get_enableFountainJets),
    SPLASH_JET("splash", WaterSplashJetEffect::new, Config.CLIENT.effects::get_enableWaterSplashJets);

    private static final Map<String, BlockEffectType> typeMap = new Object2ObjectOpenHashMap<>();
    static {
        for (final BlockEffectType effect : BlockEffectType.values())
            typeMap.put(effect.getName(), effect);
    }

    @Nonnull
    public static BlockEffectType get(@Nonnull final String name) {
        final BlockEffectType result = typeMap.get(name);
        return result == null ? BlockEffectType.UNKNOWN : result;
    }

    protected final String name;
    protected final Function<Integer, BlockEffect> factory;
    protected final Supplier<Boolean> enabled;

    BlockEffectType(@Nonnull final String name, @Nonnull final Function<Integer, BlockEffect> factory, @Nonnull final Supplier<Boolean> enabled) {
        this.name = name;
        this.enabled = enabled;
        this.factory = factory;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    public boolean isEnabled() {
        return this.enabled.get();
    }

    @Nonnull
    public Optional<BlockEffect> getInstance(final int chance) {
        return Optional.ofNullable(this.factory.apply(chance));
    }
}

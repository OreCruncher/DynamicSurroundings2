/*
 *  Dynamic Surroundings
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

import com.google.common.collect.Streams;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.BellTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.config.Config;
import org.orecruncher.environs.handlers.scripts.ConditionEvaluator;
import org.orecruncher.environs.library.BiomeLibrary;
import org.orecruncher.environs.library.DimensionLibrary;
import org.orecruncher.environs.scanner.CeilingCoverage;
import org.orecruncher.lib.*;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.resource.ResourceUtils;
import org.orecruncher.lib.seasons.Season;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class CommonStateHandler extends HandlerBase {

    private static final double VILLAGE_RANGE = 64 * 64;

    private final static List<String> scripts;

    static {
        final String script_lines = ResourceUtils.readResource(new ResourceLocation(Environs.MOD_ID, "misc/script_debug.txt"));
        assert script_lines != null;
        scripts = Pattern.compile("\\r?\\n")
                .splitAsStream(script_lines)
                .map(String::trim)
                .filter(s -> !(StringUtils.isNullOrEmpty(s) || s.startsWith("//")))
                .collect(Collectors.toList());
    }

    protected final CeilingCoverage ceilingCoverage = new CeilingCoverage();

    CommonStateHandler() {
        super("Common State");
    }

    @Override
    public void process(@Nonnull final PlayerEntity player) {

        final long currentTick = TickCounter.getTickCount();
        final CommonState data = CommonState.getData();
        final World world = player.getEntityWorld();

        ceilingCoverage.tick();

        data.clock.update(world);
        data.season = Season.getSeason(world);

        data.playerBiome = BiomeLibrary.getPlayerBiome(player, false);
        data.truePlayerBiome = BiomeLibrary.getPlayerBiome(player, true);
        data.dimInfo = DimensionLibrary.getData(world);
        data.dimensionName = data.dimInfo.getName().toString();
        data.playerPosition = player.getPosition();
        data.playerEyePosition = player.getEyePosition(1F);
        data.dayCycle = DayCycle.getCycle(world);
        data.inside = ceilingCoverage.isReallyInside();
        data.biomeTemperature = WorldUtils.getTemperatureAt(world, data.playerPosition);

        data.isUnderground = data.playerBiome == BiomeLibrary.UNDERGROUND_INFO;
        data.isInSpace = data.playerBiome == BiomeLibrary.OUTERSPACE_INFO;
        data.isInClouds = data.playerBiome == BiomeLibrary.CLOUDS_INFO;

        final int blockLight = world.getLightFor(LightType.BLOCK, data.playerPosition);
        final int skyLight = world.getLightFor(LightType.SKY, data.playerPosition) - world.getLightSubtracted(data.playerPosition, 0);
        data.lightLevel = Math.max(blockLight, skyLight);

        // Only check once a second
        if (currentTick % 20 == 0) {
            // Only for surface worlds.  Other types of worlds are interpreted as not having villages.
            if (world.getDimensionType().isNatural()) {
                // Look for a bell within range of the player
                final Optional<TileEntity> bell = world.loadedTileEntityList.stream()
                        .filter(te -> te instanceof BellTileEntity)
                        .filter(te -> te.getPos().distanceSq(data.playerEyePosition.x, data.playerEyePosition.y, data.playerEyePosition.z, true) <= VILLAGE_RANGE)
                        .findAny();

                // If a bell is found, look for a villager within range
                data.isInVillage = bell.isPresent();
                if (data.isInVillage) {
                    final Optional<Entity> entity = Streams.stream(GameUtils.getWorld().getAllEntities())
                            .filter(e -> e instanceof VillagerEntity)
                            .filter(e -> e.getDistanceSq(data.playerEyePosition.x, data.playerEyePosition.y, data.playerEyePosition.z) <= VILLAGE_RANGE)
                            .findAny();
                    data.isInVillage = entity.isPresent();
                }
            } else {
                data.isInVillage = false;
            }
        }

        // Resets cached script variables so they are updated
        ConditionEvaluator.INSTANCE.tick();
    }

    @Override
    public void onDisconnect() {
        CommonState.reset();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void diagnostics(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get()) {
            event.addLeft(TextFormatting.YELLOW + CommonState.getData().clock.getFormattedTime());

            for (final String s : scripts) {
                final String result = ConditionEvaluator.INSTANCE.eval(s).toString();
                event.getLeft().add(TextFormatting.DARK_AQUA + result);
            }
        }
    }
}

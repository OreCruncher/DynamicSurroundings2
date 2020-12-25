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

package org.orecruncher.dsurround.commands.dump;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.dsurround.commands.CommandHelpers;
import org.orecruncher.environs.library.DimensionLibrary;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.tags.TagUtils;
import org.orecruncher.mobeffects.library.FootstepLibrary;
import org.orecruncher.sndctrl.library.AcousticLibrary;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Command for dumping out various things about the state of the mod.  These commands are only available single player
 * and execute on the main client thread.
 */
public class DumpCommand {
    public static void register(@Nonnull final CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("dsdump")
                .requires(req -> req.hasPermissionLevel(0))
                .then(Commands.literal("acoustics").executes(cmd -> dumpAcoustics(cmd.getSource())))
                .then(Commands.literal("audioeffects").executes(cmd -> dumpAudioEffects(cmd.getSource())))
                .then(Commands.literal("blocks").executes(cmd -> dumpBlocks(cmd.getSource())))
                .then(Commands.literal("blocktags").executes(cmd -> dumpBlockTags(cmd.getSource())))
                .then(Commands.literal("biomes").executes(cmd -> dumpBiomes(cmd.getSource())))
                .then(Commands.literal("items").executes(cmd -> dumpItems(cmd.getSource())))
                .then(Commands.literal("footsteps").executes(cmd -> dumpFootsteps(cmd.getSource())))
                .then(Commands.literal("mobeffects").executes(cmd -> dumpMobeffects(cmd.getSource())))
                .then(Commands.literal("blockstates").executes(cmd -> dumpBlockStates(cmd.getSource())))
                .then(Commands.literal("dimensions").executes(cmd -> dumpDimensions(cmd.getSource()))));
    }

    private static int dumpAcoustics(@Nonnull final CommandSource source) {
        return handle(source, "dump.acoustics", AcousticLibrary::dump);
    }

    private static int dumpAudioEffects(@Nonnull final CommandSource source) {
        return handle(source, "dump.audioeffects", DumpCommand::tbd);
    }

    private static int dumpBlocks(@Nonnull final CommandSource source) {
        return handle(source, "dump.blocks", DumpCommand::tbd);
    }

    private static int dumpBlockTags(@Nonnull final CommandSource source) {
        return handle(source, "dump.blocktags", TagUtils::dumpBlockTags);
    }

    private static int dumpBiomes(@Nonnull final CommandSource source) {
        return handle(source, "dump.biomes", DumpCommand::tbd);
    }

    private static int dumpFootsteps(@Nonnull final CommandSource source) {
        return handle(source, "dump.footsteps", FootstepLibrary::dump);
    }

    private static int dumpItems(@Nonnull final CommandSource source) {
        return handle(source, "dump.items", DumpCommand::tbd);
    }

    private static int dumpMobeffects(@Nonnull final CommandSource source) {
        return handle(source, "dump.mobeffects", DumpCommand::tbd);
    }

    private static int dumpBlockStates(@Nonnull final CommandSource source) {
        return handle(source, "dump.blockstates", DumpCommand::tbd);
    }

    private static int dumpDimensions(@Nonnull final CommandSource source) {
        return handle(source, "dump.dimensions", DimensionLibrary::dump);
    }

    private static int handle(@Nonnull final CommandSource source, @Nonnull final String cmdString, @Nonnull final Supplier<Stream<String>> supplier) {
        try {
            if (GameUtils.getMC().isSingleplayer()) {
                final String operation = cmdString.substring(5);
                final String fileName = operation + ".txt";
                File target = new File(DynamicSurroundings.DUMP_PATH, fileName);

                CommandHelpers.scheduleOnClientThread(() -> {
                    try (PrintStream out = new PrintStream(target)) {
                        final Stream<String> strm = supplier.get();
                        strm.forEach(out::println);
                        out.flush();
                    } catch (@Nonnull final Throwable t) {
                        DynamicSurroundings.LOGGER.error(t, "Error writing dump file '%s'", target.toString());
                    }
                });
                CommandHelpers.sendSuccess(source, "dump", operation, target.toString());
            } else {
                CommandHelpers.sendFailure(source, cmdString);
            }
        } catch (@Nonnull final Throwable t) {
            CommandHelpers.sendFailure(source, cmdString);
        }
        return 0;
    }

    private static Stream<String> tbd() {
        final List<String> result = ImmutableList.of("Not hooked up");
        return result.stream();
    }

}

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

package org.orecruncher.sndctrl.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.codehaus.plexus.util.StringUtils;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.MaterialUtils;
import org.orecruncher.lib.TagUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.blockstate.BlockStateMatcher;
import org.orecruncher.sndctrl.Config;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.library.AudioEffectLibrary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Inspector {

    private static final String TEXT_BLOCKSTATE = TextFormatting.DARK_PURPLE + "<BlockState>";
    private static final String TEXT_TAGS = TextFormatting.DARK_PURPLE + "<Tags>";

    private static List<String> diagnostics = ImmutableList.of();

    private Inspector() {

    }

    private static List<String> getTags(@Nonnull final BlockState state) {
        final Collection<Tag<Block>> tags = TagUtils.getBlockStateTags(state);
        return tags.stream().map(t -> "#" + t.getId().toString()).collect(Collectors.toList());
    }

    private static List<String> gatherBlockText(final ItemStack stack, final List<String> text, final BlockState state,
                                                final BlockPos pos) {

        if (!stack.isEmpty()) {
            text.add(TextFormatting.RED + stack.getDisplayName().getFormattedText());
            final String itemName = stack.getItem().getName().getFormattedText();
            if (!StringUtils.isEmpty(itemName)) {
                text.add("ITEM: " + itemName);
                text.add(TextFormatting.DARK_AQUA + stack.getItem().getClass().getName());
            }
        }

        if (state != null) {
            final BlockStateMatcher info = BlockStateMatcher.create(state);
            if (!info.isEmpty()) {
                text.add("BLOCK: " + info.toString());
                text.add(TextFormatting.DARK_AQUA + info.getBlock().getClass().getName());
                text.add("Material: " + MaterialUtils.getMaterialName(state.getMaterial()));
                final SoundType st = state.getSoundType();
                text.add("Step Sound: " + st.getStepSound().getRegistryName().toString());
                text.add("Reflectivity: " + AudioEffectLibrary.getReflectivity(state));
                text.add("Occlusion: " + AudioEffectLibrary.getOcclusion(state));
                text.add(TEXT_BLOCKSTATE);
                final CompoundNBT nbt = NBTUtil.writeBlockState(state);
                text.add(nbt.toString());
                final List<String> tagNames = getTags(state);
                if (tagNames.size() > 0) {
                    text.add(TEXT_TAGS);
                    for (final String ore : tagNames)
                        text.add(TextFormatting.GOLD + ore);
                }
            }
        }

        return text;
    }

    private static boolean isHolding() {
        final PlayerEntity player = GameUtils.getPlayer();
        if (player == null)
            return false;
        final ItemStack held = player.getHeldItem(Hand.MAIN_HAND);
        return !held.isEmpty() && held.getItem() == Items.CARROT_ON_A_STICK;
    }

    @SubscribeEvent
    public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {

        if (TickCounter.getTickCount() % 5 == 0) {

            diagnostics = ImmutableList.of();

            if (Config.CLIENT.logging.enableLogging.get() && isHolding()) {
                final World world = GameUtils.getWorld();
                final List<String> data = new ArrayList<>();
                final RayTraceResult current = GameUtils.getMC().objectMouseOver;
                if (current instanceof BlockRayTraceResult) {
                    final BlockRayTraceResult trace = (BlockRayTraceResult) current;
                    if (trace.getType() != RayTraceResult.Type.MISS) {

                        final BlockState state = world.getBlockState(trace.getPos());

                        if (!state.isAir(world, trace.getPos())) {
                            final ItemStack stack = state.getBlock().getPickBlock(state, current, world, trace.getPos(), GameUtils.getPlayer());
                            diagnostics = gatherBlockText(stack, data, state, trace.getPos());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGatherText(@Nonnull final RenderGameOverlayEvent.Text event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT && diagnostics.size() > 0 && GameUtils.displayDebug()) {
            event.getLeft().addAll(diagnostics);
        }
    }
}

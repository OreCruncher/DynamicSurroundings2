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

package org.orecruncher.dsurround.huds.lightlevel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.dsurround.config.Config;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.gui.ColorPalette;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.particles.FrustrumHelper;

import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = DynamicSurroundings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LightLevelHUD {

    private static FontRenderer font;

    public enum Mode {
        BLOCK, SKY, BLOCK_SKY;
    }

    public enum ColorSet {

        BRIGHT(ColorPalette.MC_GREEN, ColorPalette.MC_YELLOW, ColorPalette.MC_RED, ColorPalette.MC_DARKAQUA),
        DARK(ColorPalette.MC_DARKGREEN, ColorPalette.MC_GOLD, ColorPalette.MC_DARKRED, ColorPalette.MC_DARKBLUE);

        public final Color safe;
        public final Color caution;
        public final Color hazard;
        public final Color noSpawn;

        ColorSet(@Nonnull final Color safe, @Nonnull final Color caution, @Nonnull final Color hazard,
                         @Nonnull final Color noSpawn) {
            this.safe = safe;
            this.caution = caution;
            this.hazard = hazard;
            this.noSpawn = noSpawn;
        }
    }

    private static final class LightCoord {
        public int x;
        public double y;
        public int z;
        public int lightLevel;
        public Color color;
    }

    public static boolean showHUD = false;

    private static final int ALLOCATION_SIZE = 2048;
    private static final ObjectArray<LightCoord> lightLevels = new ObjectArray<>(ALLOCATION_SIZE);
    private static final BlockPos.Mutable mutable = new BlockPos.Mutable();
    private static int nextCoord = 0;

    static {
        for (int i = 0; i < ALLOCATION_SIZE; i++)
            lightLevels.add(new LightCoord());
    }

    private static LightCoord nextCoord() {
        if (nextCoord == lightLevels.size())
            lightLevels.add(new LightCoord());
        return lightLevels.get(nextCoord++);
    }

    protected static boolean inFrustum(final double x, final double y, final double z) {
        return FrustrumHelper.isLocationInFrustum(new Vector3d(x, y, z));
    }

    protected static boolean renderLightLevel(@Nonnull final BlockState state, @Nonnull final BlockState below) {
        final Material stateMaterial = state.getMaterial();
        final Material belowMaterial = below.getMaterial();
        return !stateMaterial.isSolid() && !stateMaterial.isLiquid() && belowMaterial.isSolid();
    }

    protected static float heightAdjustment(@Nonnull final BlockState state, @Nonnull final BlockState below,
                                            @Nonnull final BlockPos pos) {
        if (state.getMaterial() == Material.AIR) {
            final VoxelShape shape = below.getCollisionShape(GameUtils.getWorld(), pos.down());
            return shape.isEmpty() ? 0 : (float) shape.getEnd(Direction.Axis.Y) - 1;
        }

        final VoxelShape shape = below.getCollisionShape(GameUtils.getWorld(), pos);
        if (shape.isEmpty())
            return 0F;
        final float adjust = (float) (shape.getEnd(Direction.Axis.Y));
        return state.getBlock() == Blocks.SNOW ? adjust + 0.125F : adjust;
    }

    protected static void updateLightInfo(@Nonnull final Vector3d position) {

        font = GameUtils.getMC().fontRenderer;
        nextCoord = 0;

        final ColorSet colors = Config.CLIENT.lightLevel.colorSet.get();
        final Mode displayMode = Config.CLIENT.lightLevel.mode.get();
        final int skyLightSub = GameUtils.getWorld().getSkylightSubtracted();
        final int rangeXZ = Config.CLIENT.lightLevel.range.get() * 2 + 1;
        final int rangeY = Config.CLIENT.lightLevel.range.get() + 1;
        final int originX = MathStuff.floor(position.x) - (rangeXZ / 2);
        final int originZ = MathStuff.floor(position.z) - (rangeXZ / 2);
        final int originY = MathStuff.floor(position.y) - (rangeY - 3);

        final World world = GameUtils.getWorld();

        for (int dX = 0; dX < rangeXZ; dX++)
            for (int dZ = 0; dZ < rangeXZ; dZ++) {

                final int trueX = originX + dX;
                final int trueZ = originZ + dZ;

                BlockState lastState = null;

                for (int dY = 0; dY < rangeY; dY++) {

                    final int trueY = originY + dY;

                    if (trueY < 1 || !inFrustum(trueX, trueY, trueZ))
                        continue;

                    final BlockPos pos = new BlockPos(trueX, trueY, trueZ);
                    final BlockState state = world.getBlockState(pos);

                    if (lastState == null)
                        lastState = world.getBlockState(pos.down());

                    if (renderLightLevel(state, lastState)) {
                        mutable.setPos(trueX, trueY, trueZ);

                        final boolean mobSpawn = lastState.canCreatureSpawn(
                                GameUtils.getWorld(),
                                mutable,
                                EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
                                null);

                        if (mobSpawn || !Config.CLIENT.lightLevel.hideSafe.get()) {
                            final int blockLight = world.getLightFor(LightType.BLOCK, mutable);
                            final int skyLight = world.getLightFor(LightType.SKY, mutable) - skyLightSub;
                            final int effective = Math.max(blockLight, skyLight);

                            final int result;
                            if (displayMode == Mode.BLOCK_SKY) {
                                result = effective;
                            } else if (displayMode == Mode.BLOCK)  {
                                result = blockLight;
                            } else {
                                result = skyLight;
                            }

                            Color color = colors.safe;
                            if (!mobSpawn) {
                                color = colors.noSpawn;
                            } else if (blockLight <= Config.CLIENT.lightLevel.lightSpawnThreshold.get()) {
                                if (effective > Config.CLIENT.lightLevel.lightSpawnThreshold.get())
                                    color = colors.caution;
                                else
                                    color = colors.hazard;
                            }

                            if (!(color == colors.safe && Config.CLIENT.lightLevel.hideSafe.get())) {
                                final LightCoord coord = nextCoord();
                                coord.x = trueX;
                                coord.y = trueY + heightAdjustment(state, lastState, mutable) + 0.002D;
                                coord.z = trueZ;
                                coord.lightLevel = result;
                                coord.color = new Color(color.red(), color.green(), color.blue(), 0.99F);
                            }
                        }
                    }

                    lastState = state;
                }
            }
    }

    @SubscribeEvent
    public static void doTick(@Nonnull final TickEvent.PlayerTickEvent event) {

        if (!showHUD || event.side == LogicalSide.SERVER || event.phase == TickEvent.Phase.END || GameUtils.getMC().isGamePaused())
            return;

        if (event.player == null || event.player.world == null)
            return;

        if (TickCounter.getTickCount() % 4 != 0)
            return;

        updateLightInfo(event.player.getPositionVec());
    }

    public static void render(@Nonnull final MatrixStack matrixStack, final float partialTicks) {
        if (!showHUD || nextCoord == 0)
            return;

        final PlayerEntity player = GameUtils.getPlayer();
        if (player == null)
            return;

        drawStringRender(matrixStack, player);
    }

    private static void drawStringRender(@Nonnull final MatrixStack matrixStack, @Nonnull final PlayerEntity player) {

        final boolean thirdPerson = GameUtils.isThirdPersonView();
        Direction playerFacing = player.getHorizontalFacing();
        if (thirdPerson)
            playerFacing = playerFacing.getOpposite();
        if (playerFacing == Direction.SOUTH || playerFacing == Direction.NORTH)
            playerFacing = playerFacing.getOpposite();
        final float rotationAngle = playerFacing.getOpposite().getHorizontalAngle();

        final Quaternion rotY = Vector3f.YP.rotationDegrees(rotationAngle);
        final Quaternion rotX = Vector3f.XP.rotationDegrees(90);
        final Vector3d view = GameUtils.getMC().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.push();
        matrixStack.translate(-view.getX(), -view.getY(), -view.getZ());

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param);
        GlStateManager.enableDepthTest();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(true);

        for (int i = 0; i < nextCoord; i++) {
            final LightCoord coord = lightLevels.get(i);
            final double x = coord.x;
            final double y = coord.y;
            final double z = coord.z;

            final String text = String.valueOf(coord.lightLevel);
            final int margin = -(font.getStringWidth(text) + 1) / 2;
            final int yAdjust = -(font.FONT_HEIGHT / 2);
            final float scale = 0.08F;

            matrixStack.push();
            matrixStack.translate(x + 0.5D, y, z + 0.5D);
            matrixStack.rotate(rotY);
            matrixStack.rotate(rotX);
            matrixStack.scale(-scale, -scale, scale);
            font.drawString(matrixStack, text, margin, yAdjust, coord.color.rgb());
            matrixStack.pop();
        }

        matrixStack.pop();
    }
}

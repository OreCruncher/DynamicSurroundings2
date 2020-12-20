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

package org.orecruncher.environs.fog;

import net.minecraft.client.GameSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.orecruncher.environs.handlers.CommonState;
import org.orecruncher.environs.library.BiomeInfo;
import org.orecruncher.environs.library.BiomeUtil;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.math.MathStuff;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class BiomeFogColorCalculator extends VanillaFogColorCalculator {

    // ForgeHooksClient.getSkyBlendColour()
    private static final int[] BLEND_RANGES = {2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34};

    protected final BlockPos.Mutable pos = new BlockPos.Mutable();

    protected int posX;
    protected int posZ;

    // Last pass calculations. We can reuse if possible to avoid scanning the area, again.
    protected double weightBiomeFog;
    protected Color biomeFogColor;
    protected boolean doScan = true;

    @Override
    @Nonnull
    public Color calculate(@Nonnull final EntityViewRenderEvent.FogColors event) {

        final PlayerEntity player = GameUtils.getPlayer();
        final World world = GameUtils.getWorld();

        assert player != null && world != null;

        final int playerX = MathStuff.floor(player.getPosX());
        final int playerZ = MathStuff.floor(player.getPosZ());

        final GameSettings settings = GameUtils.getGameSettings();
        int distance = 6;
        if (settings.fancyGraphics) {
            distance = BLEND_RANGES[MathStuff.clamp(settings.renderDistanceChunks, 0, BLEND_RANGES.length - 1)];
        }

        // Biome scan - only need to worry about the change in X and Z
        this.doScan |= this.posX != playerX || this.posZ != playerZ;

        if (this.doScan) {
            this.doScan = false;
            this.posX = playerX;
            this.posZ = playerZ;
            this.weightBiomeFog = 0;

            float red = 0;
            float green = 0;
            float blue = 0;

            final IWorldReader reader = CommonState.getBlockReader();

            for (int z = -distance; z <= distance; ++z) {
                for (int x = -distance; x <= distance; ++x) {

                    this.pos.setPos(playerX + x, 0, playerZ + z);

                    final Biome b = reader.getBiome(this.pos);
                    final BiomeInfo biome = BiomeUtil.getBiomeData(b);
                    final Color color;

                    // Fetch the color we are dealing with.
                    if (biome.getHasDust()) {
                        color = biome.getDustColor();
                    } else if (biome.getHasFog()) {
                        color = biome.getFogColor();
                    } else {
                        color = null;
                    }

                    if (color != null) {
                        red += color.red();
                        green += color.green();
                        blue += color.blue();
                        this.weightBiomeFog += 1F;
                    }
                }
            }

            if (this.weightBiomeFog > 0) {
                red /= this.weightBiomeFog;
                green /= this.weightBiomeFog;
                blue /= this.weightBiomeFog;
                this.biomeFogColor = new Color(red, green, blue);
            } else {
                this.biomeFogColor = new Color(0, 0, 0);
            }
        }

        // If we have nothing then just return whatever Vanilla wanted
        if (this.weightBiomeFog == 0)
            return super.calculate(event);

        // WorldProvider.getFogColor() - need to calculate the scale based
        // on sunlight and stuff.
        final float partialTicks = (float) event.getRenderPartialTicks();
        final float celestialAngle = world.getCelestialAngle(partialTicks);
        final float baseScale = MathStuff.clamp1(MathStuff.cos(celestialAngle * MathStuff.PI_F * 2.0F) * 2.0F + 0.5F);

        double rScale = baseScale * 0.94F + 0.06F;
        double gScale = baseScale * 0.94F + 0.06F;
        double bScale = baseScale * 0.91F + 0.09F;

        // EntityRenderer.updateFogColor() - adjust the scale further
        // based on rain and thunder.
        final float rainStrength = world.getRainStrength(partialTicks);
        if (rainStrength > 0) {
            rScale *= 1 - rainStrength * 0.5f;
            gScale *= 1 - rainStrength * 0.5f;
            bScale *= 1 - rainStrength * 0.4f;
        }

        final float thunderStrength = world.getThunderStrength(partialTicks);
        if (thunderStrength > 0) {
            rScale *= 1 - thunderStrength * 0.5f;
            gScale *= 1 - thunderStrength * 0.5f;
            bScale *= 1 - thunderStrength * 0.5f;
        }

        // Normalize the blended color components based on the biome weight.
        // The components contain a summation of all the fog components
        // in the area around the player.
        final Color fogColor = this.biomeFogColor.scale((float) rScale, (float) gScale, (float) bScale);
        final Color processedColor = applyPlayerEffects(world, player, fogColor, partialTicks);

        final double weightMixed = (distance * 2 + 1) * (distance * 2 + 1);
        final double weightDefault = weightMixed - this.weightBiomeFog;
        final Color vanillaColor = super.calculate(event);

        float red = (float) (processedColor.red() * this.weightBiomeFog);
        float green = (float) (processedColor.green() * this.weightBiomeFog);
        float blue = (float) (processedColor.blue() * this.weightBiomeFog);

        float vRed = (float) (vanillaColor.red() * weightDefault);
        float vGreen = (float) (vanillaColor.green() * weightDefault);
        float vBlue = (float) (vanillaColor.blue() * weightDefault);

        final float scale = (float) (1 / weightMixed);
        return new Color((red + vRed) * scale, (green + vGreen) * scale, (blue + vBlue) * scale);
    }

    protected Color applyPlayerEffects(@Nonnull final World world, @Nonnull final PlayerEntity player,
                                       @Nonnull final Color fogColor, final float renderPartialTicks) {
        float darkScale = (float) ((player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * renderPartialTicks)
                * world.getDimension().getVoidFogYFactor());

        // EntityRenderer.updateFogColor() - If the player is blind need to
        // darken it further
        EffectInstance effect = player.getActivePotionEffect(Effects.BLINDNESS);
        if (effect != null) {
            final int duration = effect.getDuration();
            darkScale *= (duration < 20) ? (1 - duration / 20f) : 0;
        }

        if (darkScale < 1) {
            darkScale = (darkScale < 0) ? 0 : darkScale * darkScale;
            fogColor.scale(darkScale);
        }

        // EntityRenderer.updateFogColor() - If the player has night vision going need to lighten it a bit
        effect = player.getActivePotionEffect(Effects.NIGHT_VISION);
        if (effect != null) {
            final int duration = effect.getDuration();
            final float brightness = (duration > 200) ? 1
                    : 0.7f + MathStuff.sin((duration - renderPartialTicks) * MathStuff.PI_F * 0.2f) * 0.3f;

            float scale = 1 / fogColor.red();
            scale = Math.min(scale, 1F / fogColor.green());
            scale = Math.min(scale, 1F / fogColor.blue());

            return fogColor.scale((1F - brightness) + scale * brightness);
        }

        return fogColor;
    }
}

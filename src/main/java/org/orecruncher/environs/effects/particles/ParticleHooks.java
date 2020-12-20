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

package org.orecruncher.environs.effects.particles;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.effects.JetEffect;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.particles.ParticleCollisionResult;
import org.orecruncher.sndctrl.api.acoustics.Library;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public final class ParticleHooks {

    private static final ResourceLocation STEAM_HISS_ACOUSTIC = new ResourceLocation(Environs.MOD_ID, "steam.hiss");
    private static final ResourceLocation WATER_DRIP_ACOUSTIC = new ResourceLocation(Environs.MOD_ID, "waterdrips");
    private static final ResourceLocation WATER_DROP_ACOUSTIC = new ResourceLocation(Environs.MOD_ID, "waterdrops");

    private ParticleHooks() {

    }

    /**
     * ASM hook called when detecting whether a liquid drip has hit the ground.  We can do fancy effects like water
     * ripples, steam, etc. when detected.
     * @param particle DripParticle that is being processed
     */
    public static void dripHandler(@Nonnull final DripParticle particle) {
        // If the particle is down at bedrock level kill it.  This could happen if water is sitting on top of flat
        // bedrock.
        if (particle.posY < 1) {
            particle.setExpired();
            return;
        }

        final World world = GameUtils.getWorld();
        // Move down slightly on the Y.  Reason is that the particle may literally just above the block
        final BlockPos pos = new BlockPos(particle.posX, particle.posY - 0.01D, particle.posZ);
        final BlockState state = world.getBlockState(pos);

        // If the particle is hitting solid ground we need to play a splat
        if (particle.onGround) {
            final Vec3d vecPos = new Vec3d(particle.posX, particle.posY, particle.posZ);
            final ResourceLocation acoustic;
            if (doSteamHiss(particle.fluid, state)) {
                createSteamCloud(world, vecPos);
                acoustic = STEAM_HISS_ACOUSTIC;
                particle.setExpired();
                // Do this to prevent the splash from generating
                particle.onGround = false;
            } else {
                // Don't set expired - this will cause the logic in DripParticle to do a splash
                acoustic = WATER_DROP_ACOUSTIC;
            }
            Library.resolve(acoustic).playAt(vecPos);
            return;
        }

        // Could be falling into a fluid
        final IFluidState fluidState = world.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            final float actualHeight = fluidState.getActualHeight(world, pos) + pos.getY();
            if (particle.posY <= actualHeight) {
                // The position of the particle intersected with the fluid surface thus a hit.  The effect of a drop
                // hitting lava is different than water.
                boolean isDripLava = particle.fluid.isIn(FluidTags.LAVA);
                final Vec3d vecPos = new Vec3d(particle.posX, particle.posY, particle.posZ);
                final ResourceLocation acoustic;

                if (fluidState.isTagged(FluidTags.LAVA)) {
                    if (isDripLava) {
                        acoustic = WATER_DROP_ACOUSTIC;
                    } else {
                        createSteamCloud(world, vecPos);
                        acoustic = STEAM_HISS_ACOUSTIC;
                    }
                } else {
                    // There will be a water ripple
                    Collections.addWaterRipple(world, particle.posX, particle.posY + 0.01D, particle.posZ);
                    if (isDripLava) {
                        createSteamCloud(world, vecPos);
                        acoustic = STEAM_HISS_ACOUSTIC;
                    } else {
                        acoustic = WATER_DRIP_ACOUSTIC;
                    }
                }

                Library.resolve(acoustic).playAt(vecPos);
                particle.setExpired();
            }
        }
    }

    /**
     * Similar to drip handling, but generically handles the event of a fluid type falling and hitting a surface, wether
     * solid or liquid.
     */
    public static void splashHandler(@Nonnull final Fluid fluidType, @Nonnull final ParticleCollisionResult collision, final boolean playSound) {

        final IBlockReader world = collision.world;
        // Move down slightly on the Y.  Reason is that the particle may literally just above the block
        final Vec3d particlePos = collision.position;
        final BlockPos pos = new BlockPos(particlePos.x, particlePos.y - 0.01D, particlePos.z);
        final BlockState state = collision.state;

        // If the particle is hitting solid ground we need to play a splat and generate a steam puff as needed
        if (collision.onGround) {
            final ResourceLocation acoustic;
            if (doSteamHiss(fluidType, state)) {
                createSteamCloud(world, particlePos);
                acoustic = STEAM_HISS_ACOUSTIC;
            } else {
                // Don't set expired - this will cause the logic in DripParticle to do a splash
                acoustic = WATER_DROP_ACOUSTIC;
            }
            if (playSound)
                Library.resolve(acoustic).playAt(particlePos);
            return;
        }

        // Could be falling into a fluid
        final IFluidState fluidState = collision.fluidState;
        if (!fluidState.isEmpty() && fluidState.isSource() && world.getBlockState(pos.up()).getMaterial() == Material.AIR) {
            final float actualHeight = fluidState.getActualHeight(world, pos) + pos.getY();
            if (particlePos.y <= actualHeight) {
                // The position of the particle intersected with the fluid surface thus a hit.  The effect of a drop
                // hitting lava is different than water.
                boolean isDripLava = fluidType.isIn(FluidTags.LAVA);
                final ResourceLocation acoustic;

                if (fluidState.isTagged(FluidTags.LAVA)) {
                    if (isDripLava) {
                        acoustic = WATER_DROP_ACOUSTIC;
                    } else {
                        createSteamCloud(world, particlePos);
                        acoustic = STEAM_HISS_ACOUSTIC;
                    }
                } else {
                    // There will be a water ripple
                    Collections.addWaterRipple(world, particlePos.x, particlePos.y + 0.01D, particlePos.z);
                    if (isDripLava) {
                        createSteamCloud(world, particlePos);
                        acoustic = STEAM_HISS_ACOUSTIC;
                    } else {
                        acoustic = WATER_DRIP_ACOUSTIC;
                    }
                }

                if (playSound)
                    Library.resolve(acoustic).playAt(particlePos);
            }
        }
    }

    private static void createSteamCloud(@Nonnull final IBlockReader world, @Nonnull final Vec3d pos) {
        final Particle steamCloud = new SteamCloudParticle(GameUtils.getWorld(), pos.x, pos.y + 0.01D, pos.z, 0.01D);
        GameUtils.getMC().particles.addEffect(steamCloud);
    }

    private static boolean doSteamHiss(@Nonnull final Fluid particleFluid, @Nonnull final BlockState state) {
        return JetEffect.HOTBLOCK_PREDICATE.test(state) && particleFluid.isIn(FluidTags.WATER);
    }
}

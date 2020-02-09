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

package org.orecruncher.lib;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.lighting.IWorldLightListener;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Optimizes access to the underlying IWorldReader when processing.  Logic generally avoids diagnostics, and will
 * cache the last chunk that was accessed in hopes of a subsequent hit.
 */
@OnlyIn(Dist.CLIENT)
public class EnvironmentBlockReader implements IEnviromentBlockReader {

    private static final BlockState AIR = Blocks.AIR.getDefaultState();

    protected final IWorld reader;
    protected final WorldLightManager lightManager;
    protected final IWorldLightListener sky;
    protected final IWorldLightListener block;

    protected int lastChunkX = Integer.MAX_VALUE;
    protected int lastChunkZ = Integer.MAX_VALUE;
    protected IChunk lastChunk;

    public EnvironmentBlockReader(@Nonnull final IWorld reader) {
        this.reader = reader;
        this.lightManager = reader.getChunkProvider().func_212863_j_();
        this.sky = this.lightManager.getLightEngine(LightType.SKY);
        this.block = this.lightManager.getLightEngine(LightType.BLOCK);
    }

    public boolean needsUpdate(@Nonnull final IWorld world) {
        return this.reader != world;
    }

    @Nonnull
    @Override
    public Biome getBiome(@Nonnull final BlockPos pos) {
        final IChunk chunk = resolveChunk(pos);
        return chunk == null ? Biomes.PLAINS : chunk.getBiome(pos);
    }

    @Override
    public int getLightFor(@Nonnull final LightType type, @Nonnull final BlockPos pos) {
        final IWorldLightListener wll = type == LightType.SKY ? this.sky : this.block;
        return wll.getLightFor(pos);
    }

    @Override
    public int getCombinedLight(@Nonnull final BlockPos pos, final int minLight) {
        int i = this.sky.getLightFor(pos);
        int j = this.block.getLightFor(pos);
        if (j < minLight) {
            j = minLight;
        }
        return i << 20 | j << 4;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(@Nonnull final BlockPos pos) {
        return this.reader.getTileEntity(pos);
    }

    @Nonnull
    @Override
    public BlockState getBlockState(@Nonnull final BlockPos pos) {
        final ChunkSection section = resolveSection(pos);
        return section != null ? section.getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15) : AIR;
    }

    @Nonnull
    @Override
    public IFluidState getFluidState(@Nonnull final BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Nullable
    protected ChunkSection resolveSection(@Nonnull final BlockPos pos) {
        final IChunk chunk = resolveChunk(pos);
        if (chunk != null) {
            final ChunkSection[] sections = chunk.getSections();
            final int y = pos.getY() >> 4;
            if (y < sections.length) {
                ChunkSection chunksection = sections[y];
                if (!ChunkSection.isEmpty(chunksection)) {
                    return chunksection;
                }
            }
        }
        return null;
    }

    @Nullable
    protected IChunk resolveChunk(@Nonnull final BlockPos pos) {
        if (!World.isValid(pos))
            return null;

        final int chunkX = pos.getX() >> 4;
        final int chunkZ = pos.getZ() >> 4;

        if (this.lastChunk == null || chunkX != this.lastChunkX || chunkZ != this.lastChunkZ) {
            this.lastChunkX = chunkX;
            this.lastChunkZ = chunkZ;
            this.lastChunk = this.reader.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        }

        return this.lastChunk;
    }
}

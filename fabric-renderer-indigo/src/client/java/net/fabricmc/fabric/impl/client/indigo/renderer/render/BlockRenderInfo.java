/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

/**
 * Holds, manages, and provides access to the block/world related state
 * needed to render quads.
 *
 * <p>Exception: per-block position offsets are tracked in {@link ChunkRenderInfo}
 * so they can be applied together with chunk offsets.
 */
public class BlockRenderInfo {
	private final BlockColors blockColorMap = MinecraftClient.getInstance().getBlockColors();
	private final BlockPos.Mutable searchPos = new BlockPos.Mutable();

	public BlockRenderView blockView;
	public BlockPos blockPos;
	public BlockState blockState;

	boolean useAo;
	boolean defaultAo;
	RenderLayer defaultLayer;

	Random random;
	long seed;
	boolean recomputeSeed;
	public final Supplier<Random> randomSupplier = () -> {
		long seed = this.seed;

		if (recomputeSeed) {
			seed = blockState.getRenderingSeed(blockPos);
			this.seed = seed;
			recomputeSeed = false;
		}

		final Random random = this.random;
		random.setSeed(seed);
		return random;
	};

	private boolean enableCulling;
	private int cullCompletionFlags;
	private int cullResultFlags;

	public void prepareForWorld(BlockRenderView blockView, boolean enableCulling) {
		this.blockView = blockView;
		this.enableCulling = enableCulling;
	}

	public void prepareForBlock(BlockState blockState, BlockPos blockPos, boolean modelAo) {
		this.blockPos = blockPos;
		this.blockState = blockState;

		useAo = MinecraftClient.isAmbientOcclusionEnabled();
		defaultAo = useAo && modelAo && blockState.getLuminance() == 0;

		defaultLayer = RenderLayers.getBlockLayer(blockState);

		cullCompletionFlags = 0;
		cullResultFlags = 0;
	}

	public void release() {
		blockView = null;
		blockPos = null;
		blockState = null;
	}

	int blockColor(int tintIndex) {
		return 0xFF000000 | blockColorMap.getColor(blockState, blockView, blockPos, tintIndex);
	}

	boolean shouldDrawSide(@Nullable Direction side) {
		if (side == null || !enableCulling) {
			return true;
		}

		final int mask = 1 << side.getId();

		if ((cullCompletionFlags & mask) == 0) {
			cullCompletionFlags |= mask;

			if (Block.shouldDrawSide(blockState, blockView.getBlockState(searchPos.set(blockPos, side)), side)) {
				cullResultFlags |= mask;
				return true;
			} else {
				return false;
			}
		} else {
			return (cullResultFlags & mask) != 0;
		}
	}

	boolean shouldCullSide(@Nullable Direction side) {
		return !shouldDrawSide(side);
	}

	RenderLayer effectiveRenderLayer(BlendMode blendMode) {
		return blendMode == BlendMode.DEFAULT ? this.defaultLayer : blendMode.blockRenderLayer;
	}
}

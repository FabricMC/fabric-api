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

package net.fabricmc.fabric.test.screen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public record FabriclandChunkGeneratorConfig(BlockState outline, BlockState background) {
	private static final LongSet OUTLINE_POSITIONS = LongSet.of(
			pos(6, 14),

			pos(7, 13),
			pos(5, 13),

			pos(7, 12),
			pos(4, 12),

			pos(8, 11),
			pos(6, 11),
			pos(3, 11),

			pos(9, 10),
			pos(5, 10),
			pos(2, 10),

			pos(10, 9),
			pos(4, 9),
			pos(2, 9),
			pos(1, 9),

			pos(11, 8),
			pos(3, 8),
			pos(1, 8),

			pos(12, 7),
			pos(3, 7),
			pos(2, 7),

			pos(13, 6),
			pos(4, 6),

			pos(13, 5),
			pos(5, 5),

			pos(12, 4),
			pos(6, 4),

			pos(11, 3),
			pos(7, 3),

			pos(10, 2),
			pos(8, 2),
			pos(7, 2),

			pos(9, 1),
			pos(8, 1)
	);

	public static final FabriclandChunkGeneratorConfig DEFAULT = new FabriclandChunkGeneratorConfig(Blocks.ORANGE_WOOL.getDefaultState(), Blocks.WHITE_WOOL.getDefaultState());

	public static final Codec<FabriclandChunkGeneratorConfig> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					BlockState.CODEC.fieldOf("outline").forGetter(FabriclandChunkGeneratorConfig::outline),
					BlockState.CODEC.fieldOf("background").forGetter(FabriclandChunkGeneratorConfig::background)
			).apply(instance, instance.stable(FabriclandChunkGeneratorConfig::new))
	);

	public BlockState getState(BlockPos blockPos) {
		long pos = pos(blockPos.getX(), blockPos.getZ());
		boolean outline = OUTLINE_POSITIONS.contains(pos);

		return outline ? this.outline : this.background;
	}

	public FabriclandChunkGeneratorConfig withOutline(BlockState outline) {
		return new FabriclandChunkGeneratorConfig(outline, this.background);
	}

	public FabriclandChunkGeneratorConfig withBackground(BlockState background) {
		return new FabriclandChunkGeneratorConfig(this.outline, background);
	}

	private static long pos(int x, int z) {
		return BlockPos.asLong(x, 0, z);
	}

	public static FabriclandChunkGeneratorConfig from(ChunkGenerator chunkGenerator) {
		if (chunkGenerator instanceof FabriclandChunkGenerator fabricland) {
			return fabricland.getConfig();
		}

		return DEFAULT;
	}
}

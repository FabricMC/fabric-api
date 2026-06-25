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

package net.fabricmc.fabric.test.attachment;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.Feature;

public record SetAttachmentFeature() implements Feature {
	public static final SetAttachmentFeature INSTANCE = new SetAttachmentFeature();
	public static final MapCodec<SetAttachmentFeature> CODEC = MapCodec.unit(INSTANCE);
	public static boolean featurePlaced;

	@Override
	public MapCodec<SetAttachmentFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		ChunkAccess chunk = level.getChunk(origin);

		if (chunk.getPos().equals(new ChunkPos(0, 0))) {
			featurePlaced = true;

			if (!(chunk instanceof ProtoChunk) || chunk instanceof ImposterProtoChunk) {
				AttachmentTestMod.LOGGER.warn("Feature not attaching to ProtoChunk");
			}

			chunk.setAttached(AttachmentTestMod.FEATURE_ATTACHMENT, "feature_data");
		}

		return true;
	}
}

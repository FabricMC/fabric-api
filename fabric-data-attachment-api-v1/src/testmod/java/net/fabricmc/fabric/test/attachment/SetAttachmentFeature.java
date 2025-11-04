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

import com.mojang.serialization.Codec;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SetAttachmentFeature extends Feature<NoneFeatureConfiguration> {
	public static boolean featurePlaced;

	public SetAttachmentFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		ChunkAccess chunk = context.level().getChunk(context.origin());

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

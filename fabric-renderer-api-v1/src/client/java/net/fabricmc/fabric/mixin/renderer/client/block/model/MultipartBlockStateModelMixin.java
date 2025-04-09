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

package net.fabricmc.fabric.mixin.renderer.client.block.model;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.MultipartBlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

@Mixin(MultipartBlockStateModel.class)
abstract class MultipartBlockStateModelMixin implements BlockStateModel {
	@Shadow
	@Final
	private MultipartBlockStateModel.MultipartBakedModel bakedModels;

	@Shadow
	@Final
	private BlockState state;

	@Shadow
	@Nullable
	private List<BlockStateModel> models;

	@Override
	public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
		if (models == null) {
			models = bakedModels.build(this.state);
		}

		long seed = random.nextLong();

		for (BlockStateModel model : models) {
			random.setSeed(seed);
			model.emitQuads(emitter, blockView, pos, state, random, cullTest);
		}
	}

	@Override
	@Nullable
	public Object createGeometryKey(BlockRenderView blockView, BlockPos pos, BlockState state, Random random) {
		if (models == null) {
			models = bakedModels.build(this.state);
		}

		int count = models.size();
		long seed = random.nextLong();

		if (count == 1) {
			random.setSeed(seed);
			return models.getFirst().createGeometryKey(blockView, pos, state, random);
		} else {
			Object[] subkeys = new Object[count];

			for (int i = 0; i < count; i++) {
				random.setSeed(seed);
				Object subkey = models.get(i).createGeometryKey(blockView, pos, state, random);

				if (subkey == null) {
					return null;
				}

				subkeys[i] = subkey;
			}

			record Key(Object[] subkeys) {
			}

			return new Key(subkeys);
		}
	}

	@Override
	public Sprite particleSprite(BlockRenderView blockView, BlockPos pos, BlockState state) {
		return ((MultipartBlockStateModelMultipartBakedModelAccessor) (Object) bakedModels).getSelectors().getFirst().model().particleSprite(blockView, pos, state);
	}
}

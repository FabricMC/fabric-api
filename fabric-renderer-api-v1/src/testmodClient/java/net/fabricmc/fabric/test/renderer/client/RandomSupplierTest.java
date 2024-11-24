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

package net.fabricmc.fabric.test.renderer.client;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.MultipartBakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.EmptyBlockRenderView;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/**
 * Tests that vanilla and Fabric API give the same random results.
 *
 * <p>Never do this in a real mod, this is purely for testing!
 */
public class RandomSupplierTest implements ClientModInitializer {
	private static long previousRandom = 0;
	private static boolean hasPreviousRandom = false;

	@Override
	public void onInitializeClient() {
		WeightedBakedModel weightedAgain = createWeightedBakedModel();

		long startingSeed = 42;
		Random random = Random.create();

		random.setSeed(startingSeed);
		weightedAgain.getQuads(Blocks.STONE.getDefaultState(), null, random);

		random.setSeed(startingSeed);
		weightedAgain.getQuads(Blocks.STONE.getDefaultState(), null, random);

		Supplier<Random> randomSupplier = () -> {
			random.setSeed(startingSeed);
			return random;
		};
		weightedAgain.emitBlockQuads(null, EmptyBlockRenderView.INSTANCE, Blocks.STONE.getDefaultState(), BlockPos.ORIGIN, randomSupplier, cullFace -> false);
	}

	private static WeightedBakedModel createWeightedBakedModel() {
		var checkingModel = new RandomCheckingBakedModel();

		DataPool.Builder<BakedModel> weightedBuilder = DataPool.builder();
		weightedBuilder.add(checkingModel, 1);
		weightedBuilder.add(checkingModel, 2);

		var weighted = new WeightedBakedModel(weightedBuilder.build());
		var multipart = new MultipartBakedModel(List.of(
				new MultipartBakedModel.Selector(state -> true, weighted),
				new MultipartBakedModel.Selector(state -> true, weighted)));

		DataPool.Builder<BakedModel> weightedAgainBuilder = DataPool.builder();
		weightedAgainBuilder.add(multipart, 1);
		weightedAgainBuilder.add(multipart, 2);

		return new WeightedBakedModel(weightedAgainBuilder.build());
	}

	private static class RandomCheckingBakedModel implements BakedModel {
		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
			long value = random.nextLong();

			if (hasPreviousRandom) {
				if (value != previousRandom) {
					throw new AssertionError("Random value is not the same as the previous one!");
				}
			} else {
				hasPreviousRandom = true;
				previousRandom = value;
			}

			return List.of();
		}

		@Override
		public void emitBlockQuads(QuadEmitter emitter, BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, Predicate<@Nullable Direction> cullTest) {
			getQuads(state, null, randomSupplier.get());
		}

		@Override
		public boolean useAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean hasDepth() {
			return false;
		}

		@Override
		public boolean isSideLit() {
			return false;
		}

		@Override
		public Sprite getParticleSprite() {
			return null;
		}

		@Override
		public ModelTransformation getTransformation() {
			return null;
		}
	}
}

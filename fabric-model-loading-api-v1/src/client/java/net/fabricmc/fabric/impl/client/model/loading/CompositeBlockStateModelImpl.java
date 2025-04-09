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

package net.fabricmc.fabric.impl.client.model.loading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.client.model.loading.v1.CompositeBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public class CompositeBlockStateModelImpl implements CompositeBlockStateModel {
	private final BlockStateModel[] models;
	@UnmodifiableView
	private final List<BlockStateModel> modelsView;

	public CompositeBlockStateModelImpl(BlockStateModel[] models) {
		this.models = models;
		modelsView = Arrays.asList(models);
	}

	public static CompositeBlockStateModelImpl of(List<BlockStateModel> models) {
		if (models.isEmpty()) {
			throw new IllegalArgumentException("Models list must not be empty");
		}

		for (BlockStateModel model : models) Objects.requireNonNull(model, "Model cannot be null");
		return new CompositeBlockStateModelImpl(models.toArray(BlockStateModel[]::new));
	}

	@Override
	@Unmodifiable
	public List<BlockStateModel> models() {
		return modelsView;
	}

	@Override
	public void addParts(Random random, List<BlockModelPart> parts) {
		long seed = random.nextLong();

		for (BlockStateModel model : models) {
			random.setSeed(seed);
			model.addParts(random, parts);
		}
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
		long seed = random.nextLong();

		for (BlockStateModel model : models) {
			random.setSeed(seed);
			model.emitQuads(emitter, blockView, pos, state, random, cullTest);
		}
	}

	@Override
	@Nullable
	public Object createGeometryKey(BlockRenderView blockView, BlockPos pos, BlockState state, Random random) {
		int count = models.length;
		long seed = random.nextLong();

		if (count == 1) {
			random.setSeed(seed);
			return models[0].createGeometryKey(blockView, pos, state, random);
		} else {
			List<Object> subkeys = new ArrayList<>(count);

			for (BlockStateModel submodel : models) {
				random.setSeed(seed);
				Object subkey = submodel.createGeometryKey(blockView, pos, state, random);

				if (subkey == null) {
					return null;
				}

				subkeys.add(subkey);
			}

			record Key(List<Object> subkeys) {
			}

			return new Key(subkeys);
		}
	}

	@Override
	public Sprite particleSprite() {
		return models[0].particleSprite();
	}

	@Override
	public Sprite particleSprite(BlockRenderView blockView, BlockPos pos, BlockState state) {
		return models[0].particleSprite(blockView, pos, state);
	}

	public record Unbaked(@Unmodifiable List<BlockStateModel.Unbaked> models) implements CompositeBlockStateModel.Unbaked {
		public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codecs.nonEmptyList(BlockStateModel.Unbaked.CODEC.listOf()).fieldOf("models").forGetter(Unbaked::models)
		).apply(instance, Unbaked::new));

		public static Unbaked of(List<BlockStateModel.Unbaked> models) {
			if (models.isEmpty()) {
				throw new IllegalArgumentException("Models list must not be empty");
			}

			for (BlockStateModel.Unbaked model : models) Objects.requireNonNull(model, "Model cannot be null");
			return new Unbaked(List.copyOf(models));
		}

		@Override
		public MapCodec<Unbaked> codec() {
			return CODEC;
		}

		@Override
		public BlockStateModel bake(Baker baker) {
			BlockStateModel[] bakedModels = new BlockStateModel[models.size()];

			for (int i = 0; i < models.size(); i++) {
				bakedModels[i] = models.get(i).bake(baker);
			}

			return new CompositeBlockStateModelImpl(bakedModels);
		}

		@Override
		public void resolve(Resolver resolver) {
			models.forEach(model -> model.resolve(resolver));
		}
	}
}

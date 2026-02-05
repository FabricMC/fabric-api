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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.WeightedVariants;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;

public class CustomUnbakedBlockStateModelRegistry {
	private static final String TYPE_KEY = "fabric:type";
	private static final ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends CustomUnbakedBlockStateModel>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();

	/** Map codec for a custom model. Must be a map codec to allow combining with weighted model entry's "weight" field. */
	private static final MapCodec<CustomUnbakedBlockStateModel> CUSTOM_MODEL_MAP_CODEC = ID_MAPPER.codec(Identifier.CODEC).dispatchMap(TYPE_KEY, CustomUnbakedBlockStateModel::codec, codec -> codec);
	/** Map codec for a simple model. Must be a map codec to allow checking presence of type key before parsing. */
	private static final MapCodec<SingleVariant.Unbaked> SIMPLE_MODEL_MAP_CODEC = Variant.MAP_CODEC
			.xmap(SingleVariant.Unbaked::new, SingleVariant.Unbaked::variant);
	/** Map codec for a custom model or a simple model. Uses {@link SingleVariant.Unbaked} instead of {@link Variant} like vanilla to also allow use in {@link #MODEL_CODEC} for convenience and consistent behavior. Must be a map codec to allow combining with weighted model entry's "weight" field. */
	private static final MapCodec<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>> VARIANT_MAP_CODEC = new KeyExistsCodec<>(TYPE_KEY, CUSTOM_MODEL_MAP_CODEC, SIMPLE_MODEL_MAP_CODEC);
	/** Codec for a custom model or a simple model. */
	private static final Codec<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>> VARIANT_CODEC = VARIANT_MAP_CODEC.codec();
	/** Codec for a weighted variant, with support for custom models. Used as list elements in a weighted model. */
	private static final Codec<Weighted<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>>> WEIGHTED_VARIANT_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					VARIANT_MAP_CODEC.forGetter(Weighted::value),
					ExtraCodecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(Weighted::weight)
			).apply(instance, Weighted::new)
	);
	/** Extended codec for a vanilla weighted model that supports using custom models instead of regular variants. Replaces {@link BlockStateModel.Unbaked#WEIGHTED_CODEC}. */
	public static final Codec<WeightedVariants.Unbaked> WEIGHTED_MODEL_CODEC = ExtraCodecs.nonEmptyList(WEIGHTED_VARIANT_CODEC.listOf())
			.flatComapMap(
					weightedVariants -> new WeightedVariants.Unbaked(WeightedList.of(Lists.transform(weightedVariants, weighted -> weighted.map(either -> either.map(Function.identity(), Function.identity()))))),
					model -> {
						List<Weighted<BlockStateModel.Unbaked>> entries = model.entries().unwrap();
						List<Weighted<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>>> weightedVariants = new ArrayList<>(entries.size());

						for (Weighted<BlockStateModel.Unbaked> weighted : entries) {
							switch (weighted.value()) {
								case CustomUnbakedBlockStateModel custom -> {
									weightedVariants.add(new Weighted<>(Either.left(custom), weighted.weight()));
								}
								case SingleVariant.Unbaked simple -> {
									weightedVariants.add(new Weighted<>(Either.right(simple), weighted.weight()));
								}
								default -> {
									return DataResult.error(() -> "Only custom models or single variants are supported");
								}
							}
						}

						return DataResult.success(weightedVariants);
					}
			);
	/** Extended codec for an unbaked model that supports using a custom model directly or inside weighted entries. Replaces {@link BlockStateModel.Unbaked#CODEC}. */
	public static final Codec<BlockStateModel.Unbaked> MODEL_CODEC = Codec.either(WEIGHTED_MODEL_CODEC, VARIANT_CODEC)
			.flatComapMap(either -> either.map(Function.identity(), right -> right.map(Function.identity(), Function.identity())), model -> {
				Objects.requireNonNull(model);

				return switch (model) {
				case CustomUnbakedBlockStateModel custom -> DataResult.success(Either.right(Either.left(custom)));
				case SingleVariant.Unbaked simple -> DataResult.success(Either.right(Either.right(simple)));
				case WeightedVariants.Unbaked weighted -> DataResult.success(Either.left(weighted));
				default -> DataResult.error(() -> "Only a custom model or a single variant or a list of variants are supported");
				};
			});

	public static void register(Identifier id, MapCodec<? extends CustomUnbakedBlockStateModel> codec) {
		ID_MAPPER.put(id, codec);
	}

	/** When decoding, uses a different codec depending on whether a certain key exists or not. */
	private static class KeyExistsCodec<E, N> extends MapCodec<Either<E, N>> {
		private final String key;
		private final MapCodec<E> exists;
		private final MapCodec<N> notExists;

		KeyExistsCodec(String key, MapCodec<E> exists, MapCodec<N> notExists) {
			this.key = key;
			this.exists = exists;
			this.notExists = notExists;
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.concat(exists.keys(ops), notExists.keys(ops));
		}

		@Override
		public <T> DataResult<Either<E, N>> decode(DynamicOps<T> ops, MapLike<T> input) {
			if (input.get(key) != null) {
				return exists.decode(ops, input).map(Either::left);
			} else {
				return notExists.decode(ops, input).map(Either::right);
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(Either<E, N> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			return input.map(
					left -> exists.encode(left, ops, prefix),
					right -> notExists.encode(right, ops, prefix)
			);
		}

		@Override
		public String toString() {
			return "KeyExistsCodec[" + key + " " + exists + " " + notExists + "]";
		}
	}
}

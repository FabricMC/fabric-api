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

package net.fabricmc.fabric.impl.holder.component.data;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.RegistryOps;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.mixin.resource.conditions.RegistryOpsAccessor;

// TODO: Make public api and use for datagen
public record DataHolderComponentFile(
		boolean replace,
		DataComponentPatch components,
		List<ConditionalPatch> conditional
) {
	public static final Codec<DataHolderComponentFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("replace", false).forGetter(DataHolderComponentFile::replace),
			DataComponentPatch.CODEC.fieldOf("components").forGetter(DataHolderComponentFile::components),
			ConditionalPatch.LIST_CODEC.optionalFieldOf("conditional", List.of()).forGetter(DataHolderComponentFile::conditional)
	).apply(instance, DataHolderComponentFile::new));

	// Represents an extra patch of components that will only be applied if the condition is met.
	// This can be used for mod compatibility, i.e. conditioning modded components on the presence
	// of the mod that adds them.
	public record ConditionalPatch(DataComponentPatch components, Optional<ResourceCondition> condition, boolean required) {
		private static final ConditionalPatch EMPTY = new ConditionalPatch(DataComponentPatch.EMPTY, Optional.empty(), false);
		private static final MapCodec<DataComponentPatch> COMPONENTS_MAP_CODEC = DataComponentPatch.CODEC.fieldOf("components");
		private static final MapCodec<Optional<ResourceCondition>> CONDITION_MAP_CODEC = ResourceCondition.CONDITION_CODEC.optionalFieldOf("condition");
		private static final MapCodec<Boolean> REQUIRED_MAP_CODEC = Codec.BOOL.optionalFieldOf("required", true);
		public static final Codec<List<ConditionalPatch>> LIST_CODEC = Codec.of(
				RecordCodecBuilder.create(instance -> instance.group(
						COMPONENTS_MAP_CODEC.forGetter(ConditionalPatch::components),
						CONDITION_MAP_CODEC.forGetter(ConditionalPatch::condition),
						REQUIRED_MAP_CODEC.forGetter(ConditionalPatch::required)
				).apply(instance, ConditionalPatch::new)),
				ConditionalPatch::decode
		)
				.listOf()
				.xmap(ConditionalPatch::removeEmpty, ConditionalPatch::removeEmpty);

		// The condition field is parsed and tested first as it's meant to guard against errors when
		// decoding the components.
		private static <T> DataResult<Pair<ConditionalPatch, T>> decode(DynamicOps<T> ops, T input) {
			DataResult<MapLike<T>> mapResult = ops.getMap(input);

			if (mapResult.isError()) {
				return DataResult.error(mapResult.error().orElseThrow().messageSupplier());
			}

			MapLike<T> map = mapResult.getOrThrow();
			DataResult<Boolean> requiredResult = REQUIRED_MAP_CODEC.decode(ops, map);

			if (requiredResult.isError()) {
				return DataResult.error(requiredResult.error().orElseThrow().messageSupplier());
			}

			boolean required = requiredResult.getOrThrow();
			DataResult<Optional<ResourceCondition>> conditionResult = CONDITION_MAP_CODEC.decode(ops, map);

			if (conditionResult.isError()) {
				return DataResult.error(conditionResult.error().orElseThrow().messageSupplier());
			}

			Optional<ResourceCondition> condition = conditionResult.getOrThrow();
			RegistryOps.RegistryInfoLookup registryInfo = ops instanceof RegistryOpsAccessor registryOps ? registryOps.getRegistryInfoGetter() : null;

			if (condition.isPresent() && !condition.get().test(registryInfo)) {
				return DataResult.success(Pair.of(EMPTY, input));
			}

			DataResult<DataComponentPatch> componentsResult = COMPONENTS_MAP_CODEC.decode(ops, map);

			if (componentsResult.isError()) {
				if (!required) {
					return DataResult.success(Pair.of(EMPTY, input));
				}

				return DataResult.error(componentsResult.error().orElseThrow().messageSupplier());
			}

			return DataResult.success(
					Pair.of(new ConditionalPatch(componentsResult.getOrThrow(), condition, required), input)
			);
		}

		// Empty patches can be filtered out as they will do nothing and are usually the result of a
		// soft failure (condition fails, non-required components decoding error)
		private static List<ConditionalPatch> removeEmpty(List<ConditionalPatch> conditionalPatches) {
			ImmutableList.Builder<ConditionalPatch> builder = ImmutableList.builder();

			for (ConditionalPatch conditionalPatch : conditionalPatches) {
				if (!conditionalPatch.components.isEmpty()) {
					builder.add(conditionalPatch);
				}
			}

			return builder.build();
		}
	}
}

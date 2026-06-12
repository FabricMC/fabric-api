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

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.RegistryOps;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.mixin.resource.conditions.RegistryOpsAccessor;

// TODO: Make public api and use for datagen
public record DataHolderComponentFile(
		boolean replace,
		List<Patch> patches
) {
	public static final Codec<DataHolderComponentFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("replace", false).forGetter(DataHolderComponentFile::replace),
			Patch.LIST_CODEC.optionalFieldOf("patches", List.of()).forGetter(DataHolderComponentFile::patches)
	).apply(instance, DataHolderComponentFile::new));

	public void apply(DataComponentMap.Builder builder) {
		if (replace) {
			builder.clear();
		}

		for (Patch patch : patches) {
			builder.apply(patch.components);
		}
	}

	// Represents an extra patch of components that will only be applied if the condition is met.
	// This can be used for mod compatibility, i.e. conditioning modded components on the presence
	// of the mod that adds them.
	public record Patch(DataComponentPatch components, Optional<ResourceCondition> condition, boolean required) {
		private static final Patch EMPTY = new Patch(DataComponentPatch.EMPTY, Optional.empty(), false);
		private static final MapCodec<DataComponentPatch> COMPONENTS_MAP_CODEC = DataComponentPatch.CODEC.fieldOf("components");
		private static final MapCodec<Optional<ResourceCondition>> CONDITION_MAP_CODEC = ResourceCondition.CONDITION_CODEC.optionalFieldOf("condition");
		private static final MapCodec<Boolean> REQUIRED_MAP_CODEC = Codec.BOOL.optionalFieldOf("required", true);
		public static final Codec<List<Patch>> LIST_CODEC = Codec.of(
						RecordCodecBuilder.create(instance -> instance.group(
								COMPONENTS_MAP_CODEC.forGetter(Patch::components),
								CONDITION_MAP_CODEC.forGetter(Patch::condition),
								REQUIRED_MAP_CODEC.forGetter(Patch::required)
						).apply(instance, Patch::new)),
						Patch::decode
				)
				.listOf()
				.xmap(Patch::removeEmpty, Patch::removeEmpty);

		// The condition field is parsed and tested first as it's meant to guard against errors when
		// decoding the components.
		private static <T> DataResult<Pair<Patch, T>> decode(DynamicOps<T> ops, T input) {
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
					Pair.of(new Patch(componentsResult.getOrThrow(), condition, required), input)
			);
		}

		// Empty patches can be filtered out as they will do nothing and are usually the result of a
		// soft failure (condition fails, non-required components decoding error)
		private static List<Patch> removeEmpty(List<Patch> patches) {
			ImmutableList.Builder<Patch> builder = ImmutableList.builder();

			for (Patch patch : patches) {
				if (!patch.components.isEmpty()) {
					builder.add(patch);
				}
			}

			return builder.build();
		}
	}
}

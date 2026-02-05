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

package net.fabricmc.fabric.api.resource.conditions.v1;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.Optionull;
import net.minecraft.resources.Identifier;

/**
 * A type of resource conditions.
 * @param <T> the type of {@link ResourceCondition}
 */
public interface ResourceConditionType<T extends ResourceCondition> {
	/**
	 * A codec used to serialize the condition type.
	 */
	Codec<ResourceConditionType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(id ->
					Optionull.mapOrElse(ResourceConditions.getConditionType(id), DataResult::success, () -> DataResult.error(() -> "Unknown resource condition key: "+ id)),
					ResourceConditionType::id
	);

	/**
	 * @return the condition's ID
	 */
	Identifier id();

	/**
	 * @return the condition's codec
	 */
	MapCodec<T> codec();

	/**
	 * Creates a resource condition type. The returned value needs to be registered with {@link ResourceConditions#register}.
	 * @param id the ID of the condition
	 * @param codec the codec used to serialize the condition
	 * @param <T> the type of the resource condition
	 * @return the condition type to register
	 */
	static <T extends ResourceCondition> ResourceConditionType<T> create(Identifier id, MapCodec<T> codec) {
		Objects.requireNonNull(id, "id cannot be null");
		Objects.requireNonNull(codec, "codec cannot be null");

		return new ResourceConditionType<>() {
			@Override
			public Identifier id() {
				return id;
			}

			@Override
			public MapCodec<T> codec() {
				return codec;
			}
		};
	}
}

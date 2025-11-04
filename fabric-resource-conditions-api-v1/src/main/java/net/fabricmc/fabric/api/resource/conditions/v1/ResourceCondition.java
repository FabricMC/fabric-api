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

import java.util.List;

import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;

import net.minecraft.resources.RegistryOps;

/**
 * A resource condition. To create a custom condition type, implement this interface,
 * call {@link ResourceConditionType#create} and create the type, then register
 * via {@link ResourceConditions#register}.
 */
public interface ResourceCondition {
	/**
	 * A codec for a resource condition. It is a map with a {@code condition} key denoting the ID
	 * of the condition, and any additional values required by the specific condition.
	 */
	Codec<ResourceCondition> CODEC = ResourceConditionType.TYPE_CODEC.dispatch("condition", ResourceCondition::getType, ResourceConditionType::codec);
	/**
	 * A codec for a list of conditions.
	 */
	Codec<List<ResourceCondition>> LIST_CODEC = CODEC.listOf();
	/**
	 * A codec for parsing load conditions. Can either be an array of conditions or a single condition.
	 */
	Codec<ResourceCondition> CONDITION_CODEC = Codec.withAlternative(CODEC, LIST_CODEC, conditions -> ResourceConditions.and(conditions.toArray(new ResourceCondition[0])));

	/**
	 * @return the type of the condition
	 */
	ResourceConditionType<?> getType();

	/**
	 * Tests the condition. The passed registry lookup, if any, includes all static and dynamic entries.
	 * However, the tags are not loaded yet.
	 *
	 * @implNote {@code registryLookup} should never be {@code null} for supported use cases
	 * (such as recipes or advancements). However, it may be {@code null} in client-side
	 * resources, or for non-vanilla resource types.
	 *
	 * @param registryInfo the registry lookup, or {@code null} in case registry is unavailable
	 * @return whether the condition was successful
	 */
	boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfo);
}

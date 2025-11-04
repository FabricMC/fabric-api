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

package net.fabricmc.fabric.impl.resource.conditions;

import com.mojang.serialization.MapCodec;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.conditions.AllModsLoadedResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.AndResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.AnyModsLoadedResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.FeaturesEnabledResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.NotResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.OrResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.RegistryContainsResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.TagsPopulatedResourceCondition;
import net.fabricmc.fabric.impl.resource.conditions.conditions.TrueResourceCondition;

public class DefaultResourceConditionTypes {
	public static final ResourceConditionType<TrueResourceCondition> TRUE = createResourceConditionType("true", TrueResourceCondition.CODEC);
	public static final ResourceConditionType<NotResourceCondition> NOT = createResourceConditionType("not", NotResourceCondition.CODEC);
	public static final ResourceConditionType<OrResourceCondition> OR = createResourceConditionType("or", OrResourceCondition.CODEC);
	public static final ResourceConditionType<AndResourceCondition> AND = createResourceConditionType("and", AndResourceCondition.CODEC);
	public static final ResourceConditionType<AllModsLoadedResourceCondition> ALL_MODS_LOADED = createResourceConditionType("all_mods_loaded", AllModsLoadedResourceCondition.CODEC);
	public static final ResourceConditionType<AnyModsLoadedResourceCondition> ANY_MODS_LOADED = createResourceConditionType("any_mods_loaded", AnyModsLoadedResourceCondition.CODEC);
	public static final ResourceConditionType<TagsPopulatedResourceCondition> TAGS_POPULATED = createResourceConditionType("tags_populated", TagsPopulatedResourceCondition.CODEC);
	public static final ResourceConditionType<FeaturesEnabledResourceCondition> FEATURES_ENABLED = createResourceConditionType("features_enabled", FeaturesEnabledResourceCondition.CODEC);
	public static final ResourceConditionType<RegistryContainsResourceCondition> REGISTRY_CONTAINS = createResourceConditionType("registry_contains", RegistryContainsResourceCondition.CODEC);

	private static <T extends ResourceCondition> ResourceConditionType<T> createResourceConditionType(String name, MapCodec<T> codec) {
		return ResourceConditionType.create(Identifier.fromNamespaceAndPath("fabric", name), codec);
	}
}

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

package net.fabricmc.fabric.test.resource.conditions;

import com.mojang.serialization.JsonOps;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.biome.Biomes;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

public class DefaultResourceConditionsTest {
	private static final String TESTMOD_ID = "fabric-resource-conditions-api-v1-testmod";
	private static final String API_MOD_ID = "fabric-resource-conditions-api-v1";
	private static final String UNKNOWN_MOD_ID = "fabric-tiny-potato-api-v1";
	private static final ResourceKey<? extends Registry<Object>> UNKNOWN_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(TESTMOD_ID, "unknown_registry"));
	private static final Identifier UNKNOWN_ENTRY_ID = Identifier.fromNamespaceAndPath(TESTMOD_ID, "tiny_potato");

	private void expectCondition(GameTestHelper helper, String name, ResourceCondition condition, boolean expected) {
		HolderLookup.Provider registryLookup = helper.getLevel().registryAccess();
		boolean actual = condition.test(new RegistryOps.HolderLookupAdapter(registryLookup));

		if (actual != expected) {
			throw new AssertionError("Test \"%s\" for condition %s failed; expected %s, got %s".formatted(name, condition.getType().id(), expected, actual));
		}

		// Test serialization
		ResourceCondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow(message -> new AssertionError("Could not serialize \"%s\": %s".formatted(name, message)));
	}

	@GameTest
	public void featuresEnabled(GameTestHelper helper) {
		ResourceCondition vanilla = ResourceConditions.featuresEnabled(FeatureFlags.VANILLA);
		// Reminder: GameTest enables all features by default
		ResourceCondition vanillaAndRedstoneExperiments = ResourceConditions.featuresEnabled(FeatureFlags.VANILLA, FeatureFlags.REDSTONE_EXPERIMENTS);
		Identifier unknownId = Identifier.fromNamespaceAndPath(TESTMOD_ID, "unknown_feature_to_test_condition");
		ResourceCondition unknown = ResourceConditions.featuresEnabled(unknownId);
		// Passing an array to avoid type ambiguity
		ResourceCondition empty = ResourceConditions.featuresEnabled(new FeatureFlag[]{});

		expectCondition(helper, "vanilla only", vanilla, true);
		expectCondition(helper, "vanilla and redstone experiments", vanillaAndRedstoneExperiments, true);
		expectCondition(helper, "unknown feature ID", unknown, false);
		expectCondition(helper, "no feature", empty, true);

		helper.succeed();
	}

	@GameTest
	public void registryContains(GameTestHelper helper) {
		// Dynamic registry (in vitro; separate testmod needs to determine if this actually functions while loading)
		ResourceCondition plains = ResourceConditions.registryContains(Biomes.PLAINS);
		ResourceCondition unknownBiome = ResourceConditions.registryContains(ResourceKey.create(Registries.BIOME, UNKNOWN_ENTRY_ID));
		ResourceCondition emptyDynamic = ResourceConditions.registryContains(Registries.BIOME, new Identifier[]{});

		expectCondition(helper, "plains", plains, true);
		expectCondition(helper, "unknown biome", unknownBiome, false);
		expectCondition(helper, "biome registry, empty check", emptyDynamic, true);

		helper.succeed();
	}

	@GameTest
	public void tagsPopulated(GameTestHelper helper) {
		// Static registry
		ResourceCondition dirt = ResourceConditions.tagsPopulated(Registries.BLOCK, BlockTags.DIRT);
		ResourceCondition dirtAndUnknownBlock = ResourceConditions.tagsPopulated(Registries.BLOCK, BlockTags.DIRT, TagKey.create(Registries.BLOCK, UNKNOWN_ENTRY_ID));
		ResourceCondition emptyBlock = ResourceConditions.tagsPopulated(Registries.BLOCK);
		ResourceCondition unknownRegistry = ResourceConditions.tagsPopulated(UNKNOWN_REGISTRY_KEY, TagKey.create(UNKNOWN_REGISTRY_KEY, UNKNOWN_ENTRY_ID));
		ResourceCondition emptyUnknown = ResourceConditions.tagsPopulated(UNKNOWN_REGISTRY_KEY);

		expectCondition(helper, "dirt tag", dirt, true);
		expectCondition(helper, "dirt tag and unknown tag", dirtAndUnknownBlock, false);
		expectCondition(helper, "block registry, empty tag checks", emptyBlock, true);
		expectCondition(helper, "unknown registry, non-empty tag checks", unknownRegistry, false);
		expectCondition(helper, "unknown registry, empty tag checks", emptyUnknown, true);

		// Dynamic registry (in vitro; separate testmod needs to determine if this actually functions while loading)
		ResourceCondition forest = ResourceConditions.tagsPopulated(Registries.BIOME, BiomeTags.IS_FOREST);
		ResourceCondition unknownBiome = ResourceConditions.tagsPopulated(Registries.BIOME, TagKey.create(Registries.BIOME, UNKNOWN_ENTRY_ID));
		ResourceCondition emptyDynamic = ResourceConditions.tagsPopulated(Registries.BIOME);

		expectCondition(helper, "forest tag", forest, true);
		expectCondition(helper, "unknown biome tag", unknownBiome, false);
		expectCondition(helper, "biome registry, empty tag check", emptyDynamic, true);

		helper.succeed();
	}
}

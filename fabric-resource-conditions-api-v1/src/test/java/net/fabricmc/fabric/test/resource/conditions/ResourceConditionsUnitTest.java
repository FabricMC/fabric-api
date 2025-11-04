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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

public class ResourceConditionsUnitTest {
	private static final String TESTMOD_ID = "fabric-resource-conditions-api-v1-testmod";
	private static final String API_MOD_ID = "fabric-resource-conditions-api-v1";
	private static final String UNKNOWN_MOD_ID = "fabric-tiny-potato-api-v1";
	private static final ResourceKey<? extends Registry<Object>> UNKNOWN_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(TESTMOD_ID, "unknown_registry"));
	private static final Identifier UNKNOWN_ENTRY_ID = Identifier.fromNamespaceAndPath(TESTMOD_ID, "tiny_potato");

	private static void expectCondition(String name, ResourceCondition condition, boolean expected) {
		HolderLookup.Provider registryLookup = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		boolean actual = condition.test(new RegistryOps.HolderLookupAdapter(registryLookup));

		if (actual != expected) {
			throw new AssertionError("Test \"%s\" for condition %s failed; expected %s, got %s".formatted(name, condition.getType().id(), expected, actual));
		}

		// Test serialization
		ResourceCondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow(message -> new AssertionError("Could not serialize \"%s\": %s".formatted(name, message)));
	}

	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	public void logics() {
		ResourceCondition alwaysTrue = ResourceConditions.alwaysTrue();
		ResourceCondition alwaysFalse = ResourceConditions.not(alwaysTrue);
		ResourceCondition trueAndTrue = ResourceConditions.and(alwaysTrue, alwaysTrue);
		ResourceCondition trueAndFalse = ResourceConditions.and(alwaysTrue, alwaysFalse);
		ResourceCondition emptyAnd = ResourceConditions.and();
		ResourceCondition trueOrFalse = ResourceConditions.or(alwaysTrue, alwaysFalse);
		ResourceCondition falseOrFalse = ResourceConditions.or(alwaysFalse, alwaysFalse);
		ResourceCondition emptyOr = ResourceConditions.or();

		expectCondition("always true", alwaysTrue, true);
		expectCondition("always false", alwaysFalse, false);
		expectCondition("true and true", trueAndTrue, true);
		expectCondition("true and false", trueAndFalse, false);
		expectCondition("vacuous truth", emptyAnd, true);
		expectCondition("true or false", trueOrFalse, true);
		expectCondition("false or false", falseOrFalse, false);
		expectCondition("empty OR is always false", emptyOr, false);
	}

	@Test
	public void allModsLoaded() {
		ResourceCondition testmod = ResourceConditions.allModsLoaded(TESTMOD_ID);
		ResourceCondition testmodAndApi = ResourceConditions.allModsLoaded(TESTMOD_ID, API_MOD_ID);
		ResourceCondition unknownMod = ResourceConditions.allModsLoaded(UNKNOWN_MOD_ID);
		ResourceCondition unknownAndTestmod = ResourceConditions.allModsLoaded(UNKNOWN_MOD_ID, TESTMOD_ID);
		ResourceCondition noMod = ResourceConditions.allModsLoaded();

		expectCondition("one loaded mod", testmod, true);
		expectCondition("two loaded mods", testmodAndApi, true);
		expectCondition("one unloaded mod", unknownMod, false);
		expectCondition("both loaded and unloaded mods", unknownAndTestmod, false);
		expectCondition("no mod", noMod, true);
	}

	@Test
	public void anyModsLoaded() {
		ResourceCondition testmod = ResourceConditions.anyModsLoaded(TESTMOD_ID);
		ResourceCondition testmodAndApi = ResourceConditions.anyModsLoaded(TESTMOD_ID, API_MOD_ID);
		ResourceCondition unknownMod = ResourceConditions.anyModsLoaded(UNKNOWN_MOD_ID);
		ResourceCondition unknownAndTestmod = ResourceConditions.anyModsLoaded(UNKNOWN_MOD_ID, TESTMOD_ID);
		ResourceCondition noMod = ResourceConditions.anyModsLoaded();

		expectCondition("one loaded mod", testmod, true);
		expectCondition("two loaded mods", testmodAndApi, true);
		expectCondition("one unloaded mod", unknownMod, false);
		expectCondition("both loaded and unloaded mods", unknownAndTestmod, true);
		expectCondition("no mod", noMod, false);
	}

	@Test
	public void registryContains() {
		ResourceKey<Block> dirtKey = BuiltInRegistries.BLOCK.getResourceKey(Blocks.DIRT).orElseThrow();
		ResourceCondition dirt = ResourceConditions.registryContains(dirtKey);
		ResourceCondition dirtAndUnknownBlock = ResourceConditions.registryContains(dirtKey, ResourceKey.create(Registries.BLOCK, UNKNOWN_ENTRY_ID));
		ResourceCondition emptyBlock = ResourceConditions.registryContains(Registries.BLOCK, new Identifier[]{});
		ResourceCondition unknownRegistry = ResourceConditions.registryContains(UNKNOWN_REGISTRY_KEY, UNKNOWN_ENTRY_ID);
		ResourceCondition emptyUnknown = ResourceConditions.registryContains(UNKNOWN_REGISTRY_KEY, new Identifier[]{});

		expectCondition("dirt", dirt, true);
		expectCondition("dirt and unknown block", dirtAndUnknownBlock, false);
		expectCondition("block registry, empty check", emptyBlock, true);
		expectCondition("unknown registry, non-empty", unknownRegistry, false);
		expectCondition("unknown registry, empty", emptyUnknown, true);
	}
}

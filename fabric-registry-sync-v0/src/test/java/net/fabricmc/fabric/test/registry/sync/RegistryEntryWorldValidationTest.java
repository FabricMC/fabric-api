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

package net.fabricmc.fabric.test.registry.sync;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;

import net.fabricmc.fabric.impl.registry.sync.validate.RegistryCustomContentState;
import net.fabricmc.loader.api.FabricLoader;

public class RegistryEntryWorldValidationTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void emptyAlwaysPasses() {
		RegistryAccess access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

		RegistryCustomContentState.Missing missing = RegistryCustomContentState.EMPTY.validate(access);
		assertTrue(missing.isEmpty());
	}

	@Test
	void missingEntriesTest() {
		RegistryAccess access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

		Identifier fakeRegistry = Identifier.fromNamespaceAndPath("fabric", "fake_registry");
		Identifier blocksRegistry = Registries.BLOCK.identifier();

		Identifier fakeEntry1 = Identifier.fromNamespaceAndPath("fabric", "fake_entry_1");
		Identifier fakeEntry2 = Identifier.fromNamespaceAndPath("fabric", "fake_entry_2");
		Identifier fakeEntry3 = Identifier.fromNamespaceAndPath("fabric", "fake_entry_3");
		Identifier grassEntry = BlockItemIds.GRASS_BLOCK.block().identifier();
		Identifier dirtEntry = BlockItemIds.DIRT.block().identifier();

		RegistryCustomContentState state = new RegistryCustomContentState(Map.of(
				fakeRegistry, List.of(fakeEntry1, fakeEntry2, fakeEntry3), // 3 entries, fake registry
				blocksRegistry, List.of(fakeEntry1, grassEntry, fakeEntry2, dirtEntry, fakeEntry3) // 5 entries, vanilla registry
		), RegistryCustomContentState.Status.VALID);

		RegistryCustomContentState.Missing missing = state.validate(access);

		assertFalse(missing.isEmpty());
		assertNull(missing.entries().get(fakeRegistry));
		assertTrue(missing.registries().contains(fakeRegistry));

		List<Identifier> missingBlocks = missing.entries().get(blocksRegistry);

		assertNotNull(missingBlocks);
		assertFalse(missing.registries().contains(blocksRegistry));
		assertEquals(3, missing.entries().get(blocksRegistry).size());
		assertTrue(missingBlocks.contains(fakeEntry1));
		assertTrue(missingBlocks.contains(fakeEntry2));
		assertTrue(missingBlocks.contains(fakeEntry3));
		assertFalse(missingBlocks.contains(grassEntry));
		assertFalse(missingBlocks.contains(dirtEntry));
	}

	@Test
	void fileReadWriteTest() throws IOException {
		Path testPath = FabricLoader.getInstance().getGameDir().resolve("fabric", "registry_entries.dat");

		RegistryCustomContentState writeState = new RegistryCustomContentState(new HashMap<>(), RegistryCustomContentState.Status.VALID);

		generateFakeEntries(writeState, Registries.BLOCK.identifier(), 50, 10000);
		generateFakeEntries(writeState, Registries.ITEM.identifier(), 55, 10000);
		generateFakeEntries(writeState, Registries.ENTITY_TYPE.identifier(), 5, 15);
		generateFakeEntries(writeState, Registries.BLOCK_ENTITY_TYPE.identifier(), 10, 15);
		generateFakeEntries(writeState, Registries.VILLAGER_PROFESSION.identifier(), 2, 1);
		generateFakeEntries(writeState, Registries.ATTRIBUTE.identifier(), 8, 3);

		assertDoesNotThrow(() -> RegistryCustomContentState.writeFile(testPath, writeState));

		RegistryCustomContentState readState = RegistryCustomContentState.readFile(testPath);
		assertEquals(RegistryCustomContentState.Status.VALID, readState.status());
		assertEquals(writeState, readState);

		Files.deleteIfExists(testPath);
	}

	private void generateFakeEntries(RegistryCustomContentState state, Identifier registry, int amountNamespaces, int amountEntries) {
		RandomSource random = RandomSource.createThreadLocalInstance(registry.hashCode());
		List<Identifier> identifiers = state.entries().computeIfAbsent(registry, _ -> new ArrayList<>());

		for (int in = 0; in < amountNamespaces; in++) {
			String namespace = "fabric_" + random.nextInt();

			for (int ie = 0; ie < amountEntries; ie++) {
				identifiers.add(Identifier.fromNamespaceAndPath(namespace, RandomStringUtils.insecure().next(16, "abcdefghijklmnopqrstuvwxyz0123456789_")));
			}
		}
	}
}

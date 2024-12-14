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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;

public class RegistryAliasTest {
	private static final Identifier OBSOLETE_ID = id("obsolete");
	private static final Identifier NEW_ID = id("new");
	private static final Identifier OTHER = id("other");
	private RegistryKey<Registry<String>> testRegistryKey;
	private Registry<String> testRegistry;

	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	private static Identifier id(String s) {
		return Identifier.of("registry_sync_test_alias_test", s);
	}

	@BeforeEach
	void beforeEach() {
		testRegistryKey = RegistryKey.ofRegistry(id(UUID.randomUUID().toString()));
		testRegistry = Mockito.spy(FabricRegistryBuilder.createSimple(testRegistryKey).buildAndRegister());

		Registry.register(testRegistry, NEW_ID, "entry");
		Registry.register(testRegistry, OTHER, "other");
		testRegistry.addAlias(OBSOLETE_ID, NEW_ID);
	}

	@Test
	void testAlias() {
		RegistryKey<String> obsoleteKey = RegistryKey.of(testRegistryKey, OBSOLETE_ID);

		assertTrue(testRegistry.containsId(OBSOLETE_ID));
		assertFalse(testRegistry.getIds().contains(OBSOLETE_ID));
		assertEquals("entry", testRegistry.get(OBSOLETE_ID));
		assertEquals("entry", testRegistry.get(obsoleteKey));

		Identifier moreObsolete = id("more_obsolete");
		assertFalse(testRegistry.containsId(moreObsolete));

		testRegistry.addAlias(moreObsolete, OBSOLETE_ID);

		assertTrue(testRegistry.containsId(moreObsolete));
		assertEquals("entry", testRegistry.get(moreObsolete));
	}

	@Test
	void forbidAmbiguousAlias() {
		assertThrows(IllegalArgumentException.class, () -> testRegistry.addAlias(OBSOLETE_ID, OTHER));
	}

	@Test
	void forbidCircularAliases() {
		assertThrows(IllegalArgumentException.class, () -> testRegistry.addAlias(NEW_ID, OBSOLETE_ID));
	}

	@Test
	void forbidExistingIdAsAlias() {
		assertThrows(IllegalArgumentException.class, () -> testRegistry.addAlias(NEW_ID, OTHER));
	}

	@Test
	void forbidOverridingAliasWithEntry() {
		assertThrows(IllegalArgumentException.class, () -> Registry.register(testRegistry, OBSOLETE_ID, "obsolete"));
	}
}

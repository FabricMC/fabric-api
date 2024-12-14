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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class RegistryKeysTest {
	private static final RegistryKey<Registry<Object>> TEST_KEY = RegistryKey.ofRegistry(Identifier.of("registry-keys", "test"));

	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void getPath() {
		assertEquals("item", RegistryKeys.getPath(RegistryKeys.ITEM));
		assertEquals("registry-keys/test", RegistryKeys.getPath(TEST_KEY));
	}

	@Test
	void getTagPath() {
		assertEquals("tags/item", RegistryKeys.getTagPath(RegistryKeys.ITEM));
		assertEquals("tags/registry-keys/test", RegistryKeys.getTagPath(TEST_KEY));
	}
}

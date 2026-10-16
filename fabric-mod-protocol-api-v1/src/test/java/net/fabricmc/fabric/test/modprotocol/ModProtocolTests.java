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

package net.fabricmc.fabric.test.modprotocol;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.modprotocol.v1.ModProtocolIds;
import net.fabricmc.fabric.api.modprotocol.v1.ModProtocolRegistry;
import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;

public class ModProtocolTests {
	@BeforeAll
	static void beforeAll() {
		for (int i = 0; i < 200; i++) {
			ModProtocolRegistry.register(ModProtocolIds.mod("test_mod_id_" + i), "Tested Mod v" + i, "1.2.3", Optional.of(">=0.0.0"));
		}
	}

	@Test
	void emptyMatchTest() {
		ModProtocolManager.ValidationResult result = ModProtocolManager.validate(Map.of(), List.of(), List.of());

		assertTrue(result.isSuccess());
		assertTrue(result.missingLocal().isEmpty());
		assertTrue(result.missingRemote().isEmpty());
		assertTrue(result.missing().isEmpty());
		assertFalse(result.forceNoRemote());
	}

	@Test
	void equalReceivedClientTest() {
		ModProtocolManager.ValidationResult result = ModProtocolManager.validateClient(ModProtocolManager.REQUIRED_ON_CLIENT);

		assertTrue(result.isSuccess());
		assertTrue(result.missingLocal().isEmpty());
		assertTrue(result.missingRemote().isEmpty());
		assertTrue(result.missing().isEmpty());
		assertFalse(result.forceNoRemote());
	}

	@Test
	void emptyReceivedClientTest() {
		ModProtocolManager.ValidationResult result = ModProtocolManager.validateClient(List.of());

		assertFalse(result.isSuccess());
		assertTrue(result.missingLocal().isEmpty());
		assertFalse(result.missingRemote().isEmpty());
		assertFalse(result.missing().isEmpty());
		assertFalse(result.forceNoRemote());
	}

	@Test
	void emptyReceivedFromServerEmptyClient() {
		ModProtocolManager.ValidationResult result = ModProtocolManager.validate(Map.of(), ModProtocolManager.REQUIRED_ON_CLIENT, List.of());

		assertFalse(result.isSuccess());
		assertFalse(result.missingLocal().isEmpty());
		assertTrue(result.missingRemote().isEmpty());
		assertFalse(result.missing().isEmpty());
		assertFalse(result.forceNoRemote());
	}
}

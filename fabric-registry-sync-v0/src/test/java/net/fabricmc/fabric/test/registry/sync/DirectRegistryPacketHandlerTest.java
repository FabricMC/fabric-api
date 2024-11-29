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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.registry.sync.packet.DirectRegistryPacketHandler;

public class DirectRegistryPacketHandlerTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void emptyRegistrySync() {
		DirectRegistryPacketHandler handler = new DirectRegistryPacketHandler();

		Map<Identifier, Object2IntMap<Identifier>> registry = new HashMap<>();

		var payloads = new ArrayList<DirectRegistryPacketHandler.Payload>();
		handler.sendPacket(payloads::add, registry);

		assertEquals(2, payloads.size());
		// Last payload is empty, indicating all the data has been sent.
		assertEquals(0, payloads.get(1).data().length);

		for (DirectRegistryPacketHandler.Payload payload : payloads) {
			handler.receivePayload(payload);
		}

		assertMatchesDeep(registry, handler.getSyncedPacketData().idMap());
	}

	@Test
	void singlePacketRegistrySync() {
		DirectRegistryPacketHandler handler = new DirectRegistryPacketHandler();

		Map<Identifier, Object2IntMap<Identifier>> registry = new HashMap<>();
		registry.put(Identifier.of("test"), createRegistry(150));

		var payloads = new ArrayList<DirectRegistryPacketHandler.Payload>();
		handler.sendPacket(payloads::add, registry);

		assertEquals(2, payloads.size());
		// Last payload is empty, indicating all the data has been sent.
		assertEquals(0, payloads.get(1).data().length);

		for (DirectRegistryPacketHandler.Payload payload : payloads) {
			handler.receivePayload(payload);
		}

		assertMatchesDeep(registry, handler.getSyncedPacketData().idMap());
	}

	@Test
	void splitPacketRegistrySync() {
		DirectRegistryPacketHandler handler = new DirectRegistryPacketHandler();
		Map<Identifier, Object2IntMap<Identifier>> registry = new HashMap<>();

		for (int i = 0; i < 50; i++) {
			registry.put(Identifier.of("test", "namespace_" + i), createRegistry(15000));
		}

		var payloads = new ArrayList<DirectRegistryPacketHandler.Payload>();
		handler.sendPacket(payloads::add, registry);

		// Expect 10 packets to be sent
		assertEquals(10, payloads.size());
		// Last payload is empty, indicating all the data has been sent.
		assertEquals(0, payloads.get(9).data().length);

		for (DirectRegistryPacketHandler.Payload payload : payloads) {
			handler.receivePayload(payload);
		}

		assertMatchesDeep(registry, handler.getSyncedPacketData().idMap());
	}

	private static Object2IntMap<Identifier> createRegistry(int size) {
		Object2IntMap<Identifier> entries = new Object2IntOpenHashMap<>();

		for (int i = 0; i < size; i++) {
			entries.put(Identifier.of("test", "entry_" + i), i);
		}

		return entries;
	}

	// Deep comparison of two maps of maps
	private static void assertMatchesDeep(Map<Identifier, Object2IntMap<Identifier>> expected, Map<Identifier, Object2IntMap<Identifier>> actual) {
		assertEquals(expected.size(), actual.size());

		for (Map.Entry<Identifier, Object2IntMap<Identifier>> entry : expected.entrySet()) {
			Object2IntMap<Identifier> actualValue = actual.get(entry.getKey());
			assertEquals(entry.getValue(), actualValue);
		}
	}
}

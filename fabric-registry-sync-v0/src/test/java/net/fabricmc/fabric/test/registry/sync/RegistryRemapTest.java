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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.impl.client.registry.sync.ClientRegistrySyncHandler;
import net.fabricmc.fabric.impl.registry.sync.RegistryAttributeImpl;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;
import net.fabricmc.fabric.impl.registry.sync.packet.DirectRegistryPacketHandler;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistryPacketHandler;

public class RegistryRemapTest {
	private RegistryKey<Registry<String>> testRegistryKey;
	private SimpleRegistry<String> testRegistry;

	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@BeforeEach
	void beforeEach() {
		testRegistryKey = RegistryKey.ofRegistry(id(UUID.randomUUID().toString()));
		testRegistry = FabricRegistryBuilder.createSimple(testRegistryKey)
				.attribute(RegistryAttribute.SYNCED)
				.buildAndRegister();

		Registry.register(testRegistry, id("zero"), "zero");
		Registry.register(testRegistry, id("one"), "one");
		Registry.register(testRegistry, id("two"), "two");
	}

	@AfterEach
	void afterEach() throws RemapException {
		// If a test fails, make sure we unmap the registry to avoid affecting other tests
		RemappableRegistry remappableRegistry = (RemappableRegistry) testRegistry;
		remappableRegistry.unmap();
	}

	@Test
	void remapRegistry() throws RemapException {
		RemappableRegistry remappableRegistry = (RemappableRegistry) testRegistry;

		assertEquals(0, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(2, testRegistry.getRawId("two"));

		Map<Identifier, Integer> idMap = Map.of(
				id("zero"), 2,
				id("one"), 1,
				id("two"), 0
		);
		remappableRegistry.remap(asFastMap(idMap), RemappableRegistry.RemapMode.AUTHORITATIVE);

		assertEquals(2, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(0, testRegistry.getRawId("two"));

		remappableRegistry.unmap();

		assertEquals(0, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(2, testRegistry.getRawId("two"));
	}

	@Test
	void remapRegistryViaPacket() throws RemapException {
		RemappableRegistry remappableRegistry = (RemappableRegistry) testRegistry;

		assertEquals(0, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(2, testRegistry.getRawId("two"));

		Map<Identifier, Integer> idMap = Map.of(
				id("two"), 0,
				id("one"), 1,
				id("zero"), 2
		);

		var payloads = new ArrayList<DirectRegistryPacketHandler.Payload>();

		RegistrySyncManager.DIRECT_PACKET_HANDLER.sendPacket(
				payloads::add,
				Map.of(testRegistryKey.getValue(), asFastMap(idMap))
		);

		List<Boolean> results = receivePayloads(payloads);

		// Expect 2 packets, 1 with the data (as it fits in one packet) and 1 empty packet to signal the end
		assertEquals(2, results.size());
		assertFalse(results.getFirst());
		assertTrue(results.get(1));

		assertEquals(2, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(0, testRegistry.getRawId("two"));

		remappableRegistry.unmap();

		assertEquals(0, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(2, testRegistry.getRawId("two"));
	}

	@Test
	void unknownEntry() {
		Map<Identifier, Integer> idMap = Map.of(
				id("two"), 0,
				id("one"), 1,
				id("zero"), 2,
				id("unknown"), 3
		);

		var payloads = new ArrayList<DirectRegistryPacketHandler.Payload>();

		RegistrySyncManager.DIRECT_PACKET_HANDLER.sendPacket(
				payloads::add,
				Map.of(testRegistryKey.getValue(), asFastMap(idMap))
		);

		RemapException remapException = assertThrows(RemapException.class, () -> receivePayloads(payloads));
		assertTrue(remapException.getMessage().contains("unknown-remote"));
	}

	@Test
	void unknownRegistry() {
		Map<Identifier, Integer> idMap = Map.of(
				id("two"), 0,
				id("one"), 1,
				id("zero"), 2
		);

		var payloads = new ArrayList<DirectRegistryPacketHandler.Payload>();

		RegistrySyncManager.DIRECT_PACKET_HANDLER.sendPacket(
				payloads::add,
				Map.of(id("unknown"), asFastMap(idMap))
		);

		RemapException remapException = assertThrows(RemapException.class, () -> receivePayloads(payloads));
		assertTrue(remapException.getMessage().contains("unknown-registry"));
	}

	@Test
	void unknownOptionalRegistry() throws RemapException {
		Map<Identifier, Integer> idMap = Map.of(
				id("two"), 0,
				id("one"), 1,
				id("zero"), 2
		);

		RegistryAttributeImpl holder = (RegistryAttributeImpl) RegistryAttributeHolder.get(testRegistryKey);
		holder.addAttribute(RegistryAttribute.OPTIONAL);

		var payloads = new ArrayList<DirectRegistryPacketHandler.Payload>();

		RegistrySyncManager.DIRECT_PACKET_HANDLER.sendPacket(
				payloads::add,
				Map.of(testRegistryKey.getValue(), asFastMap(idMap))
		);

		// Packet should be handled without issue.
		List<Boolean> results = receivePayloads(payloads);
		assertEquals(2, results.size());
		assertFalse(results.getFirst());
		assertTrue(results.get(1));

		holder.removeAttribute(RegistryAttribute.OPTIONAL);
	}

	@Test
	void missingRemoteEntries() throws RemapException {
		RemappableRegistry remappableRegistry = (RemappableRegistry) testRegistry;

		assertEquals(0, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(2, testRegistry.getRawId("two"));

		Map<Identifier, Integer> idMap = Map.of(
				id("two"), 0,
				id("zero"), 1
		);

		var payloads = new ArrayList<DirectRegistryPacketHandler.Payload>();

		RegistrySyncManager.DIRECT_PACKET_HANDLER.sendPacket(
				payloads::add,
				Map.of(testRegistryKey.getValue(), asFastMap(idMap))
		);

		receivePayloads(payloads);

		assertEquals(0, testRegistry.getRawId("two"));
		assertEquals(1, testRegistry.getRawId("zero"));
		// assigned an ID at the end of the registry
		assertEquals(2, testRegistry.getRawId("one"));

		remappableRegistry.unmap();

		assertEquals(0, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(2, testRegistry.getRawId("two"));
	}

	@Test
	void remapRegistryFromPacketData() throws RemapException {
		RemappableRegistry remappableRegistry = (RemappableRegistry) testRegistry;

		assertEquals(0, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(2, testRegistry.getRawId("two"));

		ClientRegistrySyncHandler.apply(new RegistryPacketHandler.SyncedPacketData(
				Map.of(
					testRegistryKey.getValue(), asFastMap(Map.of(
						id("zero"), 2,
						id("one"), 1,
						id("two"), 0
					))
				),
				Map.of()
		));

		assertEquals(2, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(0, testRegistry.getRawId("two"));

		remappableRegistry.unmap();

		assertEquals(0, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(2, testRegistry.getRawId("two"));
	}

	@Test
	void remapRegistryFromPacketDataIgnoreOptional() throws RemapException {
		RemappableRegistry remappableRegistry = (RemappableRegistry) testRegistry;

		assertEquals(0, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(2, testRegistry.getRawId("two"));

		ClientRegistrySyncHandler.apply(new RegistryPacketHandler.SyncedPacketData(
				Map.of(
					testRegistryKey.getValue(), asFastMap(Map.of(
							id("zero"), 2,
							id("one"), 1,
							id("two"), 0
					)),
					Identifier.of("test", "optional"), asFastMap(Map.of(
						id("test"), 0
					))
				),
				Map.of(
						Identifier.of("test", "optional"), EnumSet.of(RegistryAttribute.OPTIONAL)
				)
		));

		assertEquals(2, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(0, testRegistry.getRawId("two"));

		remappableRegistry.unmap();

		assertEquals(0, testRegistry.getRawId("zero"));
		assertEquals(1, testRegistry.getRawId("one"));
		assertEquals(2, testRegistry.getRawId("two"));
	}

	private static List<Boolean> receivePayloads(List<DirectRegistryPacketHandler.Payload> payloads) throws RemapException {
		var results = new ArrayList<Boolean>();

		try {
			for (DirectRegistryPacketHandler.Payload payload : payloads) {
				CompletableFuture<Boolean> future = ClientRegistrySyncHandler.receivePacket(
						ThisThreadExecutor.INSTANCE,
						RegistrySyncManager.DIRECT_PACKET_HANDLER,
						payload,
						true
				);
				results.add(future.get());
			}
		} catch (CompletionException e) {
			if (e.getCause() instanceof RemapException remapException) {
				throw remapException;
			}

			throw e;
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		return results;
	}

	private static Object2IntMap<Identifier> asFastMap(Map<Identifier, Integer> map) {
		var fastMap = new Object2IntOpenHashMap<Identifier>();
		fastMap.putAll(map);
		return fastMap;
	}

	private static Identifier id(String path) {
		return Identifier.of("registry_sync_test", path);
	}

	// Run the task on the current thread instantly
	private static class ThisThreadExecutor extends ThreadExecutor<Runnable> {
		public static final ThisThreadExecutor INSTANCE = new ThisThreadExecutor();

		private ThisThreadExecutor() {
			super("Test thread executor");
		}

		@Override
		protected boolean canExecute(Runnable task) {
			return true;
		}

		@Override
		protected Thread getThread() {
			return Thread.currentThread();
		}

		@Override
		public Runnable createTask(Runnable runnable) {
			return runnable;
		}
	}
}

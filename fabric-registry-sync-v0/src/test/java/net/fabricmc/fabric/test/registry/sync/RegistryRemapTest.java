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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.thread.BlockableEventLoop;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.impl.client.registry.sync.ClientRegistrySyncHandler;
import net.fabricmc.fabric.impl.registry.sync.RegistryAttributeImpl;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistrySyncPayload;

public class RegistryRemapTest {
	private ResourceKey<Registry<String>> testRegistryKey;
	private MappedRegistry<String> testRegistry;

	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@BeforeEach
	void beforeEach() {
		testRegistryKey = ResourceKey.createRegistryKey(id(UUID.randomUUID().toString()));
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

		assertEquals(0, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(2, testRegistry.getId("two"));

		Map<Identifier, Integer> idMap = Map.of(
				id("zero"), 2,
				id("one"), 1,
				id("two"), 0
		);
		remappableRegistry.remap(asFastMap(idMap), RemappableRegistry.RemapMode.AUTHORITATIVE);

		assertEquals(2, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(0, testRegistry.getId("two"));

		remappableRegistry.unmap();

		assertEquals(0, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(2, testRegistry.getId("two"));
	}

	@Test
	void remapRegistryViaPacket() throws RemapException {
		RemappableRegistry remappableRegistry = (RemappableRegistry) testRegistry;

		assertEquals(0, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(2, testRegistry.getId("two"));

		Map<Identifier, Integer> idMap = Map.of(
				id("two"), 0,
				id("one"), 1,
				id("zero"), 2
		);

		var payload = new RegistrySyncPayload(Map.of(testRegistryKey.identifier(), asFastMap(idMap)));

		ClientRegistrySyncHandler.apply(payload);

		assertEquals(2, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(0, testRegistry.getId("two"));

		remappableRegistry.unmap();

		assertEquals(0, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(2, testRegistry.getId("two"));
	}

	@Test
	void unknownEntry() {
		Map<Identifier, Integer> idMap = Map.of(
				id("two"), 0,
				id("one"), 1,
				id("zero"), 2,
				id("unknown"), 3
		);

		var payload = new RegistrySyncPayload(Map.of(testRegistryKey.identifier(), asFastMap(idMap)));

		RemapException remapException = assertThrows(RemapException.class, () -> ClientRegistrySyncHandler.apply(payload));
		assertTrue(remapException.getMessage().contains("unknown-remote"));
	}

	@Test
	void unknownRegistry() {
		Map<Identifier, Integer> idMap = Map.of(
				id("two"), 0,
				id("one"), 1,
				id("zero"), 2
		);

		var payload = new RegistrySyncPayload(Map.of(id("unknown"), asFastMap(idMap)));

		RemapException remapException = assertThrows(RemapException.class, () -> ClientRegistrySyncHandler.apply(payload));
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

		var payload = new RegistrySyncPayload(Map.of(testRegistryKey.identifier(), asFastMap(idMap)));

		// Packet should be handled without issue.
		ClientRegistrySyncHandler.apply(payload);

		holder.removeAttribute(RegistryAttribute.OPTIONAL);
	}

	@Test
	void missingRemoteEntries() throws RemapException {
		RemappableRegistry remappableRegistry = (RemappableRegistry) testRegistry;

		assertEquals(0, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(2, testRegistry.getId("two"));

		Map<Identifier, Integer> idMap = Map.of(
				id("two"), 0,
				id("zero"), 1
		);

		var payload = new RegistrySyncPayload(Map.of(testRegistryKey.identifier(), asFastMap(idMap)));

		ClientRegistrySyncHandler.apply(payload);

		assertEquals(0, testRegistry.getId("two"));
		assertEquals(1, testRegistry.getId("zero"));
		// assigned an ID at the end of the registry
		assertEquals(2, testRegistry.getId("one"));

		remappableRegistry.unmap();

		assertEquals(0, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(2, testRegistry.getId("two"));
	}

	@Test
	void remapRegistryFromPacketData() throws RemapException {
		RemappableRegistry remappableRegistry = (RemappableRegistry) testRegistry;

		assertEquals(0, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(2, testRegistry.getId("two"));

		ClientRegistrySyncHandler.apply(new RegistrySyncPayload(
				Map.of(
					testRegistryKey.identifier(), asFastMap(Map.of(
						id("zero"), 2,
						id("one"), 1,
						id("two"), 0
					))
				),
				Map.of()
		));

		assertEquals(2, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(0, testRegistry.getId("two"));

		remappableRegistry.unmap();

		assertEquals(0, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(2, testRegistry.getId("two"));
	}

	@Test
	void remapRegistryFromPacketDataIgnoreOptional() throws RemapException {
		RemappableRegistry remappableRegistry = (RemappableRegistry) testRegistry;

		assertEquals(0, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(2, testRegistry.getId("two"));

		ClientRegistrySyncHandler.apply(new RegistrySyncPayload(
				Map.of(
					testRegistryKey.identifier(), asFastMap(Map.of(
							id("zero"), 2,
							id("one"), 1,
							id("two"), 0
					)),
					Identifier.fromNamespaceAndPath("test", "optional"), asFastMap(Map.of(
						id("test"), 0
					))
				),
				Map.of(
						Identifier.fromNamespaceAndPath("test", "optional"), EnumSet.of(RegistryAttribute.OPTIONAL)
				)
		));

		assertEquals(2, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(0, testRegistry.getId("two"));

		remappableRegistry.unmap();

		assertEquals(0, testRegistry.getId("zero"));
		assertEquals(1, testRegistry.getId("one"));
		assertEquals(2, testRegistry.getId("two"));
	}

	private static Object2IntMap<Identifier> asFastMap(Map<Identifier, Integer> map) {
		var fastMap = new Object2IntOpenHashMap<Identifier>();
		fastMap.putAll(map);
		return fastMap;
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("registry_sync_test", path);
	}

	// Run the task on the current thread instantly
	private static class ThisThreadExecutor extends BlockableEventLoop<Runnable> {
		public static final ThisThreadExecutor INSTANCE = new ThisThreadExecutor();

		private ThisThreadExecutor() {
			super("Test thread executor");
		}

		@Override
		protected boolean shouldRun(Runnable task) {
			return true;
		}

		@Override
		protected Thread getRunningThread() {
			return Thread.currentThread();
		}

		@Override
		public Runnable wrapRunnable(Runnable runnable) {
			return runnable;
		}
	}
}

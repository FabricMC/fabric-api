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

package net.fabricmc.fabric.test.registry.sync.client;

import static net.fabricmc.fabric.test.registry.sync.CustomDynamicRegistryTest.TEST_EMPTY_SYNCED_DYNAMIC_REGISTRY_KEY;
import static net.fabricmc.fabric.test.registry.sync.CustomDynamicRegistryTest.TEST_NESTED_DYNAMIC_REGISTRY_KEY;
import static net.fabricmc.fabric.test.registry.sync.CustomDynamicRegistryTest.TEST_SYNCED_1_DYNAMIC_REGISTRY_KEY;
import static net.fabricmc.fabric.test.registry.sync.CustomDynamicRegistryTest.TEST_SYNCED_2_DYNAMIC_REGISTRY_KEY;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.test.registry.sync.TestDynamicObject;
import net.fabricmc.fabric.test.registry.sync.TestNestedDynamicObject;

public final class DynamicRegistryClientTest implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Identifier SYNCED_ID = Identifier.fromNamespaceAndPath("fabric-registry-sync-v0-testmod", "synced");

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			LOGGER.info("Starting dynamic registry sync tests...");

			TestDynamicObject synced1 = handler.registryAccess()
					.lookupOrThrow(TEST_SYNCED_1_DYNAMIC_REGISTRY_KEY)
					.getValue(SYNCED_ID);
			TestDynamicObject synced2 = handler.registryAccess()
					.lookupOrThrow(TEST_SYNCED_2_DYNAMIC_REGISTRY_KEY)
					.getValue(SYNCED_ID);
			TestNestedDynamicObject simpleNested = handler.registryAccess()
					.lookupOrThrow(TEST_NESTED_DYNAMIC_REGISTRY_KEY)
					.getValue(SYNCED_ID);

			LOGGER.info("Synced - simple: {}", synced1);
			LOGGER.info("Synced - custom network codec: {}", synced2);
			LOGGER.info("Synced - simple nested: {}", simpleNested);

			if (synced1 == null) {
				didNotReceive(TEST_SYNCED_1_DYNAMIC_REGISTRY_KEY, SYNCED_ID);
			}

			if (synced1.usesNetworkCodec()) {
				throw new AssertionError("Entries in " + TEST_SYNCED_1_DYNAMIC_REGISTRY_KEY + " should not use network codec");
			}

			if (synced2 == null) {
				didNotReceive(TEST_SYNCED_2_DYNAMIC_REGISTRY_KEY, SYNCED_ID);
			}

			// In 24w04a, dynamic registries are always serialized and sent even in singleplayer.
			if (!synced2.usesNetworkCodec()) {
				LOGGER.error("Entries in " + TEST_SYNCED_2_DYNAMIC_REGISTRY_KEY + " should use network codec");
			}

			// TODO 1.20.2
			//if (simpleNested == null) {
			//	didNotReceive(TEST_NESTED_DYNAMIC_REGISTRY_KEY, SYNCED_ID);
			//}

			//if (simpleNested.nested().value() != synced1) {
			//	throw new AssertionError("Did not match up synced nested entry to the other synced value");
			//}

			// See ClientRegistriesDynamicBuiltInRegistriesMixin
			if (handler.registryAccess().lookup(TEST_EMPTY_SYNCED_DYNAMIC_REGISTRY_KEY).isPresent()) {
				throw new AssertionError("Received empty registry that should have been skipped");
			}

			LOGGER.info("Dynamic registry sync tests passed!");
		});
	}

	private static void didNotReceive(ResourceKey<? extends Registry<?>> registryKey, Identifier entryId) {
		throw new AssertionError("Did not receive " + registryKey + "/" + entryId);
	}
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.logging.LogUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;

public class RegistrySyncTest implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	/**
	 * These are system property's as it allows for easier testing with different run configurations.
	 */
	public static final boolean REGISTER_BLOCKS = Boolean.parseBoolean(System.getProperty("fabric.registry.sync.test.register.blocks", "true"));
	public static final boolean REGISTER_ITEMS = Boolean.parseBoolean(System.getProperty("fabric.registry.sync.test.register.items", "true"));

	// Store a list of Registries used with PacketCodecs.registry, and then check that they are marked as synced when the server starts.
	// We check them later as they may be used before the registry attributes are assigned.
	private static boolean hasCheckedEarlyRegistries = false;
	private static final List<ResourceKey<? extends Registry<?>>> sycnedRegistriesToCheck = new ArrayList<>();

	@Override
	public void onInitialize() {
		if (REGISTER_BLOCKS) {
			// For checking raw id bulk in direct registry packet, make registry_sync namespace have two bulks.
			registerBlocks("registry_sync", 5, 0);
			registerBlocks("registry_sync2", 50, 0);
			registerBlocks("registry_sync", 2, 5);

			Validate.isTrue(RegistryAttributeHolder.get(BuiltInRegistries.BLOCK).hasAttribute(RegistryAttribute.MODDED), "Modded block was registered but registry not marked as modded");

			if (REGISTER_ITEMS) {
				Validate.isTrue(RegistryAttributeHolder.get(BuiltInRegistries.ITEM).hasAttribute(RegistryAttribute.MODDED), "Modded item was registered but registry not marked as modded");
			}
		}

		ResourceKey<Registry<String>> fabricRegistryKey = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("registry_sync", "fabric_registry"));
		MappedRegistry<String> fabricRegistry = FabricRegistryBuilder.createSimple(fabricRegistryKey)
				.attribute(RegistryAttribute.SYNCED)
				.buildAndRegister();

		Registry.register(fabricRegistry, Identifier.fromNamespaceAndPath("registry_sync", "test"), "test");

		Validate.isTrue(BuiltInRegistries.REGISTRY.keySet().contains(Identifier.fromNamespaceAndPath("registry_sync", "fabric_registry")));

		Validate.isTrue(RegistryAttributeHolder.get(fabricRegistry).hasAttribute(RegistryAttribute.MODDED));
		Validate.isTrue(RegistryAttributeHolder.get(fabricRegistry).hasAttribute(RegistryAttribute.SYNCED));

		final AtomicBoolean setupCalled = new AtomicBoolean(false);

		DynamicRegistrySetupCallback.EVENT.register(registryManager -> {
			setupCalled.set(true);
			registryManager.registerEntryAdded(Registries.BIOME, (rawId, id, object) -> {
				LOGGER.info("Biome added: {}", id);
			});
		});

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			hasCheckedEarlyRegistries = true;
			sycnedRegistriesToCheck.forEach(RegistrySyncTest::checkSyncedRegistry);

			if (!setupCalled.get()) {
				throw new IllegalStateException("DRM setup was not called before startup!");
			}
		});

		// Vanilla status effects don't have an entry for the int id 0, test we can handle this.
		RegistryAttributeHolder.get(BuiltInRegistries.MOB_EFFECT).addAttribute(RegistryAttribute.MODDED);
	}

	public static void checkSyncedRegistry(ResourceKey<? extends Registry<?>> registry) {
		if (!BuiltInRegistries.REGISTRY.containsKey(registry.identifier())) {
			// Skip dynamic registries, as there are always synced.
			return;
		}

		if (!hasCheckedEarlyRegistries) {
			sycnedRegistriesToCheck.add(registry);
			return;
		}

		if (registry.identifier().equals(Identifier.parse("recipe_serializer"))) {
			// Recipe serializers are not synced, as there is an unused codec left over.
			return;
		}

		if (!RegistryAttributeHolder.get(registry).hasAttribute(RegistryAttribute.SYNCED)) {
			throw new IllegalStateException("Registry " + registry.identifier() + " is not marked as SYNCED!");
		}
	}

	private static void registerBlocks(String namespace, int amount, int startingId) {
		for (int i = 0; i < amount; i++) {
			Identifier id = Identifier.fromNamespaceAndPath(namespace, "block_" + (i + startingId));
			Block block = new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
			Registry.register(BuiltInRegistries.BLOCK, id, block);

			if (REGISTER_ITEMS) {
				BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
				Registry.register(BuiltInRegistries.ITEM, id, blockItem);
			}
		}
	}
}

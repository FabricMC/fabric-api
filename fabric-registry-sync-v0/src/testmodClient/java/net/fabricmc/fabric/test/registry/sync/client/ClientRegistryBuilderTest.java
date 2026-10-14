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

import net.minecraft.client.core.registries.ClientRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.registry.v0.FabricClientRegistryBuilder;

public class ClientRegistryBuilderTest implements ClientModInitializer {
	public static final ResourceKey<Registry<String>> CLIENT_REGISTRY_KEY = ResourceKey.createRegistryKey(
			ClientRegistries.ROOT_REGISTRY_NAME,
			Identifier.fromNamespaceAndPath("fabric-registry-sync-v0-testmod", "client_registry")
	);
	public static final Registry<String> CLIENT_REGISTRY = FabricClientRegistryBuilder.create(CLIENT_REGISTRY_KEY).buildAndRegister();

	@Override
	public void onInitializeClient() {
		Registry.register(CLIENT_REGISTRY, Identifier.fromNamespaceAndPath("fabric-registry-sync-v0-testmod", "example"), "example");
	}
}

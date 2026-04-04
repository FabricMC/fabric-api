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

package net.fabricmc.fabric.test.tag.client.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class ClientTagTest implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTagTest.class);

	public static final String MOD_ID = "fabric-tag-api-v1-testmod";
	protected static final Identifier BUILT_IN_PACK_ID = Identifier.fromNamespaceAndPath(MOD_ID, "test");

	@Override
	public void onInitializeClient() {
		final ModContainer container = FabricLoader.getInstance().getModContainer(MOD_ID).get();

		if (!ResourceLoader.registerBuiltinPack(BUILT_IN_PACK_ID, container, PackActivationType.DEFAULT_ENABLED)) {
			throw new IllegalStateException("Could not register built-in resource pack.");
		}
	}
}

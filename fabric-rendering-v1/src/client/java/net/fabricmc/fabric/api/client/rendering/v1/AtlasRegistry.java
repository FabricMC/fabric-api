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

package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.List;

import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.client.rendering.AtlasRegistryImpl;

/**
 * A registry to add atlases to {@link net.minecraft.client.resources.model.sprite.AtlasManager}.
 */
public final class AtlasRegistry {
	/**
	 * Registers an atlas using an atlas config.
	 *
	 * @param config The atlas config to register.
	 */
	public static void register(AtlasManager.AtlasConfig config) {
		AtlasRegistryImpl.register(config);
	}

	/**
	 * Generates a texture id based on an atlas id.
	 * @param atlasId The atlas id to generate a texture id for.
	 * @return The generated texture id.
	 */
	public static Identifier generateTextureLocation(Identifier atlasId) {
		return AtlasRegistryImpl.generateTextureLocation(atlasId);
	}

	/**
	 * Get all registered atlases.
	 *
	 * @return The currently registered atlases.
	 */
	public static List<AtlasManager.AtlasConfig> getAtlases() {
		return AtlasRegistryImpl.getAtlases();
	}

	private AtlasRegistry() { }
}

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

import java.util.Set;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;

import net.fabricmc.fabric.impl.client.rendering.AtlasRegistryImpl;

/**
 * A registry to add atlases to {@link net.minecraft.client.resources.model.sprite.AtlasManager}.
 */
public final class AtlasRegistry {
	/**
	 * Registers an atlas using a given texture and atlas id.
	 *
	 * @param textureId The id of the texture that will be generated.
	 * @param atlasId The id of the registered atlas. Can be generated with {@link AtlasRegistry#createTextureLocation(Identifier)}.
	 * @param hasMipmaps Whether to generate mipmaps for the atlas.
	 * @param additionalMetadata Additional metadata to be set in .mcmeta files.
	 */
	public static void register(Identifier textureId, Identifier atlasId, boolean hasMipmaps, Set<MetadataSectionType<?>> additionalMetadata) {
		AtlasRegistryImpl.register(textureId, atlasId, hasMipmaps, additionalMetadata);
	}

	/**
	 * Registers an atlas using a given texture and atlas id.
	 *
	 * @param textureId The id of the texture that will be generated. Can be generated with {@link AtlasRegistry#createTextureLocation(Identifier)}.
	 * @param atlasId The id of the registered atlas.
	 * @param hasMipmaps Whether to generate mipmaps for the atlas.
	 */
	public static void register(Identifier textureId, Identifier atlasId, boolean hasMipmaps) {
		AtlasRegistryImpl.register(textureId, atlasId, hasMipmaps);
	}

	/**
	 * Generates a texture id based on an atlas id.
	 * @param atlasId The atlas id to generate a texture id for.
	 * @return The generated texture id.
	 */
	public static Identifier generateTextureLocation(Identifier atlasId) {
		return AtlasRegistryImpl.generateTextureLocation(atlasId);
	}

	private AtlasRegistry() { }
}

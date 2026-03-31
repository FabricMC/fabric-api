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

package net.fabricmc.fabric.api.client.renderer.v1.mesh;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

/**
 * An atlas texture that a {@link QuadView} uses.
 */
public enum QuadAtlas {
	BLOCK(TextureAtlas.LOCATION_BLOCKS, AtlasIds.BLOCKS),
	ITEM(TextureAtlas.LOCATION_ITEMS, AtlasIds.ITEMS);

	private final Identifier textureLocation;
	private final Identifier id;

	QuadAtlas(Identifier textureLocation, Identifier id) {
		this.textureLocation = textureLocation;
		this.id = id;
	}

	/**
	 * {@return the quad atlas for the given atlas texture location or null if no corresponding quad atlas exists}
	 */
	@Nullable
	public static QuadAtlas ofLocation(Identifier atlasTextureLocation) {
		if (atlasTextureLocation.equals(TextureAtlas.LOCATION_BLOCKS)) {
			return BLOCK;
		} else if (atlasTextureLocation.equals(TextureAtlas.LOCATION_ITEMS)) {
			return ITEM;
		} else {
			return null;
		}
	}

	/**
	 * {@return the quad atlas for the given atlas ID or null if no corresponding quad atlas exists}
	 */
	@Nullable
	public static QuadAtlas ofId(Identifier atlasId) {
		if (atlasId.equals(AtlasIds.BLOCKS)) {
			return BLOCK;
		} else if (atlasId.equals(AtlasIds.ITEMS)) {
			return ITEM;
		} else {
			return null;
		}
	}

	/**
	 * @return the atlas texture location (ends with {@code .png})
	 */
	public Identifier getTextureLocation() {
		return textureLocation;
	}

	/**
	 * @return the atlas ID
	 */
	public Identifier getId() {
		return id;
	}
}

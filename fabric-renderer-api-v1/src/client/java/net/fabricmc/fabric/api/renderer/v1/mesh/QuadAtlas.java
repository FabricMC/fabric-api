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

package net.fabricmc.fabric.api.renderer.v1.mesh;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

/**
 * An atlas that a {@link QuadView} uses.
 */
public enum QuadAtlas {
	BLOCK(TextureAtlas.LOCATION_BLOCKS),
	ITEM(TextureAtlas.LOCATION_ITEMS);

	private final Identifier id;

	QuadAtlas(Identifier id) {
		this.id = id;
	}

	public Identifier getId() {
		return id;
	}
}

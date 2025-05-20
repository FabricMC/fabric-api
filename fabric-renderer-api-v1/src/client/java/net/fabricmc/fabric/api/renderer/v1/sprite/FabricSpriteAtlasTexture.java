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

package net.fabricmc.fabric.api.renderer.v1.sprite;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;

/**
 * Note: This interface is automatically implemented on {@link SpriteAtlasTexture} via Mixin and interface injection.
 */
public interface FabricSpriteAtlasTexture {
	/**
	 * Retrieves the sprite finder for this atlas. The returned instance is only valid until the next call to
	 * {@link SpriteAtlasTexture#upload(SpriteLoader.StitchResult)}, and thus should not be persisted across resource
	 * reloads.
	 *
	 * <p><b>This method should not be used during a resource reload</b> as this atlas will only be populated with new
	 * sprites towards the end of the resource reload. In this case, use
	 * {@link FabricErrorCollectingSpriteGetter#spriteFinder(Identifier)}, {@link FabricStitchResult#spriteFinder()},
	 * or {@link FabricAtlasPreparation#spriteFinder()} instead.
	 *
	 * @return the sprite finder for this atlas
	 */
	default SpriteFinder spriteFinder() {
		throw new UnsupportedOperationException();
	}
}

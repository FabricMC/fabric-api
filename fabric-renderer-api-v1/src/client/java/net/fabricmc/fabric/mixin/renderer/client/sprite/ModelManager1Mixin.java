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

package net.fabricmc.fabric.mixin.renderer.client.sprite;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.impl.renderer.MissingSpriteFinderImpl;

@Mixin(targets = "net.minecraft.client.resources.model.ModelManager$1")
abstract class ModelManager1Mixin implements SpriteGetter {
	@Shadow
	@Final
	private TextureAtlasSprite blockMissing;
	@Shadow
	@Final
	SpriteLoader.Preparations val$blockAtlas;
	@Shadow
	@Final
	SpriteLoader.Preparations val$itemAtlas;

	@Unique
	@Nullable
	private volatile MissingSpriteFinderImpl missingSpriteFinder;

	@Override
	public SpriteFinder spriteFinder(Identifier atlasTextureId) {
		if (atlasTextureId.equals(TextureAtlas.LOCATION_BLOCKS)) {
			return val$blockAtlas.spriteFinder();
		} else if (atlasTextureId.equals(TextureAtlas.LOCATION_ITEMS)) {
			return val$itemAtlas.spriteFinder();
		}

		MissingSpriteFinderImpl result = missingSpriteFinder;

		if (result == null) {
			synchronized (this) {
				result = missingSpriteFinder;

				if (result == null) {
					missingSpriteFinder = result = new MissingSpriteFinderImpl(blockMissing);
				}
			}
		}

		return result;
	}
}

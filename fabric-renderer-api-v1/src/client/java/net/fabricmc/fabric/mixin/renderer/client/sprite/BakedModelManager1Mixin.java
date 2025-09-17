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

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.impl.renderer.MissingSpriteFinderImpl;

@Mixin(targets = "net/minecraft/client/render/model/BakedModelManager$1")
abstract class BakedModelManager1Mixin implements ErrorCollectingSpriteGetter {
	@Shadow
	@Final
	private Sprite missingSprite;
	@Shadow
	@Final
	SpriteLoader.StitchResult field_61871;

	@Unique
	@Nullable
	private volatile MissingSpriteFinderImpl missingSpriteFinder;

	@Override
	public SpriteFinder spriteFinder(Identifier atlasId) {
		if (atlasId.equals(Atlases.BLOCKS)) {
			return field_61871.spriteFinder();
		}

		MissingSpriteFinderImpl result = missingSpriteFinder;

		if (result == null) {
			synchronized (this) {
				result = missingSpriteFinder;

				if (result == null) {
					missingSpriteFinder = result = new MissingSpriteFinderImpl(missingSprite);
				}
			}
		}

		return result;
	}
}

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

package net.fabricmc.fabric.mixin.resource.loader.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;

@Mixin({
		/* public */
		SoundManager.class, BakedModelManager.class, LanguageManager.class, TextureManager.class,
		/* private */
		WorldRenderer.class, BlockRenderManager.class, ItemRenderer.class
})
public abstract class KeyedResourceReloadListenerClientMixin implements IdentifiableResourceReloadListener {
	@Unique
	private Identifier id;
	@Unique
	private Collection<Identifier> dependencies;

	@Override
	public Identifier getFabricId() {
		if (this.id == null) {
			Object self = this;

			if (self instanceof SoundManager) {
				this.id = ResourceReloadListenerKeys.SOUNDS;
			} else if (self instanceof BakedModelManager) {
				this.id = ResourceReloadListenerKeys.MODELS;
			} else if (self instanceof LanguageManager) {
				this.id = ResourceReloadListenerKeys.LANGUAGES;
			} else if (self instanceof TextureManager) {
				this.id = ResourceReloadListenerKeys.TEXTURES;
			} else {
				this.id = Identifier.ofVanilla("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.id;
	}

	@Override
	@SuppressWarnings({"ConstantConditions"})
	public Collection<Identifier> getFabricDependencies() {
		if (this.dependencies == null) {
			Object self = this;

			if (self instanceof BakedModelManager || self instanceof WorldRenderer) {
				this.dependencies = Collections.singletonList(ResourceReloadListenerKeys.TEXTURES);
			} else if (self instanceof ItemRenderer || self instanceof BlockRenderManager) {
				this.dependencies = Collections.singletonList(ResourceReloadListenerKeys.MODELS);
			} else {
				this.dependencies = Collections.emptyList();
			}
		}

		return this.dependencies;
	}
}

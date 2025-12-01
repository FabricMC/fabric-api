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

package net.fabricmc.fabric.mixin.resource.client;

import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.PeriodicNotificationManager;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DryFoliageColorReloadListener;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.WaypointStyleManager;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.impl.resource.FabricResourceReloader;

@Mixin({
		/* public */
		AtlasManager.class,
		ModelManager.class,
		BlockEntityRenderDispatcher.class,
		BlockRenderDispatcher.class,
		CloudRenderer.class,
		EquipmentAssetManager.class,
		EntityRenderDispatcher.class,
		DryFoliageColorReloadListener.class,
		FoliageColorReloadListener.class,
		FontManager.class,
		GrassColorReloadListener.class,
		LanguageManager.class,
		ParticleResources.class,
		ShaderManager.class,
		SplashManager.class,
		SoundManager.class,
		TextureManager.class,
		WaypointStyleManager.class,
		/* private */
		LevelRenderer.class, GpuWarnlistManager.class, PeriodicNotificationManager.class
})
public abstract class KeyedClientResourceReloadListenerMixin implements FabricResourceReloader {
	@Unique
	private Identifier fabric$id;

	@Override
	public Identifier fabric$getId() {
		if (this.fabric$id == null) {
			Object self = this;

			if (self instanceof AtlasManager) {
				this.fabric$id = ResourceReloaderKeys.Client.ATLAS;
			} else if (self instanceof ModelManager) {
				this.fabric$id = ResourceReloaderKeys.Client.MODELS;
			} else if (self instanceof BlockEntityRenderDispatcher) {
				this.fabric$id = ResourceReloaderKeys.Client.BLOCK_ENTITY_RENDERERS;
			} else if (self instanceof BlockRenderDispatcher) {
				this.fabric$id = ResourceReloaderKeys.Client.BLOCK_RENDER_MANAGER;
			} else if (self instanceof CloudRenderer) {
				this.fabric$id = ResourceReloaderKeys.Client.CLOUD_CELLS;
			} else if (self instanceof DryFoliageColorReloadListener) {
				this.fabric$id = ResourceReloaderKeys.Client.DRY_FOLIAGE_COLORMAP;
			} else if (self instanceof EquipmentAssetManager) {
				this.fabric$id = ResourceReloaderKeys.Client.EQUIPMENT_MODELS;
			} else if (self instanceof EntityRenderDispatcher) {
				this.fabric$id = ResourceReloaderKeys.Client.ENTITY_RENDERERS;
			} else if (self instanceof FontManager) {
				this.fabric$id = ResourceReloaderKeys.Client.FONTS;
			} else if (self instanceof FoliageColorReloadListener) {
				this.fabric$id = ResourceReloaderKeys.Client.FOLIAGE_COLORMAP;
			} else if (self instanceof GrassColorReloadListener) {
				this.fabric$id = ResourceReloaderKeys.Client.GRASS_COLORMAP;
			} else if (self instanceof LanguageManager) {
				this.fabric$id = ResourceReloaderKeys.Client.LANGUAGES;
			} else if (self instanceof ParticleResources) {
				this.fabric$id = ResourceReloaderKeys.Client.PARTICLES;
			} else if (self instanceof ShaderManager) {
				this.fabric$id = ResourceReloaderKeys.Client.SHADERS;
			} else if (self instanceof SplashManager) {
				this.fabric$id = ResourceReloaderKeys.Client.SPLASH_TEXTS;
			} else if (self instanceof SoundManager) {
				this.fabric$id = ResourceReloaderKeys.Client.SOUNDS;
			} else if (self instanceof TextureManager) {
				this.fabric$id = ResourceReloaderKeys.Client.TEXTURES;
			} else if (self instanceof WaypointStyleManager) {
				this.fabric$id = ResourceReloaderKeys.Client.WAYPOINT_STYLE_ASSETS;
			} else {
				this.fabric$id = Identifier.withDefaultNamespace("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.fabric$id;
	}
}

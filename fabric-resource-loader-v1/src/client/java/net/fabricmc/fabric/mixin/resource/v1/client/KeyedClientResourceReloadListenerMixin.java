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

package net.fabricmc.fabric.mixin.resource.v1.client;

import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.font.FontManager;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.particle.ParticleSpriteManager;
import net.minecraft.client.render.CloudRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.resource.DryFoliageColormapResourceSupplier;
import net.minecraft.client.resource.FoliageColormapResourceSupplier;
import net.minecraft.client.resource.GrassColormapResourceSupplier;
import net.minecraft.client.resource.PeriodicNotificationManager;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.resource.VideoWarningManager;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.resource.waypoint.WaypointStyleAssetManager;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.impl.resource.v1.FabricResourceReloader;

@Mixin({
		/* public */
		AtlasManager.class,
		BakedModelManager.class,
		BlockEntityRenderManager.class,
		BlockRenderManager.class,
		CloudRenderer.class,
		EquipmentModelLoader.class,
		EntityRenderManager.class,
		DryFoliageColormapResourceSupplier.class,
		FoliageColormapResourceSupplier.class,
		FontManager.class,
		GrassColormapResourceSupplier.class,
		LanguageManager.class,
		ParticleSpriteManager.class,
		ShaderLoader.class,
		SplashTextResourceSupplier.class,
		SoundManager.class,
		TextureManager.class,
		WaypointStyleAssetManager.class,
		/* private */
		WorldRenderer.class, VideoWarningManager.class, PeriodicNotificationManager.class
})
public abstract class KeyedClientResourceReloadListenerMixin implements FabricResourceReloader {
	@Unique
	private Identifier id;

	@Override
	public Identifier fabric$getId() {
		if (this.id == null) {
			Object self = this;

			if (self instanceof AtlasManager) {
				this.id = ResourceReloaderKeys.Client.ATLAS;
			} else if (self instanceof BakedModelManager) {
				this.id = ResourceReloaderKeys.Client.MODELS;
			} else if (self instanceof BlockEntityRenderManager) {
				this.id = ResourceReloaderKeys.Client.BLOCK_ENTITY_RENDERERS;
			} else if (self instanceof BlockRenderManager) {
				this.id = ResourceReloaderKeys.Client.BLOCK_RENDER_MANAGER;
			} else if (self instanceof CloudRenderer) {
				this.id = ResourceReloaderKeys.Client.CLOUD_CELLS;
			} else if (self instanceof DryFoliageColormapResourceSupplier) {
				this.id = ResourceReloaderKeys.Client.DRY_FOLIAGE_COLORMAP;
			} else if (self instanceof EquipmentModelLoader) {
				this.id = ResourceReloaderKeys.Client.EQUIPMENT_MODELS;
			} else if (self instanceof EntityRenderManager) {
				this.id = ResourceReloaderKeys.Client.ENTITY_RENDERERS;
			} else if (self instanceof FontManager) {
				this.id = ResourceReloaderKeys.Client.FONTS;
			} else if (self instanceof FoliageColormapResourceSupplier) {
				this.id = ResourceReloaderKeys.Client.FOLIAGE_COLORMAP;
			} else if (self instanceof GrassColormapResourceSupplier) {
				this.id = ResourceReloaderKeys.Client.GRASS_COLORMAP;
			} else if (self instanceof LanguageManager) {
				this.id = ResourceReloaderKeys.Client.LANGUAGES;
			} else if (self instanceof ParticleSpriteManager) {
				this.id = ResourceReloaderKeys.Client.PARTICLES;
			} else if (self instanceof ShaderLoader) {
				this.id = ResourceReloaderKeys.Client.SHADERS;
			} else if (self instanceof SplashTextResourceSupplier) {
				this.id = ResourceReloaderKeys.Client.SPLASH_TEXTS;
			} else if (self instanceof SoundManager) {
				this.id = ResourceReloaderKeys.Client.SOUNDS;
			} else if (self instanceof TextureManager) {
				this.id = ResourceReloaderKeys.Client.TEXTURES;
			} else if (self instanceof WaypointStyleAssetManager) {
				this.id = ResourceReloaderKeys.Client.WAYPOINT_STYLE_ASSETS;
			} else {
				this.id = Identifier.ofVanilla("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.id;
	}
}

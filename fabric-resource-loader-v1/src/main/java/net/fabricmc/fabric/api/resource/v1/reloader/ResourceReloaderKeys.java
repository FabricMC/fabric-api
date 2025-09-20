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

package net.fabricmc.fabric.api.resource.v1.reloader;

import net.minecraft.util.Identifier;

/**
 * This class contains default keys for various Minecraft resource reloaders.
 *
 * @see net.minecraft.resource.ResourceReloader
 */
public final class ResourceReloaderKeys {
	/**
	 * Represents the application phase before Vanilla resource reloaders are invoked.
	 *
	 * <p>No resource reloaders are assigned to this identifier.
	 *
	 * @see net.fabricmc.fabric.api.resource.v1.ResourceLoader#addReloaderOrdering(Identifier, Identifier)
	 */
	public static final Identifier BEFORE_VANILLA = Identifier.of("fabric", "before_vanilla");
	/**
	 * Represents the application phase after Vanilla resource reloaders are invoked.
	 *
	 * <p>No resource reloaders are assigned to this identifier.
	 *
	 * @see net.fabricmc.fabric.api.resource.v1.ResourceLoader#addReloaderOrdering(Identifier, Identifier)
	 */
	public static final Identifier AFTER_VANILLA = Identifier.of("fabric", "after_vanilla");

	private ResourceReloaderKeys() { }

	/**
	 * Keys for various client resource reloaders.
	 */
	public static final class Client {
		public static final Identifier BLOCK_ENTITY_RENDERERS = Identifier.ofVanilla("block_entity_renderers");
		public static final Identifier BLOCK_RENDER_MANAGER = Identifier.ofVanilla("block_render_manager");
		public static final Identifier CLOUD_CELLS = Identifier.ofVanilla("cloud_cells");
		public static final Identifier EQUIPMENT_MODELS = Identifier.ofVanilla("equipment_models");
		public static final Identifier ENTITY_RENDERERS = Identifier.ofVanilla("entity_renderers");
		public static final Identifier DRY_FOLIAGE_COLORMAP = Identifier.ofVanilla("dry_foliage_colormap");
		public static final Identifier FOLIAGE_COLORMAP = Identifier.ofVanilla("foliage_colormap");
		public static final Identifier FONTS = Identifier.ofVanilla("fonts");
		public static final Identifier GRASS_COLORMAP = Identifier.ofVanilla("grass_colormap");
		public static final Identifier ATLAS = Identifier.ofVanilla("atlas");
		public static final Identifier LANGUAGES = Identifier.ofVanilla("languages");
		public static final Identifier MODELS = Identifier.ofVanilla("models");
		public static final Identifier PARTICLES = Identifier.ofVanilla("particles");
		public static final Identifier SHADERS = Identifier.ofVanilla("shaders");
		public static final Identifier SOUNDS = Identifier.ofVanilla("sounds");
		public static final Identifier SPLASH_TEXTS = Identifier.ofVanilla("splash_texts");
		public static final Identifier TEXTURES = Identifier.ofVanilla("textures");
		public static final Identifier WAYPOINT_STYLE_ASSETS = Identifier.ofVanilla("waypoint_style_assets");

		private Client() {
		}
	}

	/**
	 * Keys for various server resource reloaders.
	 */
	public static final class Server {
		public static final Identifier ADVANCEMENTS = Identifier.ofVanilla("advancements");
		public static final Identifier FUNCTIONS = Identifier.ofVanilla("functions");
		public static final Identifier RECIPES = Identifier.ofVanilla("recipes");

		private Server() {
		}
	}
}

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

import net.minecraft.resources.Identifier;

/**
 * This class contains default keys for various Minecraft resource reloaders.
 *
 * @see net.minecraft.server.packs.resources.PreparableReloadListener
 */
public final class ResourceReloaderKeys {
	/**
	 * Represents the application phase before Vanilla resource reloaders are invoked.
	 *
	 * <p>No resource reloaders are assigned to this identifier.
	 *
	 * @see net.fabricmc.fabric.api.resource.v1.ResourceLoader#addReloaderOrdering(Identifier, Identifier)
	 */
	public static final Identifier BEFORE_VANILLA = Identifier.fromNamespaceAndPath("fabric", "before_vanilla");
	/**
	 * Represents the application phase after Vanilla resource reloaders are invoked.
	 *
	 * <p>No resource reloaders are assigned to this identifier.
	 *
	 * @see net.fabricmc.fabric.api.resource.v1.ResourceLoader#addReloaderOrdering(Identifier, Identifier)
	 */
	public static final Identifier AFTER_VANILLA = Identifier.fromNamespaceAndPath("fabric", "after_vanilla");

	private ResourceReloaderKeys() { }

	/**
	 * Keys for various client resource reloaders.
	 */
	public static final class Client {
		public static final Identifier BLOCK_ENTITY_RENDERERS = Identifier.withDefaultNamespace("block_entity_renderers");
		public static final Identifier BLOCK_RENDER_MANAGER = Identifier.withDefaultNamespace("block_render_manager");
		public static final Identifier CLOUD_CELLS = Identifier.withDefaultNamespace("cloud_cells");
		public static final Identifier EQUIPMENT_MODELS = Identifier.withDefaultNamespace("equipment_models");
		public static final Identifier ENTITY_RENDERERS = Identifier.withDefaultNamespace("entity_renderers");
		public static final Identifier DRY_FOLIAGE_COLORMAP = Identifier.withDefaultNamespace("dry_foliage_colormap");
		public static final Identifier FOLIAGE_COLORMAP = Identifier.withDefaultNamespace("foliage_colormap");
		public static final Identifier FONTS = Identifier.withDefaultNamespace("fonts");
		public static final Identifier GRASS_COLORMAP = Identifier.withDefaultNamespace("grass_colormap");
		public static final Identifier ATLAS = Identifier.withDefaultNamespace("atlas");
		public static final Identifier LANGUAGES = Identifier.withDefaultNamespace("languages");
		public static final Identifier MODELS = Identifier.withDefaultNamespace("models");
		public static final Identifier PARTICLES = Identifier.withDefaultNamespace("particles");
		public static final Identifier SHADERS = Identifier.withDefaultNamespace("shaders");
		public static final Identifier SOUNDS = Identifier.withDefaultNamespace("sounds");
		public static final Identifier SPLASH_TEXTS = Identifier.withDefaultNamespace("splash_texts");
		public static final Identifier TEXTURES = Identifier.withDefaultNamespace("textures");
		public static final Identifier WAYPOINT_STYLE_ASSETS = Identifier.withDefaultNamespace("waypoint_style_assets");

		private Client() {
		}
	}

	/**
	 * Keys for various server resource reloaders.
	 */
	public static final class Server {
		public static final Identifier ADVANCEMENTS = Identifier.withDefaultNamespace("advancements");
		public static final Identifier FUNCTIONS = Identifier.withDefaultNamespace("functions");
		public static final Identifier RECIPES = Identifier.withDefaultNamespace("recipes");

		private Server() {
		}
	}
}

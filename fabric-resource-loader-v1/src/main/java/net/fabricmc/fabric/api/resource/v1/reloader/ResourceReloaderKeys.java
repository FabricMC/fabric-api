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
 * This class contains default keys for various Minecraft reload listener.
 *
 * @see net.minecraft.server.packs.resources.PreparableReloadListener
 */
public final class ResourceReloaderKeys {
	/**
	 * Represents the application phase before Vanilla reload listeners are invoked.
	 *
	 * <p>No reload listeners are assigned to this identifier.
	 *
	 * @see net.fabricmc.fabric.api.resource.v1.ResourceLoader#addListenerOrdering(Identifier, Identifier)
	 */
	public static final Identifier BEFORE_VANILLA = Identifier.fromNamespaceAndPath("fabric", "before_vanilla");
	/**
	 * Represents the application phase after Vanilla reload listeners are invoked.
	 *
	 * <p>No reload listeners are assigned to this identifier.
	 *
	 * @see net.fabricmc.fabric.api.resource.v1.ResourceLoader#addListenerOrdering(Identifier, Identifier)
	 */
	public static final Identifier AFTER_VANILLA = Identifier.fromNamespaceAndPath("fabric", "after_vanilla");

	private ResourceReloaderKeys() { }

	/**
	 * Keys for various client reload listeners.
	 */
	public static final class Client {
		public static final Identifier BLOCK_ENTITY_RENDER_DISPATCHER = Identifier.withDefaultNamespace("block_entity_render_dispatcher");
		public static final Identifier CLOUD_RENDERER = Identifier.withDefaultNamespace("cloud_renderer");
		public static final Identifier EQUIPMENT_ASSETS = Identifier.withDefaultNamespace("equipment_assets");
		public static final Identifier ENTITY_RENDER_DISPATCHER = Identifier.withDefaultNamespace("entity_render_dispatcher");
		public static final Identifier DRY_FOLIAGE_COLOR = Identifier.withDefaultNamespace("dry_foliage_color");
		public static final Identifier FOLIAGE_COLOR = Identifier.withDefaultNamespace("foliage_color");
		public static final Identifier FONTS = Identifier.withDefaultNamespace("fonts");
		public static final Identifier GRASS_COLOR = Identifier.withDefaultNamespace("grass_color");
		public static final Identifier ATLAS = Identifier.withDefaultNamespace("atlas");
		public static final Identifier LANGUAGES = Identifier.withDefaultNamespace("languages");
		public static final Identifier MODELS = Identifier.withDefaultNamespace("models");
		public static final Identifier PARTICLES = Identifier.withDefaultNamespace("particles");
		public static final Identifier SHADERS = Identifier.withDefaultNamespace("shaders");
		public static final Identifier SOUNDS = Identifier.withDefaultNamespace("sounds");
		public static final Identifier SPLASH_TEXTS = Identifier.withDefaultNamespace("splash_texts");
		public static final Identifier TEXTURES = Identifier.withDefaultNamespace("textures");
		public static final Identifier WAYPOINT_STYLE = Identifier.withDefaultNamespace("waypoint_style");

		private Client() {
		}
	}

	/**
	 * Keys for various server reload listeners.
	 */
	public static final class Server {
		public static final Identifier ADVANCEMENTS = Identifier.withDefaultNamespace("advancements");
		public static final Identifier FUNCTIONS = Identifier.withDefaultNamespace("functions");
		public static final Identifier RECIPES = Identifier.withDefaultNamespace("recipes");

		private Server() {
		}
	}
}

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

package net.fabricmc.fabric.api.renderer.v1;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.impl.renderer.RendererManager;

/**
 * Interface for rendering plug-ins that provide enhanced capabilities
 * for model lighting, buffering and rendering. Such plug-ins implement the
 * enhanced model rendering interfaces specified by the Fabric API.
 */
public interface Renderer {
	/**
	 * Access to the current {@link Renderer} for creating and retrieving mesh builders
	 * and materials.
	 */
	static Renderer get() {
		return RendererManager.getRenderer();
	}

	/**
	 * Rendering extension mods must implement {@link Renderer} and
	 * call this method during initialization.
	 *
	 * <p>Only one {@link Renderer} plug-in can be active in any game instance.
	 * If a second mod attempts to register, this method will throw an UnsupportedOperationException.
	 */
	static void register(Renderer renderer) {
		RendererManager.registerRenderer(renderer);
	}

	/**
	 * Obtain a new {@link MutableMesh} instance to build optimized meshes and create baked models
	 * with enhanced features.
	 *
	 * <p>Renderer does not retain a reference to returned instances, so they should be re-used
	 * when possible to avoid memory allocation overhead.
	 */
	MutableMesh mutableMesh();

	/**
	 * Obtain a new {@link MaterialFinder} instance to retrieve standard {@link RenderMaterial}
	 * instances.
	 *
	 * <p>Renderer does not retain a reference to returned instances, so they should be re-used for
	 * multiple materials when possible to avoid memory allocation overhead.
	 */
	MaterialFinder materialFinder();

	/**
	 * Return a material previously registered via {@link #registerMaterial(Identifier, RenderMaterial)}.
	 * Will return null if no material was found matching the given identifier.
	 */
	@Nullable
	RenderMaterial materialById(Identifier id);

	/**
	 * Register a material for re-use by other mods or models within a mod.
	 * The registry does not persist registrations - mods must create and register
	 * all materials at game initialization.
	 *
	 * <p>Returns false if a material with the given identifier is already present,
	 * leaving the existing material intact.
	 */
	boolean registerMaterial(Identifier id, RenderMaterial material);
}

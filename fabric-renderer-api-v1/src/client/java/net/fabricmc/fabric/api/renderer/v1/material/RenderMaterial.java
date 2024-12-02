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

package net.fabricmc.fabric.api.renderer.v1.material;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;

/**
 * All model quads have an associated render material governing
 * how the quad will be rendered.
 *
 * <p>A material instance is always immutable and thread-safe.  References to a material
 * remain valid until the end of the current game session.
 *
 * <p>Materials can be registered and shared between mods using {@link Renderer#registerMaterial(Identifier, RenderMaterial)}.
 * The registering mod is responsible for creating each registered material at startup.
 *
 * <p>Materials are not required to know their registration identity, and two materials
 * with the same attributes may or may not satisfy equality and identity tests. Model
 * implementations should never attempt to analyze materials or implement control logic based on them.
 * They are only tokens for communicating quad attributes to the ModelRenderer.
 *
 * <p>There are three classes of materials...
 *
 * <p><b>STANDARD MATERIALS</b>
 *
 * <p>Standard materials have "normal" rendering with control over lighting,
 * color, and texture blending. In the default renderer, "normal" rendering
 * emulates unmodified Minecraft. Other renderers may offer a different aesthetic.
 *
 * <p>The number of standard materials is finite, but not necessarily small.
 * To find a standard material, use {@link Renderer#materialFinder()}.
 *
 * <p>All renderer implementations should support standard materials.
 *
 * <p><b>SPECIAL MATERIALS</b>
 *
 * <p>Special materials are implemented directly by the Renderer implementation, typically
 * with the aim of providing advanced/extended features. Such materials may offer additional
 * vertex attributes via extensions to {@link MutableMesh} and {@link MutableQuadView}.
 *
 * <p>Special materials can be obtained using {@link Renderer#materialById(Identifier)}
 * with a known identifier. Renderers may provide other means of access. Popular
 * special materials could be implemented by multiple renderers, however there is
 * no requirement that special materials be cross-compatible.
 */
public interface RenderMaterial extends MaterialView {
	/**
	 * This will be identical to the material that would be obtained by calling {@link MaterialFinder#find()} on a new,
	 * unaltered, {@link MaterialFinder} instance. It is defined here for clarity and convenience.
	 *
	 * <p>Quads using this material use {@link RenderLayers#getBlockLayer(BlockState)} of the associated block to
	 * determine texture blending, honor block color index, are non-emissive, and apply both diffuse and ambient
	 * occlusion shading to vertex colors.
	 *
	 * <p>All standard, non-fluid baked models are rendered using this material.
	 */
	Identifier STANDARD_ID = Identifier.of("fabric", "standard");
}

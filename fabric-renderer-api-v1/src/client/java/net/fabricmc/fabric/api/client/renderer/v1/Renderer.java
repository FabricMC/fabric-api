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

package net.fabricmc.fabric.api.client.renderer.v1;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.fabricmc.fabric.impl.client.renderer.RendererManager;

/**
 * Interface for rendering plug-ins that provide enhanced capabilities
 * for model lighting, buffering and rendering. Such plug-ins implement the
 * enhanced model rendering interfaces specified by the Fabric API.
 *
 * <p>Renderers must ensure that terrain buffering supports {@link BlockStateModel#emitQuads}, if they introduce an
 * alternate path for it. In vanilla, this happens in {@link SectionCompiler}, which is automatically patched to use
 * {@link #altModelBlockRenderer(boolean, boolean, BlockColors)}.
 *
 * <p>All places in vanilla code that invoke {@link BlockStateModel#collectParts(RandomSource, List)} or
 * {@link ModelBlockRenderer#tesselateBlock(BlockQuadOutput, float, float, float, BlockAndTintGetter, BlockPos, BlockState, BlockStateModel, long)}
 * are, where appropriate, patched automatically to invoke {@link BlockStateModel#emitQuads} or
 * {@link AltModelBlockRenderer#tesselateBlock(QuadEmitter, float, float, float, BlockAndTintGetter, BlockPos, BlockState, BlockStateModel, long)},
 * respectively, instead.
 *
 * <p>Renderers must patch {@link ItemFeatureRenderer} to support
 * {@link FabricSubmitNodeCollection#getExtendedItemSubmits()}. {@link BlockFeatureRenderer} is automatically patched
 * to support {@link FabricSubmitNodeCollection#getExtendedBlockModelSubmits()} and {@link BlockStateModel#emitQuads}.
 */
public interface Renderer {
	/**
	 * Access to the current {@link Renderer} for creating and retrieving mesh builders
	 * and materials.
	 *
	 * <p><b>Warning:</b> do not call this method before {@link ModInitializer} has been invoked. Doing
	 * so will likely crash.
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
	 * Obtain a new {@link QuadEmitter} instance that invokes the given consumer on
	 * {@link QuadEmitter#emit()}, after transforms are applied.
	 *
	 * @param consumer logic performed when the quad is emitted.
	 */
	QuadEmitter quadEmitter(Consumer<? super MutableQuadView> consumer);

	/**
	 * Obtain a new {@link MutableMesh} instance to build optimized meshes and create baked models
	 * with enhanced features.
	 *
	 * <p>Renderer does not retain a reference to returned instances, so they should be re-used
	 * when possible to avoid memory allocation overhead.
	 */
	MutableMesh mutableMesh();

	/**
	 * Obtain a new {@link AltModelBlockRenderer} instance to tesselate blocks with
	 * {@linkplain QuadEmitter modded quads}. Prefer using this over the vanilla
	 * {@link ModelBlockRenderer} to correctly tesselate modded models.
	 */
	AltModelBlockRenderer altModelBlockRenderer(boolean ambientOcclusion, boolean cull, BlockColors blockColors);
}

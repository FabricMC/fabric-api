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

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockModelPart;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockRenderManager;
import net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.impl.renderer.RendererManager;
import net.fabricmc.fabric.impl.renderer.VanillaBlockModelPartEncoder;

/**
 * Interface for rendering plug-ins that provide enhanced capabilities
 * for model lighting, buffering and rendering. Such plug-ins implement the
 * enhanced model rendering interfaces specified by the Fabric API.
 *
 * <p>Renderers must ensure that terrain buffering supports {@link BlockStateModel#emitQuads}, which happens in
 * {@link SectionBuilder} in vanilla; this code is not patched automatically. Renderers must also ensure that the
 * following vanilla methods support {@link BlockStateModel#emitQuads}; these methods are not patched automatically.
 *
 * <ul><li>{@link BlockModelRenderer#render(MatrixStack.Entry, VertexConsumer, BlockStateModel, float, float, float, int, int)}
 *
 * <li>{@link BlockRenderManager#renderDamage(BlockState, BlockPos, BlockRenderView, MatrixStack, VertexConsumer)}
 *
 * <li>{@link BlockRenderManager#renderBlockAsEntity(BlockState, MatrixStack, VertexConsumerProvider, int, int)}</ul>
 *
 * <p>All other places in vanilla code that invoke {@link BlockStateModel#addParts(Random, List)},
 * {@link BlockStateModel#getParts(Random)}, or
 * {@link BlockModelRenderer#render(MatrixStack.Entry, VertexConsumer, BlockStateModel, float, float, float, int, int)}
 * are, where appropriate, patched automatically to invoke the corresponding method above or the corresponding method in
 * {@link FabricBlockModelRenderer} or {@link FabricBlockRenderManager}.
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

	/**
	 * @see FabricBlockModelRenderer#render(BlockRenderView, BlockStateModel, BlockState, BlockPos, MatrixStack, VertexConsumerProvider, boolean, long, int)
	 */
	@ApiStatus.OverrideOnly
	void render(BlockModelRenderer modelRenderer, BlockRenderView blockView, BlockStateModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumerProvider vertexConsumers, boolean cull, long seed, int overlay);

	/**
	 * @see FabricBlockModelRenderer#render(MatrixStack.Entry, VertexConsumerProvider, BlockStateModel, float, float, float, int, int, BlockRenderView, BlockPos, BlockState)
	 */
	@ApiStatus.OverrideOnly
	void render(MatrixStack.Entry matrices, VertexConsumerProvider vertexConsumers, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockRenderView blockView, BlockPos pos, BlockState state);

	/**
	 * @see FabricBlockRenderManager#renderBlockAsEntity(BlockState, MatrixStack, VertexConsumerProvider, int, int, BlockRenderView, BlockPos)
	 */
	@ApiStatus.OverrideOnly
	void renderBlockAsEntity(BlockRenderManager renderManager, BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BlockRenderView blockView, BlockPos pos);

	/**
	 * @see FabricBlockModelPart#emitQuads(QuadEmitter, Predicate)
	 */
	@Deprecated(forRemoval = true)
	@ApiStatus.OverrideOnly
	default void emitBlockModelPartQuads(BlockModelPart modelPart, QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
		VanillaBlockModelPartEncoder.emitQuads(modelPart, emitter, cullTest);
	}

	/**
	 * @see FabricLayerRenderState#emitter()
	 */
	@ApiStatus.OverrideOnly
	QuadEmitter getLayerRenderStateEmitter(ItemRenderState.LayerRenderState layer);
}

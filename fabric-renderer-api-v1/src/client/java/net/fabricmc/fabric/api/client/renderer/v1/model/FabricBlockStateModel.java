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

package net.fabricmc.fabric.api.client.renderer.v1.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.multipart.MultiPartModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;

/**
 * Interface for baked block state models that output geometry with enhanced rendering features.
 * Can also be used to generate or customize geometry output based on level state.
 *
 * <p>Implementors should have a look at {@link ModelHelper} as it contains many useful functions.
 *
 * <p>Note: This interface is automatically implemented on {@link BlockStateModel} via Mixin and interface injection.
 */
public interface FabricBlockStateModel {
	/**
	 * Produces this model's geometry. <b>This method must be called instead of
	 * {@link BlockStateModel#collectParts(RandomSource, List)}; the vanilla method
	 * should be considered deprecated as it may not produce accurate results.</b> However, it is acceptable for a
	 * custom model to only implement the vanilla method as the default implementation of this method will delegate to
	 * it.
	 *
	 * <p>Like {@link BlockStateModel#collectParts(RandomSource, List)}, this method may be called outside of chunk rebuilds. For
	 * example, some entities and block entities render blocks. In some such cases, the provided position may be the
	 * <em>nearest</em> position and not actual position. In others, the provided level may be
	 * {@linkplain BlockAndTintGetter#EMPTY empty}.
	 *
	 * <p>If multiple independent subtasks use the provided random, it is recommended that implementations
	 * {@linkplain RandomSource#setSeed(long) reseed} the random using a predetermined value before invoking each subtask, so
	 * that one subtask's operations do not affect subsequent subtasks. For example, if a model collects geometry from
	 * multiple submodels, each submodel is considered a subtask and thus the random should be reseeded before
	 * collecting geometry from each submodel. See {@link MultiPartModel#collectParts(RandomSource, List)} for an
	 * example implementation of this.
	 *
	 * <p>Implementations should rely on pre-baked meshes as much as possible and keep dynamic transformations to a
	 * minimum for performance.
	 *
	 * <p>Implementations should generally also override {@link #createGeometryKey}.
	 *
	 * @param emitter Accepts model output.
	 * @param level Access to level state.
	 * @param pos Position of block for model being rendered.
	 * @param state Block state whose model was queried for geometry. <b>This is not guaranteed to be the
	 *              state corresponding to {@code this} model!</b>
	 * @param random Random object seeded per vanilla conventions. Do not cache or retain a reference.
	 * @param cullTest A test that returns {@code true} for faces which will be culled and {@code false} for faces which
	 *                 may or may not be culled. Meant to be used to cull groups of quads or expensive dynamic quads
	 *                 early for performance. Early culled quads will likely not be added the emitter, so callers of
	 *                 this method must account for this. In general, prefer using
	 *                 {@link MutableQuadView#cullFace(Direction)} instead of this test.
	 *
	 * @see #createGeometryKey(BlockAndTintGetter, BlockPos, BlockState, RandomSource)
	 */
	default void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		final boolean cutoutLeaves = Minecraft.getInstance().options.cutoutLeaves().get();
		final boolean forceOpaque = ModelBlockRenderer.forceOpaque(cutoutLeaves, state);

		if (forceOpaque) {
			emitter.pushTransform(quad -> {
				quad.chunkLayer(ChunkSectionLayer.SOLID);
				return true;
			});
		}

		final List<BlockStateModelPart> parts = new ArrayList<>();
		((BlockStateModel) this).collectParts(random, parts);
		final int partCount = parts.size();

		for (int i = 0; i < partCount; i++) {
			parts.get(i).emitQuads(emitter, cullTest);
		}

		if (forceOpaque) {
			emitter.popTransform();
		}
	}

	/**
	 * Creates a geometry key using the given context. A geometry key represents the exact geometry output from
	 * {@link #emitQuads} when given the same parameters as this method and a cull test that always returns
	 * {@code false}. Geometry keys are intended to be used in a cache to avoid recomputing expensive transformations
	 * applied to a certain model's geometry.
	 *
	 * <p>The geometry key must implement {@link Object#equals(Object)} and
	 * {@link Object#hashCode()}. The geometry key may be compared to the geometry key of <b>any other model</b>, not
	 * just those produced by this model instance, so care should be taken when selecting the type of the key.
	 * Generally, one class of model will want to make its own record class to use for geometry keys.
	 *
	 * <p>A {@code null} key means that a geometry key does not exist for specifically the given context; a key may exist
	 * for a different context. It is always possible to create a key for any context, but some custom models may choose
	 * not to if doing so is too complex. Vanilla models correctly implement this method, but may return {@code null}
	 * when delegating to a submodel that returns {@code null}.
	 *
	 * @param level The level in which the block exists.
	 * @param pos The position of the block in the level.
	 * @param state The block state whose model was queried for a geometry key. <b>This is not guaranteed to be the
	 *              state corresponding to {@code this} model!</b>
	 * @param random Random object seeded per vanilla conventions.
	 * @return the geometry key, or {@code null} if one does not exist for the given context
	 *
	 * @see #emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)
	 */
	@Nullable
	default Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return null;
	}

	/**
	 * Extension of {@link BlockStateModel#particleMaterial()} that accepts level state. This method will be invoked most
	 * of the time, but the vanilla method may still be invoked when no level context is available.
	 *
	 * <p><b>If your model delegates to other {@link BlockStateModel}s, ensure that it also delegates invocations of
	 * this method to its submodels as appropriate!</b>
	 *
	 * @param level The level in which the block exists.
	 * @param pos The position of the block in the level.
	 * @param state The block state whose model was queried for the particle material. <b>This is not guaranteed to be the
	 *              state corresponding to {@code this} model!</b>
	 * @return the particle material
	 */
	default Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		return ((BlockStateModel) this).particleMaterial();
	}

	/**
	 * Extension of {@link BlockStateModel#materialFlags()} that accepts level state. This method will be invoked most
	 * of the time, but the vanilla method may still be invoked when no level context is available. Alternatively, this
	 * method may not be invoked at all; see the rest of this documentation for more details.
	 *
	 * <p><b>If your model delegates to other {@link BlockStateModel}s, ensure that it also delegates invocations of
	 * this method to its submodels as appropriate!</b>
	 *
	 * <p>This method exists solely for performance of dynamic models. It is acceptable and sometimes desirable for
	 * material flags to be "generalized"; in other words, it is functionally correct for a model to report that it has
	 * translucent or animated geometry even when it does not. It is up to the implementor of the model to decide how
	 * much the model should generalize its flags. This decision is primarily informed by how dynamic the model is and
	 * how often it is expected to be rendered. More generalized flags are cheaper to compute and use less context,
	 * but are less accurate, which can result in performance loss elsewhere in the rendering pipeline. As
	 * {@link BlockStateModel#materialFlags()} does not receive any context, it must return the most generalized flags,
	 * which must be functionally correct in all possible level contexts.
	 *
	 * <p>The result of this method is inherently tied to the result of
	 * {@link #emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)}. It is
	 * required that, for some level context and an arbitrary cull test, applying
	 * {@link ModelHelper#computeMaterialFlags(QuadView)} to every quad output by
	 * {@link #emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)} must produce
	 * flags that are at least as accurate as the result of this method. In other words, code that uses this model may,
	 * for performance reasons, inspect the geometry output and compute the material flags from it instead of calling
	 * this method, and this model must account for that.
	 *
	 * @param level The level in which the block exists.
	 * @param pos The position of the block in the level.
	 * @param state The block state whose model was queried for the particle material. <b>This is not guaranteed to be the
	 *              state corresponding to {@code this} model!</b>
	 * @param random The random object seeded per vanilla conventions.
	 * @return the material flags
	 */
	@BakedQuad.MaterialFlags
	default int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return ((BlockStateModel) this).materialFlags();
	}

	/**
	 * Extension of {@link BlockStateModel#hasMaterialFlag(int)} that accepts level state. This method will be invoked
	 * most of the time, but the vanilla method may still be invoked when no level context is available. Alternatively,
	 * this method may not be invoked at all; see the rest of this documentation for more details.
	 *
	 * <p>If you need to check this model's flags more than once in some context, call
	 * {@link #materialFlags(BlockAndTintGetter, BlockPos, BlockState, RandomSource)} manually instead and perform the
	 * checks manually to avoid recomputing the flags for each check, which can be computationally expensive.
	 *
	 * <p>This method should generally not be overridden.
	 *
	 * @param level The level in which the block exists.
	 * @param pos The position of the block in the level.
	 * @param state The block state whose model was queried for the particle material. <b>This is not guaranteed to be the
	 *              state corresponding to {@code this} model!</b>
	 * @param random The random object seeded per vanilla conventions.
	 * @param flag The flag mask to check against.
	 * @return whether this model has the given material flag
	 *
	 * @see #materialFlags(BlockAndTintGetter, BlockPos, BlockState, RandomSource)
	 */
	default boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, @BakedQuad.MaterialFlags int flag) {
		return (materialFlags(level, pos, state, random) & flag) != 0;
	}
}

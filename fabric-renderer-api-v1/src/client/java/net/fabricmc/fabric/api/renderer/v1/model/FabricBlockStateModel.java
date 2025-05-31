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

package net.fabricmc.fabric.api.renderer.v1.model;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.MultipartBlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.EmptyBlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/**
 * Interface for baked block state models that output geometry with enhanced rendering features.
 * Can also be used to generate or customize geometry output based on world state.
 *
 * <p>Implementors should have a look at {@link ModelHelper} as it contains many useful functions.
 *
 * <p>Note: This interface is automatically implemented on {@link BlockStateModel} via Mixin and interface injection.
 */
public interface FabricBlockStateModel {
	/**
	 * Produces this model's geometry. <b>This method must be called instead of
	 * {@link BlockStateModel#addParts(Random, List)} or {@link BlockStateModel#getParts(Random)}; the vanilla methods
	 * should be considered deprecated as they may not produce accurate results.</b> However, it is acceptable for a
	 * custom model to only implement the vanilla methods as the default implementation of this method will delegate to
	 * one of the vanilla methods.
	 *
	 * <p>Like {@link BlockStateModel#addParts(Random, List)}, this method may be called outside of chunk rebuilds. For
	 * example, some entities and block entities render blocks. In some such cases, the provided position may be the
	 * <em>nearest</em> position and not actual position. In others, the provided world may be
	 * {@linkplain EmptyBlockRenderView#INSTANCE empty}.
	 *
	 * <p>If multiple independent subtasks use the provided random, it is recommended that implementations
	 * {@linkplain Random#setSeed(long) reseed} the random using a predetermined value before invoking each subtask, so
	 * that one subtask's operations do not affect the next subtask. For example, if a model collects geometry from
	 * multiple submodels, each submodel is considered a subtask and thus the random should be reseeded before
	 * collecting geometry from each submodel. See {@link MultipartBlockStateModel#addParts(Random, List)} for an
	 * example implementation of this.
	 *
	 * <p>Implementations should rely on pre-baked meshes as much as possible and keep dynamic transformations to a
	 * minimum for performance.
	 *
	 * <p>Implementations should generally also override {@link #createGeometryKey}.
	 *
	 * @param emitter Accepts model output.
	 * @param blockView Access to world state.
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
	 * @see #createGeometryKey(BlockRenderView, BlockPos, BlockState, Random)
	 */
	default void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
		final List<BlockModelPart> parts = ((BlockStateModel) this).getParts(random);
		final int partCount = parts.size();

		for (int i = 0; i < partCount; i++) {
			parts.get(i).emitQuads(emitter, cullTest);
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
	 * <p>A {@code null} key means that a geometry key does exist for specifically the given context; a key may exist
	 * for a different context. It is always possible to create a key for any context, but some custom models may choose
	 * not to if doing so is too complex. Vanilla models correctly implement this method, but may return {@code null}
	 * when delegating to a submodel that returns {@code null}.
	 *
	 * @param blockView The world in which the block exists.
	 * @param pos The position of the block in the world.
	 * @param state The block state whose model was queried for a geometry key. <b>This is not guaranteed to be the
	 *              state corresponding to {@code this} model!</b>
	 * @param random Random object seeded per vanilla conventions.
	 * @return the geometry key, or {@code null} if one does not exist for the given context
	 *
	 * @see #emitQuads(QuadEmitter, BlockRenderView, BlockPos, BlockState, Random, Predicate)
	 */
	@Nullable
	default Object createGeometryKey(BlockRenderView blockView, BlockPos pos, BlockState state, Random random) {
		return null;
	}

	/**
	 * Extension of {@link BlockStateModel#particleSprite()} that accepts world state. This method will be invoked most
	 * of the time, but the vanilla method may still be invoked when no world context is available.
	 *
	 * <p><b>If your model delegates to other {@link BlockStateModel}s, ensure that it also delegates invocations of
	 * this method to its submodels as appropriate!</b>
	 *
	 * @param blockView The world in which the block exists.
	 * @param pos The position of the block in the world.
	 * @param state The block state whose model was queried for the particle sprite. <b>This is not guaranteed to be the
	 *              state corresponding to {@code this} model!</b>
	 * @return the particle sprite
	 */
	default Sprite particleSprite(BlockRenderView blockView, BlockPos pos, BlockState state) {
		return ((BlockStateModel) this).particleSprite();
	}
}

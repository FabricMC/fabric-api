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

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/// Interface for baked block state models that output geometry with enhanced rendering features.
/// Can also be used to generate or customize geometry output based on level state.
///
/// Implementors should have a look at [ModelHelper] as it contains many useful functions.
///
/// Note: This interface is automatically implemented on [BlockStateModel] via Mixin and interface injection.
public interface FabricBlockStateModel {
	/// Produces this model's geometry. **This method must be called instead of
	/// [BlockStateModel#collectParts(RandomSource, List)] or [BlockStateModel#collectParts(RandomSource)]; the vanilla methods
	/// should be considered deprecated as they may not produce accurate results.** However, it is acceptable for a
	/// custom model to only implement the vanilla methods as the default implementation of this method will delegate to
	/// one of the vanilla methods.
	///
	/// Like [BlockStateModel#collectParts(RandomSource, List)], this method may be called outside of chunk rebuilds. For
	/// example, some entities and block entities render blocks. In some such cases, the provided position may be the
	/// _nearest_ position and not actual position. In others, the provided level may be
	/// [empty][net.minecraft.world.level.EmptyBlockAndTintGetter#INSTANCE].
	///
	/// If multiple independent subtasks use the provided random, it is recommended that implementations
	/// [reseed][RandomSource#setSeed(long)] the random using a predetermined value before invoking each subtask, so
	/// that one subtask's operations do not affect the next subtask. For example, if a model collects geometry from
	/// multiple submodels, each submodel is considered a subtask and thus the random should be reseeded before
	/// collecting geometry from each submodel. See [net.minecraft.client.renderer.block.model.multipart.MultiPartModel#collectParts(RandomSource, List)] for an
	/// example implementation of this.
	///
	/// Implementations should rely on pre-baked meshes as much as possible and keep dynamic transformations to a
	/// minimum for performance.
	///
	/// Implementations should generally also override [#createGeometryKey].
	///
	/// @param emitter Accepts model output.
	/// @param level Access to level state.
	/// @param pos Position of block for model being rendered.
	/// @param state Block state whose model was queried for geometry. **This is not guaranteed to be the
	///              state corresponding to `this` model!**
	/// @param random Random object seeded per vanilla conventions. Do not cache or retain a reference.
	/// @param cullTest A test that returns `true` for faces which will be culled and `false` for faces which
	///                 may or may not be culled. Meant to be used to cull groups of quads or expensive dynamic quads
	///                 early for performance. Early culled quads will likely not be added the emitter, so callers of
	///                 this method must account for this. In general, prefer using
	///                 [net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView#cullFace(Direction)] instead of this test.
	///
	/// @see #createGeometryKey(BlockAndTintGetter, BlockPos, BlockState, RandomSource)
	default void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		final List<BlockModelPart> parts = ((BlockStateModel) this).collectParts(random);
		final int partCount = parts.size();

		for (int i = 0; i < partCount; i++) {
			parts.get(i).emitQuads(emitter, cullTest);
		}
	}

	/// Creates a geometry key using the given context. A geometry key represents the exact geometry output from
	/// [#emitQuads] when given the same parameters as this method and a cull test that always returns
	/// `false`. Geometry keys are intended to be used in a cache to avoid recomputing expensive transformations
	/// applied to a certain model's geometry.
	///
	/// The geometry key must implement [Object#equals(Object)] and
	/// [Object#hashCode()]. The geometry key may be compared to the geometry key of **any other model**, not
	/// just those produced by this model instance, so care should be taken when selecting the type of the key.
	/// Generally, one class of model will want to make its own record class to use for geometry keys.
	///
	/// A `null` key means that a geometry key does exist for specifically the given context; a key may exist
	/// for a different context. It is always possible to create a key for any context, but some custom models may choose
	/// not to if doing so is too complex. Vanilla models correctly implement this method, but may return `null`
	/// when delegating to a submodel that returns `null`.
	///
	/// @param level The level in which the block exists.
	/// @param pos The position of the block in the level.
	/// @param state The block state whose model was queried for a geometry key. **This is not guaranteed to be the
	///              state corresponding to `this` model!**
	/// @param random Random object seeded per vanilla conventions.
	/// @return the geometry key, or `null` if one does not exist for the given context
	///
	/// @see #emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)
	@Nullable
	default Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return null;
	}

	/// Extension of [BlockStateModel#particleIcon()] that accepts level state. This method will be invoked most
	/// of the time, but the vanilla method may still be invoked when no level context is available.
	///
	/// **If your model delegates to other [BlockStateModel]s, ensure that it also delegates invocations of
	/// this method to its submodels as appropriate!**
	///
	/// @param level The level in which the block exists.
	/// @param pos The position of the block in the level.
	/// @param state The block state whose model was queried for the particle sprite. **This is not guaranteed to be the
	///              state corresponding to `this` model!**
	/// @return the particle sprite
	default TextureAtlasSprite particleIcon(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		return ((BlockStateModel) this).particleIcon();
	}
}

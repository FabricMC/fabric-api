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

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/**
 * Note: This interface is automatically implemented on {@link BlockModelPart} via Mixin and interface injection.
 */
public interface FabricBlockModelPart {
	/**
	 * Produces this model part's geometry. <b>This method must be called instead of
	 * {@link BlockModelPart#getQuads(Direction)} and {@link BlockModelPart#useAmbientOcclusion()}; the vanilla methods
	 * should be considered deprecated as they may not produce accurate results.</b> However, it is acceptable for a
	 * custom model part to only implement the vanilla methods as the default implementation of this method will
	 * delegate to the vanilla methods.
	 *
	 * <p>This method mainly exists for convenience when interacting with parts implemented and produced by vanilla
	 * code. Custom models should generally override
	 * {@link FabricBlockStateModel#emitQuads(QuadEmitter, BlockRenderView, BlockPos, BlockState, Random, Predicate)}
	 * instead of subclassing {@link BlockModelPart} and overriding this method.
	 *
	 * @param emitter Accepts model part output.
	 * @param cullTest A test that returns {@code true} for faces which will be culled and {@code false} for faces which
	 *                 may or may not be culled. Meant to be used to cull groups of quads or expensive dynamic quads
	 *                 early for performance. Early culled quads will likely not be added the emitter, so callers of
	 *                 this method must account for this. Since model parts should be completely static, this test
	 *                 should be used whenever possible.
	 */
	default void emitQuads(QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
		Renderer.get().emitBlockModelPartQuads((BlockModelPart) this, emitter, cullTest);
	}
}

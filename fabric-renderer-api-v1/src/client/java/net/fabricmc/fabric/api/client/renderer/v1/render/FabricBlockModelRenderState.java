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

package net.fabricmc.fabric.api.client.renderer.v1.render;

import java.util.function.Predicate;

import org.joml.Matrix4fc;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

/**
 * Note: This interface is automatically implemented on {@link BlockModelRenderState} via Mixin and interface injection.
 */
public interface FabricBlockModelRenderState {
	// TODO FRAPI 26.1: docs
	//  also the design here is not ideal because both setupModel and setupMesh override the transformation and renderType fields.
	//  if a user wants to use both methods, that's unnecessary and unintuitive. might not be worth changing as the part list is always initialized by setupModel.
	/**
	 * Alternative to {@link BlockModelRenderState#setupModel(Matrix4fc, boolean)} that returns a
	 * {@link QuadEmitter}.
	 *
	 * <p>This method should be used in favor of the vanilla one in order to support modded models.
	 * <b>The vanilla method is therefore considered deprecated.</b>
	 *
	 * @return an emitter to use with {@link BlockStateModel#emitQuads}.
	 * @see BlockStateModel#emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)
	 */
	default QuadEmitter setupMesh(Matrix4fc transformation, boolean hasTranslucency) {
		throw new IllegalStateException("Implemented via Mixin.");
	}
}

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

import org.joml.Matrix4fc;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

/**
 * Note: This interface is automatically implemented on {@link BlockModelRenderState} via Mixin and interface injection.
 */
public interface FabricBlockModelRenderState {
	// TODO FRAPI 26.1: the design here is not ideal because both setupModel and setupMesh override the transformation and renderType fields.
	//  if a user wants to use both methods, that's unnecessary and unintuitive. might not be worth changing as the part list is always initialized by setupModel.
	//  also, the user might not know hasTranslucency a priori.
	/**
	 * Alternative to {@link BlockModelRenderState#setupModel(Matrix4fc, boolean)} that returns a
	 * {@link QuadEmitter}.
	 *
	 * @return a quad emitter to use with {@link BlockStateModel#emitQuads}.
	 */
	default QuadEmitter setupMesh(Matrix4fc transformation, boolean hasTranslucency) {
		throw new IllegalStateException("Implemented via Mixin.");
	}
}

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

import net.minecraft.client.renderer.block.BlockModelRenderState;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModelPart;

/**
 * Note: This interface is automatically implemented on {@link BlockModelRenderState} via Mixin and interface injection.
 */
public interface FabricBlockModelRenderState {
	/**
	 * Alternative to {@link BlockModelRenderState#setupModel(Matrix4fc, boolean)} that returns a
	 * {@link QuadEmitter} that adds geometry to this state. Calling this method a second time
	 * clears all previously added geometry and any changes made to the emitter.
	 *
	 * <p>Calling this method or {@link BlockModelRenderState#setupModel(Matrix4fc, boolean)} clears
	 * both this state's vanilla model parts and mesh (whose quad emitter is returned by this
	 * method). If you must use both vanilla model parts and a quad emitter, use this method in
	 * conjunction with {@link FabricBlockStateModelPart#emitQuads(QuadEmitter, Predicate)}.
	 *
	 * @return a quad emitter that appends geometry to this state.
	 */
	default QuadEmitter setupMesh(Matrix4fc transformation, boolean hasTranslucency) {
		throw new IllegalStateException("Implemented via Mixin.");
	}
}

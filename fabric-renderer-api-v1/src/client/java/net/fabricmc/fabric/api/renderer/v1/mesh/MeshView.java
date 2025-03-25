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

package net.fabricmc.fabric.api.renderer.v1.mesh;

import java.util.function.Consumer;

import org.jetbrains.annotations.Range;

/**
 * A bundle of {@linkplain QuadView quads} encoded by the renderer. It may be {@linkplain MutableMesh mutable} or
 * {@linkplain Mesh immutable}.
 *
 * <p>Meshes are similar in purpose to {@code List<BakedQuad>} instances passed around in vanilla pipelines, but allow
 * the renderer to optimize their format for performance and memory allocation.
 *
 * <p>All declared methods in this interface are <b>not</b> thread-safe and must not be used concurrently. Subclasses
 * may override this contract.
 *
 * <p>Only the renderer should implement or extend this interface.
 */
public interface MeshView {
	/**
	 * Returns the number of quads encoded in this mesh.
	 */
	@Range(from = 0, to = Integer.MAX_VALUE)
	int size();

	/**
	 * Access all quads encoded in this mesh. The quad instance sent to the consumer should never be retained outside
	 * the current call to the consumer.
	 *
	 * <p>Nesting calls to this method on the same mesh is allowed.
	 */
	void forEach(Consumer<? super QuadView> action);

	/**
	 * Outputs all quads in this mesh to the given quad emitter.
	 */
	void outputTo(QuadEmitter emitter);
}

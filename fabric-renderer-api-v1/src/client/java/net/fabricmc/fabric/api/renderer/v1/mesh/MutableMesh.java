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

/**
 * A bundle of {@link MutableQuadView} instances encoded by the renderer that
 * can have more quads added to it. Typically used to build optimized,
 * immutable {@link Mesh}es via {@link #emitter()}, {@link #immutableCopy()},
 * and {@link #clear()}. Encoded quads can also be inspected, modified, and
 * outputted directly to allow for advanced use cases where creating an
 * immutable {@link Mesh} is not desirable.
 *
 * <p>All declared methods in this interface are <b>not</b> thread-safe and
 * should not be used concurrently. Inherited methods from {@link Mesh} are
 * still thread-safe when used in isolation.
 *
 * <p>Only the renderer should implement or extend this interface.
 */
public interface MutableMesh extends Mesh {
	/**
	 * Returns the {@link QuadEmitter} used to append quads to this mesh.
	 * Calling this method a second time invalidates any prior result.
	 * Do not retain references outside the context of this mesh.
	 */
	QuadEmitter emitter();

	/**
	 * Use to access all the quads encoded in this mesh and modify them as
	 * necessary. The quad instance sent to the consumer should never be
	 * retained outside the current call to the consumer.
	 */
	void forEachMutable(Consumer<? super MutableQuadView> action);

	/**
	 * Returns a new, optimized, immutable {@link Mesh} instance containing all
	 * quads currently encoded in {@code this} mesh. This operation does not
	 * change the state of {@code this} mesh; if you need to build another
	 * immutable mesh, call {@link #clear()} first.
	 *
	 * <p>If quad data has been added to the {@link #emitter()} but has
	 * not yet been emitted, calling this method will not affect it.
	 */
	Mesh immutableCopy();

	/**
	 * Resets this mesh to an empty state with zero quads, effectively clearing
	 * all existing quads.
	 */
	void clear();
}

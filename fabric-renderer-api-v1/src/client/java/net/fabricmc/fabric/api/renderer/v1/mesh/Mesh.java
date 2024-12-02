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

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

/**
 * A bundle of {@link QuadView} instances encoded by the renderer, typically
 * via {@link MutableMesh#immutableCopy()}.
 *
 * <p>Similar in purpose to the {@code List<BakedQuad>} instances returned by
 * {@link BakedModel#getQuads(BlockState, Direction, Random)}, but allows the
 * renderer to optimize the format for performance and memory allocation.
 *
 * <p>All declared methods in this interface are thread-safe and can be used
 * concurrently.
 *
 * <p>Only the renderer should implement or extend this interface.
 */
public interface Mesh {
	/**
	 * Returns the number of quads encoded in this mesh.
	 */
	@Range(from = 0, to = Integer.MAX_VALUE)
	int size();

	/**
	 * Use to access all the quads encoded in this mesh. The quad instance sent
	 * to the consumer should never be retained outside the current call to the
	 * consumer.
	 */
	void forEach(Consumer<? super QuadView> action);

	/**
	 * Outputs all quads in this mesh to the given quad emitter.
	 */
	void outputTo(QuadEmitter emitter);
}

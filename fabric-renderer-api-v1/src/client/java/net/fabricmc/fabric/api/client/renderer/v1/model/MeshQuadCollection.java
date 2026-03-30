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

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;

/**
 * A special {@link QuadCollection} which hides a {@link Mesh} instead of using {@link BakedQuad}s. Useful for custom
 * implementations of {@link UnbakedGeometry#bake(TextureSlots, ModelBaker, ModelState, ModelDebugName)} that want to
 * return a mesh. Instances of this class always return empty quad lists from inherited methods. Other public methods,
 * including {@link QuadCollection#materialFlags()} and {@link QuadCollection#hasMaterialFlag(int)}, will return
 * expected values computed from the mesh.
 *
 * <p>Any code that interacts with {@link QuadCollection} should first check {@code instanceof MeshQuadCollection} and
 * use {@link #getMesh()} if {@code true} or the vanilla methods otherwise.
 */
public final class MeshQuadCollection extends QuadCollection {
	private final Mesh mesh;
	private @BakedQuad.MaterialFlags int materialFlags = -1;

	public MeshQuadCollection(Mesh mesh) {
		super(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
		this.mesh = mesh;
	}

	/**
	 * Gets this geometry's mesh. Always use this method instead of vanilla methods when available.
	 */
	public Mesh getMesh() {
		return mesh;
	}

	private static @BakedQuad.MaterialFlags int computeMaterialFlags(final Mesh mesh) {
		var quadConsumer = new Consumer<QuadView>() {
			@BakedQuad.MaterialFlags int flags = 0;

			@Override
			public void accept(QuadView quad) {
				flags |= ModelHelper.computeMaterialFlags(quad);
			}
		};

		mesh.forEach(quadConsumer);
		return quadConsumer.flags;
	}

	@Override
	public @BakedQuad.MaterialFlags int materialFlags() {
		if (materialFlags == -1) {
			materialFlags = computeMaterialFlags(mesh);
		}

		return materialFlags;
	}
}

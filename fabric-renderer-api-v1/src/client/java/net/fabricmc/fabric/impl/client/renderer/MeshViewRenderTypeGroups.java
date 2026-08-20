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

package net.fabricmc.fabric.impl.client.renderer;

import java.util.function.Consumer;

import org.jetbrains.annotations.Range;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;

public record MeshViewRenderTypeGroups(MeshView solid, MeshView translucent) {
	public static MeshViewRenderTypeGroups split(MeshView mesh) {
		int[] counts = new int[2];
		mesh.forEach(quad -> counts[quad.itemRenderType().hasBlending() ? 1 : 0]++);
		return new MeshViewRenderTypeGroups(
				new FilteredMeshView(mesh, false, counts[0]),
				new FilteredMeshView(mesh, true, counts[1])
		);
	}

	// FIXME: This class violates the contract of MeshView. It should not exist.
	private record FilteredMeshView(MeshView mesh, boolean translucent, int size) implements MeshView {
		@Override
		@Range(from = 0, to = Integer.MAX_VALUE)
		public int size() {
			return size;
		}

		@Override
		public void forEach(Consumer<? super QuadView> action) {
			mesh.forEach(quad -> {
				if (quad.itemRenderType().hasBlending() == translucent) {
					action.accept(quad);
				}
			});
		}

		@Override
		public void outputTo(QuadEmitter emitter) {
			forEach(quad -> emitter.copyFrom(quad).emit());
		}
	}
}

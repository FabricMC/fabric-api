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

import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.SimpleModel;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;

/**
 * A special {@link BakedGeometry} which hides a {@link Mesh} instead of using {@link BakedQuad}s. Useful for custom
 * implementations of {@link Geometry#bake(ModelTextures, Baker, ModelBakeSettings, SimpleModel)} that want to return a
 * mesh. Instances of this class always return empty lists from inherited methods.
 *
 * <p>Any code that interacts with {@link BakedGeometry} should first check {@code instanceof MeshBakedGeometry} and use
 * {@link #getMesh()} if {@code true} or the vanilla methods otherwise.
 */
public final class MeshBakedGeometry extends BakedGeometry {
	private final Mesh mesh;

	public MeshBakedGeometry(Mesh mesh) {
		super(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
		this.mesh = mesh;
	}

	/**
	 * Gets this geometry's mesh. Always use this method instead of vanilla methods when available.
	 */
	public Mesh getMesh() {
		return mesh;
	}
}

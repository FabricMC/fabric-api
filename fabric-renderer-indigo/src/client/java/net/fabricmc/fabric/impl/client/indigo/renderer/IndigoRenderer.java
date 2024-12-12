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

package net.fabricmc.fabric.impl.client.indigo.renderer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.impl.client.indigo.renderer.material.MaterialFinderImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.material.RenderMaterialImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableMeshImpl;

/**
 * The Fabric default renderer implementation. Supports all
 * features defined in the API and offers no special materials.
 */
public class IndigoRenderer implements Renderer {
	public static final IndigoRenderer INSTANCE = new IndigoRenderer();

	public static final RenderMaterial STANDARD_MATERIAL = INSTANCE.materialFinder().find();

	static {
		INSTANCE.registerMaterial(RenderMaterial.STANDARD_ID, STANDARD_MATERIAL);
	}

	private final Map<Identifier, RenderMaterial> materialMap = new HashMap<>();

	private IndigoRenderer() { }

	@Override
	public MutableMesh mutableMesh() {
		return new MutableMeshImpl();
	}

	@Override
	public MaterialFinder materialFinder() {
		return new MaterialFinderImpl();
	}

	@Override
	public RenderMaterial materialById(Identifier id) {
		return materialMap.get(id);
	}

	@Override
	public boolean registerMaterial(Identifier id, RenderMaterial material) {
		if (materialMap.containsKey(id)) return false;

		// cast to prevent acceptance of impostor implementations
		materialMap.put(id, (RenderMaterialImpl) material);
		return true;
	}
}

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

package net.fabricmc.fabric.api.client.model.loading.v1;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.util.Identifier;

/**
 * An unbaked model that returns another {@link BakedModel} at {@linkplain #bake bake time}.
 * This allows multiple {@link UnbakedModel}s to share the same {@link BakedModel} instance
 * and prevents baking the same model multiple times.
 */
public record DelegatingUnbakedModel(Identifier delegate) implements UnbakedModel {
	/**
	 * Constructs a new delegating model.
	 *
	 * @param delegate The identifier of the underlying baked model.
	 */
	public DelegatingUnbakedModel {
	}

	@Override
	public void resolve(Resolver resolver) {
		resolver.resolve(delegate);
	}

	@Override
	public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
		return baker.bake(delegate, settings);
	}
}

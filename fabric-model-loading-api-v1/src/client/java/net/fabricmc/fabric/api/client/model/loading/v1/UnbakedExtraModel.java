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

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;

/**
 * An unbaked extra model.
 *
 * <p>Similar to {@link ItemModel.Unbaked} and other {@link ResolvableModel}, this model can
 * {@linkplain ResolvableModel.Resolver#resolveDependencies(Resolver) depend} on one or more model files, and then combine them into
 * a single baked model.
 *
 * @param <T> The type of the baked model.
 * @see ModelLoadingPlugin.Context#addModel(ExtraModelKey, UnbakedExtraModel)
 */
public interface UnbakedExtraModel<T> extends ResolvableModel {
	/**
	 * Bake this model.
	 *
	 * @param baker The current model baker.
	 * @return The fully-baked model.
	 */
	T bake(ModelBaker baker);
}

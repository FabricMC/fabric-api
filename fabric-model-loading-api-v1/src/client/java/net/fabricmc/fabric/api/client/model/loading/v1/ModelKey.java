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

import java.util.function.BiFunction;

import org.jetbrains.annotations.Contract;

import net.minecraft.util.Identifier;

/**
 * A unique key representing an extra model, not tied to a blockstate or item model.
 *
 * <p>Extra models can be registered with a {@link ModelLoadingPlugin} (see
 * {@link ModelLoadingPlugin.Context#addModel(ModelKey, Identifier, BiFunction)} and
 * {@link ModelLoadingPlugin.Context#addModel(ModelKey, UnbakedExtraModel)}). Once baking is complete, they may then be
 * queried from the model manager using {@link FabricBakedModelManager#getModel(ModelKey)}.
 *
 * @param <T> The type of the baked model.
 * @see FabricBakedModelManager#getModel(ModelKey)
 * @see ModelLoadingPlugin.Context#addModel(ModelKey, Identifier, BiFunction)
 * @see ModelLoadingPlugin.Context#addModel(ModelKey, UnbakedExtraModel)
 */
public final class ModelKey<T> {
	private ModelKey() {
	}

	/**
	 * Create a new unique model key.
	 *
	 * @param <T> The type of the baked model.
	 * @return The newly created model key.
	 */
	@Contract("-> new")
	public static <T> ModelKey<T> create() {
		return new ModelKey<>();
	}
}

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

import java.util.function.Supplier;

import org.jetbrains.annotations.Contract;

/**
 * A unique key representing an extra model, not tied to a block state or item model.
 *
 * <p>Extra models can be registered with a {@link ModelLoadingPlugin} (see
 * {@link ModelLoadingPlugin.Context#addModel(ExtraModelKey, UnbakedExtraModel)}). Once baking is complete, they may
 * then be queried from the model manager using {@link FabricBakedModelManager#getModel(ExtraModelKey)}.
 *
 * @param <T> The type of the baked model.
 * @see FabricBakedModelManager#getModel(ExtraModelKey)
 * @see ModelLoadingPlugin.Context#addModel(ExtraModelKey, UnbakedExtraModel)
 */
public final class ExtraModelKey<T> {
	private final Supplier<String> name;

	private ExtraModelKey(Supplier<String> debugName) {
		this.name = debugName;
	}

	/**
	 * Create a new unique model key.
	 *
	 * @param <T> The type of the baked model.
	 * @return The newly created model key.
	 */
	@Contract("-> new")
	public static <T> ExtraModelKey<T> create() {
		return new ExtraModelKey<>(() -> "unnamed");
	}

	/**
	 * Create a new unique model key.
	 *
	 * @param name The name of this model key, shown in error messages.
	 * @param <T>  The type of the baked model.
	 * @return The newly created model key.
	 */
	@Contract("_ -> new")
	public static <T> ExtraModelKey<T> create(Supplier<String> name) {
		return new ExtraModelKey<>(name);
	}

	@Override
	public String toString() {
		return "ExtraModelKey(" + name.get() + ")";
	}
}

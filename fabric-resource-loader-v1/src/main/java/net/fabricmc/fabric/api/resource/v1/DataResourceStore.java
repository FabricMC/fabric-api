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

package net.fabricmc.fabric.api.resource.v1;

/**
 * Represents a resource store for data.
 *
 * <p>Such resource store can be filled in the application phase of data {@linkplain net.minecraft.resource.ResourceReloader resource reloaders}.
 * And queried through an instance of {@link net.minecraft.server.MinecraftServer}.
 */
public interface DataResourceStore {
	/**
	 * Represents a typed key for {@linkplain DataResourceStore the data resource store}.
	 *
	 * @param <T> the type of this key
	 */
	final class Key<T> {
	}

	/**
	 * Gets data stored at the given key, or throws if not found.
	 *
	 * @param key the key
	 * @return the data stored at the given key
	 * @param <T> the type of data
	 */
	<T> T getOrThrow(Key<T> key);

	interface Mutable extends DataResourceStore {
		/**
		 * Puts data at the given key.
		 *
		 * @param key the key to store at
		 * @param data the data to store
		 * @param <T> the type of data
		 */
		<T> void put(Key<T> key, T data);
	}
}

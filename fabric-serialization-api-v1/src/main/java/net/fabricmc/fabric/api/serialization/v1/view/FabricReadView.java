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

package net.fabricmc.fabric.api.serialization.v1.view;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import net.minecraft.storage.ReadView;

import net.fabricmc.fabric.impl.serialization.SpecialCodecs;

/**
 * Fabric provided extension of ReadView.
 *
 * <p>Note: This interface is automatically implemented on {@link ReadView} via Mixin and interface injection.
 */
public interface FabricReadView {
	/**
	 * Returns a collection of keys available in this {@link ReadView}.
	 *
	 * @return collection of keys or empty list if this {@link ReadView} is empty.
	 */
	default Collection<String> keys() {
		//noinspection deprecation
		return ((ReadView) this).read(SpecialCodecs.KEYS_EXTRACT).orElse(List.of());
	}

	/**
	 * Checks if this {@link ReadView} contains data under provided key.
	 *
	 * @param key key to check for
	 * @return true, when this {@link ReadView} contains data under provided key, otherwise false
	 */
	default boolean contains(String key) {
		return ((ReadView) this).read(SpecialCodecs.contains(key)).orElseThrow();
	}

	/**
	 * Returns an long array present in this {@link ReadView} under provided key.
	 *
	 * @param key key to check for
	 * @return long array wrapped in optional if long array is present, empty Optional otherwise
	 */
	default Optional<long[]> getOptionalLongArray(String key) {
		return ((ReadView) this).read(key, SpecialCodecs.LONG_ARRAY);
	}

	/**
	 * Returns an byte array present in this {@link ReadView} under provided key.
	 *
	 * @param key key to check for
	 * @return byte array wrapped in optional if byte array is present, empty Optional otherwise
	 */
	default Optional<byte[]> getOptionalByteArray(String key) {
		return ((ReadView) this).read(key, SpecialCodecs.BYTE_ARRAY);
	}
}

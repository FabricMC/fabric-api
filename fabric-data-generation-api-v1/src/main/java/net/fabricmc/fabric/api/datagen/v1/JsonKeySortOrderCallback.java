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

package net.fabricmc.fabric.api.datagen.v1;

/**
 * Provides a callback for setting the sort priority of object keys in generated JSON files.
 */
@FunctionalInterface
public interface JsonKeySortOrderCallback {
	/**
	 * Sets the sort priority for a given object key within generated JSON files.
	 * @param key the key to set priority for
	 * @param priority the priority for the key, where keys with lower priority are sorted before keys with higher priority
	 * @implNote The default priority is 2.
	 * @see net.minecraft.data.DataProvider#FIXED_ORDER_FIELDS
	 */
	void add(String key, int priority);
}

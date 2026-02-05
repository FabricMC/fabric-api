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

package net.fabricmc.fabric.test.lookup.api;

import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;

/**
 * Something that can provide items.
 */
public interface ItemExtractable {
	/**
	 * Try to extract a single stack.
	 * @param maxCount The maximum number of items to extract
	 * @param filter What items to extract. Please note that the predicate should be independent of the count of the stack!
	 * @param simulate If true, don't modify any state
	 * @return The extracted stack
	 */
	ItemStack tryExtract(int maxCount, Predicate<ItemStack> filter, boolean simulate);
}

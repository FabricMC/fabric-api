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

import net.minecraft.world.item.ItemStack;

/**
 * Something that can accept items.
 */
public interface ItemInsertable {
	/**
	 * Try to insert some items. If this object can accept a stack of n items, it should also accept the same stack with
	 * a smaller count!
	 * @param input The input items. Must not be changed by this function!
	 * @param simulate If true, don't modify any state
	 * @return The leftover items; it should never be the received input stack!
	 */
	ItemStack tryInsert(ItemStack input, boolean simulate);
}

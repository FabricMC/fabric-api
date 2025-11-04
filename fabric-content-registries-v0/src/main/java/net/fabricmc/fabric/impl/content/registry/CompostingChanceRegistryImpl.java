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

package net.fabricmc.fabric.impl.content.registry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;

public class CompostingChanceRegistryImpl implements CompostingChanceRegistry {
	@Override
	public Float get(ItemLike item) {
		return ComposterBlock.COMPOSTABLES.getOrDefault(item.asItem(), 0.0F);
	}

	@Override
	public void add(ItemLike item, Float value) {
		ComposterBlock.COMPOSTABLES.put(item.asItem(), value);
	}

	@Override
	public void add(TagKey<Item> tag, Float value) {
		throw new UnsupportedOperationException("Tags currently not supported!");
	}

	@Override
	public void remove(ItemLike item) {
		ComposterBlock.COMPOSTABLES.removeFloat(item.asItem());
	}

	@Override
	public void remove(TagKey<Item> tag) {
		throw new UnsupportedOperationException("Tags currently not supported!");
	}

	@Override
	public void clear(ItemLike item) {
		throw new UnsupportedOperationException("CompostingChanceRegistry operates directly on the vanilla map - clearing not supported!");
	}

	@Override
	public void clear(TagKey<Item> tag) {
		throw new UnsupportedOperationException("CompostingChanceRegistry operates directly on the vanilla map - clearing not supported!");
	}
}

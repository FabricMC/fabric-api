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

package net.fabricmc.fabric.api.registry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.util.Block2ObjectMap;
import net.fabricmc.fabric.impl.content.registry.FlammableBlockRegistryImpl;

public interface FlammableBlockRegistry extends Block2ObjectMap<FlammableBlockRegistry.Entry> {
	static FlammableBlockRegistry getDefaultInstance() {
		return getInstance(Blocks.FIRE);
	}

	static FlammableBlockRegistry getInstance(Block block) {
		return FlammableBlockRegistryImpl.getInstance(block);
	}

	default void add(Block block, int burn, int spread) {
		this.add(block, new Entry(burn, spread));
	}

	default void add(TagKey<Block> tag, int burn, int spread) {
		this.add(tag, new Entry(burn, spread));
	}

	final class Entry {
		private final int burn, spread;

		public Entry(int burn, int spread) {
			this.burn = burn;
			this.spread = spread;
		}

		public int getBurnChance() {
			return burn;
		}

		public int getSpreadChance() {
			return spread;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Entry)) {
				return false;
			} else {
				Entry other = (Entry) o;
				return other.burn == burn && other.spread == spread;
			}
		}

		@Override
		public int hashCode() {
			return burn * 11 + spread;
		}
	}
}

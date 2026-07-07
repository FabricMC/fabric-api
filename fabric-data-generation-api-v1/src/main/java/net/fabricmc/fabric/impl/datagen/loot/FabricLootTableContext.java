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

package net.fabricmc.fabric.impl.datagen.loot;

import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public record FabricLootTableContext(HolderLookup.Provider registries) implements LootTableSubProvider.Context {
	@Override
	public Holder.Reference<LootTable> accept(ResourceKey<LootTable> key, LootTable.Builder value) {
		return Holder.Reference.createStandAlone(registries.lookupOrThrow(Registries.LOOT_TABLE), key);
	}

	@Override
	public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
		return registries.lookupOrThrow(key);
	}

	@Override
	public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
		return registries.lookupOrThrow(key).listElements();
	}
}

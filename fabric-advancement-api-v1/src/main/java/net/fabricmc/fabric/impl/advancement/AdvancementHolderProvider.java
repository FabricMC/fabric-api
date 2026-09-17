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

package net.fabricmc.fabric.impl.advancement;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

// Exact copy of LootTableHolderProvider
public final class AdvancementHolderProvider implements HolderLookup.Provider {
	private final RegistryOps.RegistryInfoLookup registryInfoLookup;
	private final HolderLookup.Provider holderLookup;

	public AdvancementHolderProvider(RegistryOps.RegistryInfoLookup registryInfoLookup, HolderLookup.Provider holderLookup) {
		this.registryInfoLookup = registryInfoLookup;
		this.holderLookup = holderLookup;
	}

	@Override
	public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
		return holderLookup.listRegistryKeys();
	}

	@Override
	public <A> Optional<? extends HolderLookup.RegistryLookup<A>> lookup(ResourceKey<? extends Registry<? extends A>> key) {
		if (registryInfoLookup.lookup(key).isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(new AdvancementLookup<>(key, registryInfoLookup.lookup(key).orElseThrow(), holderLookup.lookup(key).orElse(null)));
	}

	@Override
	public <A> Optional<Holder.Reference<A>> get(ResourceKey<A> id) {
		return registryInfoLookup.lookup(id.registryKey()).flatMap(getter -> getter.get(id));
	}

	@Override
	public <T> Holder.Reference<T> getOrThrow(ResourceKey<T> id) {
		return registryInfoLookup.lookup(id.registryKey()).orElseThrow().getOrThrow(id);
	}

	@Override
	public <T> HolderSet.Named<T> getOrThrow(TagKey<T> id) {
		return registryInfoLookup.lookup(id.registry()).orElseThrow().getOrThrow(id);
	}

	@Override
	public <T> Optional<HolderSet.Named<T>> get(TagKey<T> id) {
		return registryInfoLookup.lookup(id.registry()).flatMap(getter -> getter.get(id));
	}
}

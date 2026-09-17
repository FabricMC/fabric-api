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

import com.mojang.serialization.Lifecycle;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

// Exact copy of LootTableLookup
public final class AdvancementLookup<T> implements HolderLookup.RegistryLookup<T> {
	private final ResourceKey<? extends Registry<? extends T>> key;
	private final HolderGetter<T> lookup;
	private final @Nullable RegistryLookup<T> holderLookup;

	public AdvancementLookup(ResourceKey<? extends Registry<? extends T>> key, HolderGetter<T> registryLookup, @Nullable RegistryLookup<T> holderLookup) {
		this.key = key;
		this.lookup = registryLookup;
		this.holderLookup = holderLookup;
	}

	@Override
	public Stream<Holder.Reference<T>> listElements() {
		if (holderLookup != null) {
			return holderLookup.listElements();
		}

		return Stream.empty();
	}

	@Override
	public Stream<HolderSet.Named<T>> listTags() {
		if (holderLookup != null) {
			return holderLookup.listTags();
		}

		return Stream.empty();
	}

	@Override
	public ResourceKey<? extends Registry<? extends T>> key() {
		return key;
	}

	@Override
	public Lifecycle registryLifecycle() {
		if (holderLookup != null) {
			return holderLookup.registryLifecycle();
		}

		return Lifecycle.stable();
	}

	@Override
	public Optional<Holder.Reference<T>> get(ResourceKey<T> id) {
		return lookup.get(id);
	}

	@Override
	public Optional<HolderSet.Named<T>> get(TagKey<T> id) {
		return lookup.get(id);
	}
}

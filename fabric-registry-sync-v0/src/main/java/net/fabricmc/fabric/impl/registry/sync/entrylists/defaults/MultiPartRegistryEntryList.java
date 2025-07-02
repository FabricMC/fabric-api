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

package net.fabricmc.fabric.impl.registry.sync.entrylists.defaults;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryList;

/**
 * A default template implementation of {@link RegistryEntryList} that derives it's contents from some set of backing lists.
 * the resulting list is cached as a set and a list, to provide a consistent iteration order, and O(1) contains
 *
 * @param <T> type of entries held by this list
 */
public abstract class MultiPartRegistryEntryList<T> implements CustomRegistryEntryList<T> {
	private final ImmutableList<RegistryEntryList<T>> parts;
	@Nullable
	private Set<RegistryEntry<T>> cachedSet = null;
	@Nullable
	private List<RegistryEntry<T>> cachedList = null;

	public MultiPartRegistryEntryList(List<RegistryEntryList<T>> parts) {
		this.parts = ImmutableList.copyOf(parts);

		for (RegistryEntryList<T> part : parts) {
			part.registerDependency(this);
		}
	}

	public ImmutableList<RegistryEntryList<T>> getParts() {
		return this.parts;
	}

	protected abstract Set<RegistryEntry<T>> createCache();

	private Set<RegistryEntry<T>> getCachedSet() {
		if (this.cachedSet == null) {
			this.cachedSet = this.createCache();
		}

		return this.cachedSet;
	}

	private List<RegistryEntry<T>> getCachedList() {
		if (this.cachedList == null) {
			this.cachedList = List.copyOf(this.getCachedSet());
		}

		return this.cachedList;
	}

	@Override
	public void invalidate() {
		this.cachedSet = null;
		this.cachedList = null;
		CustomRegistryEntryList.super.invalidate();
	}

	@Override
	public Stream<RegistryEntry<T>> stream() {
		return this.getCachedList().stream();
	}

	@Override
	public int size() {
		return this.getCachedList().size();
	}

	@Override
	public boolean isBound() {
		return this.parts.stream().allMatch(RegistryEntryList::isBound);
	}

	@Override
	public Either<TagKey<T>, List<RegistryEntry<T>>> getStorage() {
		return Either.right(this.getCachedList());
	}

	@Override
	public Optional<RegistryEntry<T>> getRandom(Random random) {
		return Util.getRandomOrEmpty(getCachedList(), random);
	}

	@Override
	public RegistryEntry<T> get(int index) {
		return this.getCachedList().get(index);
	}

	@Override
	public boolean contains(RegistryEntry<T> entry) {
		return this.getCachedSet().contains(entry);
	}

	@Override
	public boolean ownerEquals(RegistryEntryOwner<T> owner) {
		return this.parts.stream()
				.allMatch(it -> it.ownerEquals(owner));
	}

	@Override
	public Optional<TagKey<T>> getTagKey() {
		return Optional.empty();
	}

	@Override
	@NotNull
	public Iterator<RegistryEntry<T>> iterator() {
		return this.getCachedList().iterator();
	}
}

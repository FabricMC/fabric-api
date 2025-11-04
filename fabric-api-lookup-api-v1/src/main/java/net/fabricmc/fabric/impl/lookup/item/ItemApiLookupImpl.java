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

package net.fabricmc.fabric.impl.lookup.item;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;

public class ItemApiLookupImpl<A, C> implements ItemApiLookup<A, C> {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-api-lookup-api-v1/item");
	private static final ApiLookupMap<ItemApiLookup<?, ?>> LOOKUPS = ApiLookupMap.create(ItemApiLookupImpl::new);

	@SuppressWarnings("unchecked")
	public static <A, C> ItemApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass) {
		return (ItemApiLookup<A, C>) LOOKUPS.getLookup(lookupId, apiClass, contextClass);
	}

	private final Identifier identifier;
	private final Class<A> apiClass;
	private final Class<C> contextClass;
	private final ApiProviderMap<Item, ItemApiProvider<A, C>> providerMap = ApiProviderMap.create();
	private final List<ItemApiProvider<A, C>> fallbackProviders = new CopyOnWriteArrayList<>();

	@SuppressWarnings("unchecked")
	private ItemApiLookupImpl(Identifier identifier, Class<?> apiClass, Class<?> contextClass) {
		this.identifier = identifier;
		this.apiClass = (Class<A>) apiClass;
		this.contextClass = (Class<C>) contextClass;
	}

	@Override
	public @Nullable A find(ItemStack itemStack, C context) {
		Objects.requireNonNull(itemStack, "ItemStack may not be null.");

		@Nullable
		ItemApiProvider<A, C> provider = providerMap.get(itemStack.getItem());

		if (provider != null) {
			A instance = provider.find(itemStack, context);

			if (instance != null) {
				return instance;
			}
		}

		for (ItemApiProvider<A, C> fallbackProvider : fallbackProviders) {
			A instance = fallbackProvider.find(itemStack, context);

			if (instance != null) {
				return instance;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerSelf(ItemLike... items) {
		for (ItemLike itemConvertible : items) {
			Item item = itemConvertible.asItem();

			if (!apiClass.isAssignableFrom(item.getClass())) {
				String errorMessage = String.format(
						"Failed to register self-implementing items. API class %s is not assignable from item class %s.",
						apiClass.getCanonicalName(),
						item.getClass().getCanonicalName()
				);
				throw new IllegalArgumentException(errorMessage);
			}
		}

		registerForItems((itemStack, context) -> (A) itemStack.getItem(), items);
	}

	@Override
	public void registerForItems(ItemApiProvider<A, C> provider, ItemLike... items) {
		Objects.requireNonNull(provider, "ItemApiProvider may not be null.");

		if (items.length == 0) {
			throw new IllegalArgumentException("Must register at least one ItemConvertible instance with an ItemApiProvider.");
		}

		for (ItemLike itemConvertible : items) {
			Item item = itemConvertible.asItem();
			Objects.requireNonNull(item, "Item convertible in item form may not be null.");

			if (providerMap.putIfAbsent(item, provider) != null) {
				LOGGER.warn("Encountered duplicate API provider registration for item: " + BuiltInRegistries.ITEM.getKey(item));
			}
		}
	}

	@Override
	public void registerFallback(ItemApiProvider<A, C> fallbackProvider) {
		Objects.requireNonNull(fallbackProvider, "ItemApiProvider may not be null.");

		fallbackProviders.add(fallbackProvider);
	}

	@Override
	public Identifier getId() {
		return identifier;
	}

	@Override
	public Class<A> apiClass() {
		return apiClass;
	}

	@Override
	public Class<C> contextClass() {
		return contextClass;
	}

	@Override
	@Nullable
	public ItemApiProvider<A, C> getProvider(Item item) {
		return providerMap.get(item);
	}
}

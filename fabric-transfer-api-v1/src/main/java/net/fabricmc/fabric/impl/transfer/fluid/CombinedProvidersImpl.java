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

package net.fabricmc.fabric.impl.transfer.fluid;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

public class CombinedProvidersImpl {
	public static Event<FluidStorage.CombinedItemApiProvider> createEvent(boolean invokeFallback) {
		return EventFactory.createArrayBacked(FluidStorage.CombinedItemApiProvider.class, listeners -> context -> {
			List<Storage<FluidVariant>> storages = new ArrayList<>();

			for (FluidStorage.CombinedItemApiProvider listener : listeners) {
				Storage<FluidVariant> found = listener.find(context);

				if (found != null) {
					storages.add(found);
				}
			}

			// Allow combining per-item combined providers with fallback combined providers.
			if (!storages.isEmpty() && invokeFallback) {
				// Only invoke the fallback if API Lookup doesn't invoke it right after,
				// that is only invoke the fallback if storages were offered,
				// otherwise we can wait for API Lookup to invoke the fallback provider itself.
				Storage<FluidVariant> fallbackFound = FluidStorage.GENERAL_COMBINED_PROVIDER.invoker().find(context);

				if (fallbackFound != null) {
					storages.add(fallbackFound);
				}
			}

			return storages.isEmpty() ? null : new CombinedStorage<>(storages);
		});
	}

	private static class Provider implements ItemApiLookup.ItemApiProvider<Storage<FluidVariant>, ContainerItemContext> {
		private final Event<FluidStorage.CombinedItemApiProvider> event = createEvent(true);

		@Override
		@Nullable
		public Storage<FluidVariant> find(ItemStack itemStack, ContainerItemContext context) {
			if (!context.getItemVariant().matches(itemStack)) {
				String errorMessage = String.format(
						"Query stack %s and ContainerItemContext variant %s don't match.",
						itemStack,
						context.getItemVariant()
				);
				throw new IllegalArgumentException(errorMessage);
			}

			return event.invoker().find(context);
		}
	}

	public static Event<FluidStorage.CombinedItemApiProvider> getOrCreateItemEvent(Item item) {
		ItemApiLookup.ItemApiProvider<Storage<FluidVariant>, ContainerItemContext> existingProvider = FluidStorage.ITEM.getProvider(item);

		if (existingProvider == null) {
			FluidStorage.ITEM.registerForItems(new Provider(), item);
			// The provider might not be new Provider() if a concurrent registration happened, re-query.
			existingProvider = FluidStorage.ITEM.getProvider(item);
		}

		if (existingProvider instanceof Provider registeredProvider) {
			return registeredProvider.event;
		} else {
			String errorMessage = String.format(
					"An incompatible provider was already registered for item %s. Provider: %s.",
					item,
					existingProvider
			);
			throw new IllegalStateException(errorMessage);
		}
	}
}

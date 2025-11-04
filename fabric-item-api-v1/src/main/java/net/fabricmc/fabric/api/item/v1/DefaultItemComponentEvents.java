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

package net.fabricmc.fabric.api.item.v1;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events to modify the default {@link DataComponentMap} of items.
 */
public final class DefaultItemComponentEvents {
	/**
	 * Event used to add or remove data components to known items.
	 */
	public static final Event<ModifyCallback> MODIFY = EventFactory.createArrayBacked(ModifyCallback.class, listeners -> context -> {
		for (ModifyCallback listener : listeners) {
			listener.modify(context);
		}
	});

	private DefaultItemComponentEvents() {
	}

	public interface ModifyContext {
		/**
		 * Modify the default data components of the specified item.
		 *
		 * @param itemPredicate A predicate to match items to modify
		 * @param builderConsumer A consumer that provides a {@link DataComponentMap.Builder} to modify the item's components.
		 */
		void modify(Predicate<Item> itemPredicate, BiConsumer<DataComponentMap.Builder, Item> builderConsumer);

		/**
		 * Modify the default data components of the specified item.
		 *
		 * @param item The item to modify
		 * @param builderConsumer A consumer that provides a {@link DataComponentMap.Builder} to modify the item's components.
		 */
		default void modify(Item item, Consumer<DataComponentMap.Builder> builderConsumer) {
			modify(Predicate.isEqual(item), (builder, _item) -> builderConsumer.accept(builder));
		}

		/**
		 * Modify the default data components of the specified items.
		 * @param items The items to modify
		 * @param builderConsumer A consumer that provides a {@link DataComponentMap.Builder} to modify the item's components.
		 */
		default void modify(Collection<Item> items, BiConsumer<DataComponentMap.Builder, Item> builderConsumer) {
			modify(items::contains, builderConsumer);
		}
	}

	@FunctionalInterface
	public interface ModifyCallback {
		/**
		 * Modify the default data components of items using the provided {@link ModifyContext} instance.
		 *
		 * @param context The context to modify items
		 */
		void modify(ModifyContext context);
	}
}

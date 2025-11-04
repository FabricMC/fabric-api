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

package net.fabricmc.fabric.impl.item;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.mixin.item.ItemAccessor;

public class DefaultItemComponentImpl {
	public static void modifyItemComponents() {
		DefaultItemComponentEvents.MODIFY.invoker().modify(ModifyContextImpl.INSTANCE);
	}

	static class ModifyContextImpl implements DefaultItemComponentEvents.ModifyContext {
		private static final ModifyContextImpl INSTANCE = new ModifyContextImpl();

		private ModifyContextImpl() {
		}

		@Override
		public void modify(Predicate<Item> itemPredicate, BiConsumer<DataComponentMap.Builder, Item> builderConsumer) {
			for (Item item : BuiltInRegistries.ITEM) {
				if (itemPredicate.test(item)) {
					DataComponentMap.Builder builder = DataComponentMap.builder().addAll(item.components());
					builderConsumer.accept(builder, item);
					((ItemAccessor) item).setComponents(builder.build());
				}
			}
		}
	}
}

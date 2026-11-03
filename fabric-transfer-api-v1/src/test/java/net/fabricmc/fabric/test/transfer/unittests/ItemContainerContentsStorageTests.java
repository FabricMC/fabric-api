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

package net.fabricmc.fabric.test.transfer.unittests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

/**
 * Tests {@link net.fabricmc.fabric.impl.transfer.item.ItemContainerContentsStorage}.
 */
public class ItemContainerContentsStorageTests extends AbstractTransferApiTest {
	@BeforeAll
	static void beforeAll() {
		bootstrap();
	}

	@Test
	public void testInsert() {
		SimpleContainer container = new SimpleContainer(new ItemStack(Items.SHULKER_BOX));
		SingleSlotStorage<ItemVariant> storage = ContainerStorage.of(container, null).getSlot(0);
		Storage<ItemVariant> contents = ContainerItemContext.ofSingleSlot(storage).find(ItemStorage.ITEM);

		try (Transaction transaction = Transaction.openOuter()) {
			assertEquals(32, contents.insert(ItemVariant.of(Items.DIRT), 32, transaction));
			transaction.commit();
		}

		assertEquals(
				new DisplayWrapper(ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIRT, 32)))),
				new DisplayWrapper(container.getItem(0).get(DataComponents.CONTAINER))
		);
	}

	private record DisplayWrapper(@Nullable ItemContainerContents contents) {
		@Override
		public @NonNull String toString() {
			return contents == null ? "null" : contents.itemCopies().toList().toString();
		}
	}
}

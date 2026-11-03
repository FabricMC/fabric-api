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

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

/**
 * Tests {@link net.fabricmc.fabric.impl.transfer.item.ContainerComponentStorage}.
 */
public class ContainerComponentStorageTests extends AbstractTransferApiTest {
	@BeforeAll
	static void beforeAll() {
		bootstrap();
	}

	@Test
	public void testInsert() {
		SimpleInventory container = new SimpleInventory(new ItemStack(Items.SHULKER_BOX));
		SingleSlotStorage<ItemVariant> storage = InventoryStorage.of(container, null).getSlot(0);
		Storage<ItemVariant> contents = ContainerItemContext.ofSingleSlot(storage).find(ItemStorage.ITEM);

		try (Transaction transaction = Transaction.openOuter()) {
			assertEquals(32, contents.insert(ItemVariant.of(Items.DIRT), 32, transaction));
			transaction.commit();
		}

		assertEquals(
				new DisplayWrapper(ContainerComponent.fromStacks(List.of(new ItemStack(Items.DIRT, 32)))),
				new DisplayWrapper(container.getStack(0).get(DataComponentTypes.CONTAINER))
		);
	}

	private record DisplayWrapper(@Nullable ContainerComponent contents) {
		@Override
		public String toString() {
			return contents == null ? "null" : contents.stream().toList().toString();
		}
	}
}

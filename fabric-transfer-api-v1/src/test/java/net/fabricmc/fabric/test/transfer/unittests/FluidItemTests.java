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

import static net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BOTTLE;
import static net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;
import static net.fabricmc.fabric.test.transfer.TestUtil.assertEquals;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

class FluidItemTests extends AbstractTransferApiTest {
	@BeforeAll
	static void beforeAll() {
		bootstrap();
	}

	@Test
	public void testFluidItemApi() {
		FluidVariant water = FluidVariant.of(Fluids.WATER);
		ItemVariant waterBucket = ItemVariant.of(Items.WATER_BUCKET);
		Container testInventory = new FluidItemTestInventory(ItemStack.EMPTY, new ItemStack(Items.BUCKET), new ItemStack(Items.WATER_BUCKET));

		Storage<FluidVariant> slot1Storage = new InventoryContainerItem(testInventory, 1).find(FluidStorage.ITEM);
		Storage<FluidVariant> slot2Storage = new InventoryContainerItem(testInventory, 2).find(FluidStorage.ITEM);

		if (slot1Storage == null || slot2Storage == null) throw new AssertionError("We should have provided a fluid storage for buckets.");

		try (Transaction transaction = Transaction.openOuter()) {
			// Test extract.
			if (slot2Storage.extract(water, BUCKET, transaction) != BUCKET) throw new AssertionError("Should have extracted from full bucket.");
			// Test that an empty bucket was added.
			if (!stackEquals(testInventory.getItem(1), Items.BUCKET, 2)) throw new AssertionError("Buckets should have stacked.");
			// Test that we can't extract again
			if (slot2Storage.extract(water, BUCKET, transaction) != 0) throw new AssertionError("Should not have extracted a second time.");
			// Now insert water into slot 1.
			if (slot1Storage.insert(water, BUCKET, transaction) != BUCKET) throw new AssertionError("Failed to insert.");
			// Check that it filled slot 0.
			if (!stackEquals(testInventory.getItem(0), Items.WATER_BUCKET, 1)) throw new AssertionError("Should have filled slot 0.");
			// Now we yeet the bucket just because we can.
			SingleSlotStorage<ItemVariant> slot0 = InventoryStorage.of(testInventory, null).getSlots().get(0);
			if (slot0.extract(waterBucket, 1, transaction) != 1) throw new AssertionError("Failed to yeet bucket.");
			// Now insert should fill slot 1 with a bucket.
			if (slot1Storage.insert(water, BUCKET, transaction) != BUCKET) throw new AssertionError("Failed to insert.");
			// Check inventory contents.
			if (!testInventory.getItem(0).isEmpty()) throw new AssertionError("Slot 0 should have been empty.");
			if (!stackEquals(testInventory.getItem(1), Items.WATER_BUCKET, 1)) throw new AssertionError("Should have filled slot 1 with a water bucket.");
		}

		// Check contents after abort
		if (!testInventory.getItem(0).isEmpty()) throw new AssertionError("Failed to abort slot 0.");
		if (!stackEquals(testInventory.getItem(1), Items.BUCKET, 1)) throw new AssertionError("Failed to abort slot 1.");
		if (!stackEquals(testInventory.getItem(2), Items.WATER_BUCKET, 1)) throw new AssertionError("Failed to abort slot 2.");
	}

	private static boolean stackEquals(ItemStack stack, Item item, int count) {
		return stack.getItem() == item && stack.getCount() == count;
	}

	private static class FluidItemTestInventory extends SimpleContainer {
		FluidItemTestInventory(ItemStack... stacks) {
			super(stacks);
		}

		@Override
		public boolean canPlaceItem(int slot, ItemStack stack) {
			return slot != 2; // Forbid insertion into slot 2.
		}
	}

	private static class InventoryContainerItem implements ContainerItemContext {
		private final InventoryStorage inventory;
		private final SingleSlotStorage<ItemVariant> slot;

		InventoryContainerItem(Container inv, int slotIndex) {
			this.inventory = InventoryStorage.of(inv, null);
			this.slot = inventory.getSlots().get(slotIndex);
		}

		@Override
		public SingleSlotStorage<ItemVariant> getMainSlot() {
			return slot;
		}

		@Override
		public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
			long inserted = 0;

			// Try to be smart and stack first!
			for (SingleSlotStorage<ItemVariant> slot : inventory.getSlots()) {
				if (slot.getResource().equals(itemVariant)) {
					inserted += slot.insert(itemVariant, maxAmount - inserted, transactionContext);
				}
			}

			return inserted + inventory.insert(itemVariant, maxAmount - inserted, transactionContext);
		}

		@Override
		public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
			return inventory.getSlots();
		}
	}

	@Test
	public void testWaterPotion() {
		FluidVariant water = FluidVariant.of(Fluids.WATER);
		Container testInventory = new SimpleContainer(new ItemStack(Items.GLASS_BOTTLE));

		// Try to fill empty potion
		Storage<FluidVariant> emptyBottleStorage = new InventoryContainerItem(testInventory, 0).find(FluidStorage.ITEM);

		try (Transaction transaction = Transaction.openOuter()) {
			if (emptyBottleStorage.insert(water, Long.MAX_VALUE, transaction) != BOTTLE) throw new AssertionError("Failed to insert.");
			transaction.commit();
		}

		if (getPotion(testInventory.getItem(0)) != Potions.WATER) throw new AssertionError("Expected water potion.");

		// Try to empty from water potion
		Storage<FluidVariant> waterBottleStorage = new InventoryContainerItem(testInventory, 0).find(FluidStorage.ITEM);

		try (Transaction transaction = Transaction.openOuter()) {
			if (waterBottleStorage.extract(water, Long.MAX_VALUE, transaction) != BOTTLE) throw new AssertionError("Failed to extract.");
			transaction.commit();
		}

		// Make sure extraction nothing is returned for other potions
		setPotion(testInventory.getItem(0), Potions.LUCK);
		Storage<FluidVariant> luckyStorage = new InventoryContainerItem(testInventory, 0).find(FluidStorage.ITEM);

		if (StorageUtil.findStoredResource(luckyStorage) != null) {
			throw new AssertionError("Found a resource in an unhandled potion.");
		}

		// Make sure extraction returns nothing for no potion at all
		testInventory.setItem(0, new ItemStack(Items.POTION));
		Storage<FluidVariant> defaultStorage = new InventoryContainerItem(testInventory, 0).find(FluidStorage.ITEM);

		if (StorageUtil.findStoredResource(defaultStorage) != null) {
			throw new AssertionError("Found a resource in empty potion.");
		}
	}

	@Test
	public void testSimpleContentsQuery() {
		assertEquals(
				new ResourceAmount<>(FluidVariant.of(Fluids.WATER), BUCKET),
				StorageUtil.findExtractableContent(
						ContainerItemContext.withConstant(new ItemStack(Items.WATER_BUCKET)).find(FluidStorage.ITEM),
						null
				)
		);
		// Test the filtering.
		assertEquals(
				null,
				StorageUtil.findExtractableContent(
						ContainerItemContext.withConstant(new ItemStack(Items.WATER_BUCKET)).find(FluidStorage.ITEM),
						FluidVariant::hasComponents, // Only allow components -> won't match anything.
						null
				)
		);
	}

	@Test
	public void testDoesNotThrow() {
		// Ensure this doesn't throw an error due to the empty stack.
		assertEquals(null, ContainerItemContext.withConstant(ItemStack.EMPTY).find(FluidStorage.ITEM));
	}

	@Nullable
	public static Holder<Potion> getPotion(ItemStack stack) {
		return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(null);
	}

	public static void setPotion(ItemStack itemStack, Holder<Potion> potion) {
		itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
	}
}

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

import static net.fabricmc.fabric.test.transfer.TestUtil.assertEquals;

import java.util.stream.IntStream;

import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.test.transfer.ingame.TransferTestInitializer;

/**
 * Tests for the item transfer APIs.
 */
class ItemTests extends AbstractTransferApiTest {
	public static ComponentType<Integer> ENERGY;

	@BeforeAll
	static void beforeAll() {
		bootstrap();
		ENERGY = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(TransferTestInitializer.MOD_ID, "energy"),
								ComponentType.<Integer>builder().codec(Codecs.NON_NEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build());
	}

	@Test
	public void testStackReference() {
		// Ensure that Inventory wrappers will try to mutate the backing stack as much as possible.
		// In many cases, MC code captures a reference to the ItemStack so we want to edit that stack directly
		// and not a copy whenever we can. Obviously this can't be perfect, but we try to cover as many cases as possible.
		SimpleInventory inv = new SimpleInventory(new ItemStack(Items.DIAMOND, 2));
		InventoryStorage invWrapper = InventoryStorage.of(inv, null);
		ItemStack stack = inv.getStack(0);

		// Simulate should correctly reset the stack.
		try (Transaction tx = Transaction.openOuter()) {
			invWrapper.extract(ItemVariant.of(Items.DIAMOND), 2, tx);
		}

		if (stack != inv.getStack(0)) throw new AssertionError("Stack should have stayed the same.");

		// Commit should try to edit the original stack when it is feasible to do so.
		try (Transaction tx = Transaction.openOuter()) {
			invWrapper.extract(ItemVariant.of(Items.DIAMOND), 1, tx);
			tx.commit();
		}

		if (stack != inv.getStack(0)) throw new AssertionError("Stack should have stayed the same.");

		// Also edit the stack when the item matches, even when the components and the count change.
		ItemVariant oldVariant = ItemVariant.of(Items.DIAMOND);
		ComponentChanges components = ComponentChanges.builder().add(ENERGY, 42).build();
		ItemVariant newVariant = ItemVariant.of(Items.DIAMOND, components);

		try (Transaction tx = Transaction.openOuter()) {
			invWrapper.extract(oldVariant, 2, tx);
			invWrapper.insert(newVariant, 5, tx);
			tx.commit();
		}

		if (stack != inv.getStack(0)) throw new AssertionError("Stack should have stayed the same.");
		if (!stackEquals(stack, newVariant, 5)) throw new AssertionError("Failed to update stack components or count.");
	}

	@Test
	public void testInventoryWrappers() {
		ItemVariant emptyBucket = ItemVariant.of(Items.BUCKET);
		TestSidedInventory testInventory = new TestSidedInventory();
		checkComparatorOutput(testInventory);

		// Create a few wrappers.
		InventoryStorage unsidedWrapper = InventoryStorage.of(testInventory, null);
		InventoryStorage downWrapper = InventoryStorage.of(testInventory, Direction.DOWN);
		InventoryStorage upWrapper = InventoryStorage.of(testInventory, Direction.UP);

		// Make sure querying a new wrapper returns the same one.
		if (InventoryStorage.of(testInventory, null) != unsidedWrapper) throw new AssertionError("Wrappers should be ==.");

		for (int iter = 0; iter < 2; ++iter) {
			// First time, abort.
			// Second time, commit.
			try (Transaction transaction = Transaction.openOuter()) {
				// Insert bucket from down - should fail.
				if (downWrapper.insert(emptyBucket, 1, transaction) != 0) throw new AssertionError("Bucket should not have been inserted.");
				// Insert bucket unsided - should go in slot 1 (isValid returns false for slot 0).
				if (unsidedWrapper.insert(emptyBucket, 1, transaction) != 1) throw new AssertionError("Failed to insert bucket.");
				if (!testInventory.getStack(0).isEmpty()) throw new AssertionError("Slot 0 should have been empty.");
				if (!stackEquals(testInventory.getStack(1), Items.BUCKET, 1)) throw new AssertionError("Slot 1 should have been a bucket.");
				// The bucket should be extractable from any side but the top.
				if (!emptyBucket.equals(StorageUtil.findExtractableResource(unsidedWrapper, transaction))) throw new AssertionError("Bucket should be extractable from unsided wrapper.");
				if (!emptyBucket.equals(StorageUtil.findExtractableResource(downWrapper, transaction))) throw new AssertionError("Bucket should be extractable from down wrapper.");
				if (StorageUtil.findExtractableResource(upWrapper, transaction) != null) throw new AssertionError("Bucket should NOT be extractable from up wrapper.");

				if (iter == 1) {
					// Commit the second time only.
					transaction.commit();
				}
			}
		}

		// Check commit.
		if (!testInventory.getStack(0).isEmpty()) throw new AssertionError("Slot 0 should have been empty.");
		if (!testInventory.getStack(1).isOf(Items.BUCKET) || testInventory.getStack(1).getCount() != 1) throw new AssertionError("Slot 1 should have been a bucket.");

		checkComparatorOutput(testInventory);

		// Check that we return sensible results if amount stored > capacity
		ItemStack oversizedStack = new ItemStack(Items.DIAMOND_PICKAXE, 2);
		SimpleInventory simpleInventory = new SimpleInventory(oversizedStack);
		InventoryStorage wrapper = InventoryStorage.of(simpleInventory, null);

		try (Transaction transaction = Transaction.openOuter()) {
			assertEquals(0L, wrapper.insert(ItemVariant.of(oversizedStack), 10, transaction));
			transaction.commit();
		}
	}

	@Test
	void testPacketCodec() {
		ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
		stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Custom name"));

		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		RegistryByteBuf rbuf = new RegistryByteBuf(buf, staticDrm());
		ItemVariant.PACKET_CODEC.encode(rbuf, ItemVariant.of(stack));

		ItemVariant decoded = ItemVariant.PACKET_CODEC.decode(rbuf);
		Assertions.assertTrue(ItemStack.areEqual(stack, decoded.toStack()));
	}

	private static boolean stackEquals(ItemStack stack, Item item, int count) {
		return stackEquals(stack, ItemVariant.of(item), count);
	}

	private static boolean stackEquals(ItemStack stack, ItemVariant variant, int count) {
		return variant.matches(stack) && stack.getCount() == count;
	}

	private static class TestSidedInventory extends SimpleInventory implements SidedInventory {
		private static final int[] SLOTS = IntStream.range(0, 3).toArray();

		TestSidedInventory() {
			super(SLOTS.length);
		}

		@Override
		public int[] getAvailableSlots(Direction side) {
			return SLOTS;
		}

		@Override
		public boolean isValid(int slot, ItemStack stack) {
			return slot != 0 || !stack.isOf(Items.BUCKET); // can't have buckets in slot 0.
		}

		@Override
		public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
			return dir != Direction.DOWN;
		}

		@Override
		public boolean canExtract(int slot, ItemStack stack, Direction dir) {
			return dir != Direction.UP;
		}
	}

	/**
	 * Test insertion when {@link Inventory#getMaxCountPerStack()} is the bottleneck.
	 */
	@Test
	public void testLimitedStackCountInventory() {
		ItemVariant diamond = ItemVariant.of(Items.DIAMOND);
		LimitedStackCountInventory inventory = new LimitedStackCountInventory(diamond.toStack(), diamond.toStack(), diamond.toStack());
		InventoryStorage wrapper = InventoryStorage.of(inventory, null);

		// Should only be able to insert 2 diamonds per stack * 3 stacks = 6 diamonds.
		try (Transaction transaction = Transaction.openOuter()) {
			if (wrapper.insert(diamond, 1000, transaction) != 6) {
				throw new AssertionError("Only 6 diamonds should have been inserted.");
			}

			checkComparatorOutput(inventory);
		}
	}

	/**
	 * Test insertion when {@link Item#getMaxCount()} is the bottleneck.
	 */
	@Test
	public void testLimitedStackCountItem() {
		ItemVariant diamondPickaxe = ItemVariant.of(Items.DIAMOND_PICKAXE);
		LimitedStackCountInventory inventory = new LimitedStackCountInventory(5);
		InventoryStorage wrapper = InventoryStorage.of(inventory, null);

		// Should only be able to insert 5 pickaxes, as the item limits stack counts to 1.
		try (Transaction transaction = Transaction.openOuter()) {
			if (wrapper.insert(diamondPickaxe, 1000, transaction) != 5) {
				throw new AssertionError("Only 5 pickaxes should have been inserted.");
			}

			checkComparatorOutput(inventory);
		}
	}

	private static class LimitedStackCountInventory extends SimpleInventory {
		LimitedStackCountInventory(int size) {
			super(size);
		}

		LimitedStackCountInventory(ItemStack... stacks) {
			super(stacks);
		}

		@Override
		public int getMaxCountPerStack() {
			return 3;
		}
	}

	private static void checkComparatorOutput(Inventory inventory) {
		Storage<ItemVariant> storage = InventoryStorage.of(inventory, null);

		int vanillaOutput = ScreenHandler.calculateComparatorOutput(inventory);
		int transferApiOutput = StorageUtil.calculateComparatorOutput(storage);

		if (vanillaOutput != transferApiOutput) {
			String error = String.format(
					"Vanilla and Transfer API comparator outputs should have been identical. Vanilla: %d. Transfer API: %d.",
					vanillaOutput,
					transferApiOutput
			);
			throw new AssertionError(error);
		}
	}

	/**
	 * Ensure that SimpleInventory only calls markDirty at the end of a successful transaction.
	 */
	@Test
	public void testSimpleInventoryUpdates() {
		var simpleInventory = new SimpleInventory(2) {
			boolean throwOnMarkDirty = true;
			boolean markDirtyCalled = false;

			@Override
			public void markDirty() {
				if (throwOnMarkDirty) {
					throw new AssertionError("Unexpected markDirty call!");
				}

				markDirtyCalled = true;
			}
		};
		InventoryStorage wrapper = InventoryStorage.of(simpleInventory, null);
		ItemVariant diamond = ItemVariant.of(Items.DIAMOND);

		// Simulation should not trigger notifications.
		try (Transaction tx = Transaction.openOuter()) {
			wrapper.insert(diamond, 1000, tx);
		}

		// But commit after modification should.
		try (Transaction tx = Transaction.openOuter()) {
			wrapper.insert(diamond, 1000, tx);

			simpleInventory.throwOnMarkDirty = false;
			tx.commit();
		}

		if (!simpleInventory.markDirtyCalled) {
			throw new AssertionError("markDirty should have been called when committing.");
		}
	}
}

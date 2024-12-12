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

package net.fabricmc.fabric.test.transfer.gametests;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.test.transfer.mixin.AbstractFurnaceBlockEntityAccessor;

public class VanillaStorageTests {
	/**
	 * Regression test for https://github.com/FabricMC/fabric/issues/1972.
	 * Ensures that furnace cook time is only reset when extraction is actually committed.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFurnaceCookTime(TestContext context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.FURNACE.getDefaultState());
		FurnaceBlockEntity furnace = context.getBlockEntity(pos);
		AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) furnace;

		ItemVariant rawIron = ItemVariant.of(Items.RAW_IRON);
		furnace.setStack(0, rawIron.toStack(64));
		furnace.setStack(1, new ItemStack(Items.COAL, 64));
		InventoryStorage furnaceWrapper = InventoryStorage.of(furnace, null);

		context.runAtTick(5, () -> {
			if (accessor.getCookingTimeSpent() <= 0) {
				throw new GameTestException("Furnace should have started cooking.");
			}

			try (Transaction transaction = Transaction.openOuter()) {
				if (furnaceWrapper.extract(rawIron, 64, transaction) != 64) {
					throw new GameTestException("Failed to extract 64 raw iron.");
				}
			}

			if (accessor.getCookingTimeSpent() <= 0) {
				throw new GameTestException("Furnace should still cook after simulation.");
			}

			try (Transaction transaction = Transaction.openOuter()) {
				if (furnaceWrapper.extract(rawIron, 64, transaction) != 64) {
					throw new GameTestException("Failed to extract 64 raw iron.");
				}

				transaction.commit();
			}

			if (accessor.getCookingTimeSpent() != 0) {
				throw new GameTestException("Furnace should have reset cook time after being emptied.");
			}

			context.complete();
		});
	}

	/**
	 * Tests that the passed block doesn't update adjacent comparators until the very end of a committed transaction.
	 *
	 * @param block A block with an Inventory block entity.
	 * @param variant The variant to try to insert (needs to be supported by the Inventory).
	 */
	private static void testComparatorOnInventory(TestContext context, Block block, ItemVariant variant) {
		World world = context.getWorld();

		BlockPos pos = new BlockPos(0, 2, 0);
		context.setBlockState(pos, block.getDefaultState());
		Inventory inventory = context.getBlockEntity(pos);
		InventoryStorage storage = InventoryStorage.of(inventory, null);

		BlockPos comparatorPos = new BlockPos(1, 2, 0);
		Direction comparatorFacing = context.getRotation().rotate(Direction.WEST);
		// support block under the comparator
		context.setBlockState(comparatorPos.offset(Direction.DOWN), Blocks.GREEN_WOOL.getDefaultState());
		// comparator
		context.setBlockState(comparatorPos, Blocks.COMPARATOR.getDefaultState().with(ComparatorBlock.FACING, comparatorFacing));

		try (Transaction transaction = Transaction.openOuter()) {
			if (world.getBlockTickScheduler().isQueued(context.getAbsolutePos(comparatorPos), Blocks.COMPARATOR)) {
				throw new GameTestException("Comparator should not have a tick scheduled.");
			}

			storage.insert(variant, 1000000, transaction);

			// uncommitted insert should not schedule an update
			if (world.getBlockTickScheduler().isQueued(context.getAbsolutePos(comparatorPos), Blocks.COMPARATOR)) {
				throw new GameTestException("Comparator should not have a tick scheduled.");
			}

			transaction.commit();

			// committed insert should schedule an update
			if (!world.getBlockTickScheduler().isQueued(context.getAbsolutePos(comparatorPos), Blocks.COMPARATOR)) {
				throw new GameTestException("Comparator should have a tick scheduled.");
			}
		}

		context.complete();
	}

	/**
	 * Tests that containers such as chests don't update adjacent comparators until the very end of a committed transaction.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testChestComparator(TestContext context) {
		testComparatorOnInventory(context, Blocks.CHEST, ItemVariant.of(Items.DIAMOND));
	}

	/**
	 * Same as {@link #testChestComparator} but for chiseled bookshelves, because their implementation is very... strange.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testChiseledBookshelfComparator(TestContext context) {
		testComparatorOnInventory(context, Blocks.CHISELED_BOOKSHELF, ItemVariant.of(Items.BOOK));
	}

	/**
	 * Test for chiseled bookshelves, because their implementation is very... strange.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testChiseledBookshelf(TestContext context) {
		ItemVariant book = ItemVariant.of(Items.BOOK);

		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.CHISELED_BOOKSHELF.getDefaultState());
		ChiseledBookshelfBlockEntity bookshelf = context.getBlockEntity(pos);
		InventoryStorage storage = InventoryStorage.of(bookshelf, null);

		// First, check that we can correctly undo insert operations, because vanilla's setStack doesn't permit it without our patches.
		try (Transaction transaction = Transaction.openOuter()) {
			if (storage.insert(book, 2, transaction) != 2) throw new GameTestException("Should have inserted 2 books");

			if (bookshelf.getStack(0).getCount() != 1) throw new GameTestException("Bookshelf stack 0 should have size 1");
			if (!book.matches(bookshelf.getStack(0))) throw new GameTestException("Bookshelf stack 0 should be a book");
			if (bookshelf.getStack(1).getCount() != 1) throw new GameTestException("Bookshelf stack 1 should have size 1");
			if (!book.matches(bookshelf.getStack(1))) throw new GameTestException("Bookshelf stack 1 should be a book");
		}

		if (!bookshelf.getStack(0).isEmpty()) throw new GameTestException("Bookshelf stack 0 should be empty again after aborting transaction");
		if (!bookshelf.getStack(1).isEmpty()) throw new GameTestException("Bookshelf stack 1 should be empty again after aborting transaction");

		// Second, check that we correctly update the last modified slot.
		try (Transaction tx = Transaction.openOuter()) {
			if (storage.getSlot(1).insert(book, 1, tx) != 1) throw new GameTestException("Should have inserted 1 book");
			if (bookshelf.getLastInteractedSlot() != 1) throw new GameTestException("Last modified slot should be 1");

			if (storage.getSlot(2).insert(book, 1, tx) != 1) throw new GameTestException("Should have inserted 1 book");
			if (bookshelf.getLastInteractedSlot() != 2) throw new GameTestException("Last modified slot should be 2");

			if (storage.getSlot(1).extract(book, 1, tx) != 1) throw new GameTestException("Should have extracted 1 book");
			if (bookshelf.getLastInteractedSlot() != 1) throw new GameTestException("Last modified slot should be 1");

			// Now, create an aborted nested transaction.
			try (Transaction nested = tx.openNested()) {
				if (storage.insert(book, 100, nested) != 5) throw new GameTestException("Should have inserted 5 books");
				// Now, last modified slot should be 5.
				if (bookshelf.getLastInteractedSlot() != 5) throw new GameTestException("Last modified slot should be 5");
			}

			// And it's back to 1 in theory.
			if (bookshelf.getLastInteractedSlot() != 1) throw new GameTestException("Last modified slot should be 1");
			tx.commit();
		}

		if (bookshelf.getLastInteractedSlot() != 1) throw new GameTestException("Last modified slot should be 1 after committing transaction");

		// Let's also check the state properties. Only slot 2 should be occupied.
		BlockState state = bookshelf.getCachedState();

		if (state.get(Properties.SLOT_0_OCCUPIED)) throw new GameTestException("Slot 0 should not be occupied");
		if (state.get(Properties.SLOT_1_OCCUPIED)) throw new GameTestException("Slot 1 should not be occupied");
		if (!state.get(Properties.SLOT_2_OCCUPIED)) throw new GameTestException("Slot 2 should be occupied");
		if (state.get(Properties.SLOT_3_OCCUPIED)) throw new GameTestException("Slot 3 should not be occupied");
		if (state.get(Properties.SLOT_4_OCCUPIED)) throw new GameTestException("Slot 4 should not be occupied");
		if (state.get(Properties.SLOT_5_OCCUPIED)) throw new GameTestException("Slot 5 should not be occupied");

		context.complete();
	}

	/**
	 * Tests that shulker boxes cannot be inserted into other shulker boxes.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testShulkerNoInsert(TestContext context) {
		BlockPos pos = new BlockPos(0, 2, 0);
		context.setBlockState(pos, Blocks.SHULKER_BOX);
		ShulkerBoxBlockEntity shulker = context.getBlockEntity(pos);
		InventoryStorage storage = InventoryStorage.of(shulker, null);

		if (StorageUtil.simulateInsert(storage, ItemVariant.of(Items.SHULKER_BOX), 1, null) > 0) {
			context.throwPositionedException("Expected shulker box to be rejected", pos);
		}

		context.complete();
	}

	/**
	 * {@link Inventory#isValid(int, ItemStack)} is supposed to be independent of the stack size.
	 * However, to limit some stackable inputs to a size of 1, brewing stands and furnaces don't follow this rule in all cases.
	 * This test ensures that the Transfer API works around this issue for furnaces.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBadFurnaceIsValid(TestContext context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.FURNACE.getDefaultState());
		FurnaceBlockEntity furnace = context.getBlockEntity(pos);
		InventoryStorage furnaceWrapper = InventoryStorage.of(furnace, null);

		try (Transaction tx = Transaction.openOuter()) {
			if (furnaceWrapper.getSlot(1).insert(ItemVariant.of(Items.BUCKET), 2, tx) != 1) {
				throw new GameTestException("Exactly 1 bucket should have been inserted");
			}
		}

		context.complete();
	}

	/**
	 * Same as {@link #testBadFurnaceIsValid(TestContext)}, but for brewing stands.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBadBrewingStandIsValid(TestContext context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.BREWING_STAND.getDefaultState());
		BrewingStandBlockEntity brewingStand = context.getBlockEntity(pos);
		InventoryStorage brewingStandWrapper = InventoryStorage.of(brewingStand, null);

		try (Transaction tx = Transaction.openOuter()) {
			for (int bottleSlot = 0; bottleSlot < 3; ++bottleSlot) {
				if (brewingStandWrapper.getSlot(bottleSlot).insert(ItemVariant.of(Items.GLASS_BOTTLE), 2, tx) != 1) {
					throw new GameTestException("Exactly 1 glass bottle should have been inserted");
				}
			}

			if (brewingStandWrapper.getSlot(3).insert(ItemVariant.of(Items.REDSTONE), 2, tx) != 2) {
				throw new GameTestException("Brewing ingredient insertion should not be limited");
			}
		}

		context.complete();
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/2810">double chest wrapper only updating modified halves</a>.
	 */
	@GameTest(templateName = "fabric-transfer-api-v1-testmod:double_chest_comparators", skyAccess = true)
	public void testDoubleChestComparator(TestContext context) {
		BlockPos chestPos = new BlockPos(2, 1, 2);
		Storage<ItemVariant> storage = ItemStorage.SIDED.find(context.getWorld(), context.getAbsolutePos(chestPos), Direction.UP);
		context.assertTrue(storage != null, "Storage must not be null");

		// Insert one item
		try (Transaction tx = Transaction.openOuter()) {
			context.assertTrue(storage.insert(ItemVariant.of(Items.DIAMOND), 1, tx) == 1, "Diamond should have been inserted");
			tx.commit();
		}

		// Check that the inventory and slotted storages match
		Inventory inventory = HopperBlockEntity.getInventoryAt(context.getWorld(), context.getAbsolutePos(chestPos));
		context.assertTrue(inventory != null, "Inventory must not be null");

		if (!(storage instanceof SlottedStorage<ItemVariant> slottedStorage)) {
			throw new GameTestException("Double chest storage must be a SlottedStorage");
		}

		for (int i = 0; i < inventory.size(); ++i) {
			ItemStack stack = inventory.getStack(i);
			ItemVariant variant = ItemVariant.of(stack.getItem());
			context.assertTrue(variant.matches(stack), "Item variant in slot " + i + " must match stack");
			long expectedCount = stack.getCount();
			long actualCount = slottedStorage.getSlot(i).getAmount();
			context.assertTrue(expectedCount == actualCount, "Slot " + i + " should have " + expectedCount + " items, but has " + actualCount);
		}

		// Check that an update is queued for every single comparator
		MutableInt comparatorCount = new MutableInt();

		context.forEachRelativePos(relativePos -> {
			if (context.getBlockState(relativePos).getBlock() != Blocks.COMPARATOR) {
				return;
			}

			comparatorCount.increment();

			if (!context.getWorld().getBlockTickScheduler().isQueued(context.getAbsolutePos(relativePos), Blocks.COMPARATOR)) {
				throw new GameTestException("Comparator at " + relativePos + " should have an update scheduled");
			}
		});

		context.assertTrue(comparatorCount.intValue() == 6, "Expected exactly 6 comparators");

		context.complete();
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/3017">composters not always incrementing their level on the first insert</a>.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testComposterFirstInsert(TestContext context) {
		BlockPos pos = new BlockPos(0, 1, 0);

		ItemVariant carrot = ItemVariant.of(Items.CARROT);

		for (int i = 0; i < 200; ++i) { // Run many times as this can be random.
			context.setBlockState(pos, Blocks.COMPOSTER.getDefaultState());
			Storage<ItemVariant> storage = ItemStorage.SIDED.find(context.getWorld(), context.getAbsolutePos(pos), Direction.UP);

			try (Transaction tx = Transaction.openOuter()) {
				if (storage.insert(carrot, 1, tx) != 1) {
					context.throwPositionedException("Carrot should have been inserted", pos);
				}

				tx.commit();
			}

			context.checkBlockState(pos, state -> state.get(ComposterBlock.LEVEL) == 1, () -> "Composter should have level 1");
		}

		context.complete();
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/3485">jukeboxes having their state changed mid-transaction</a>.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testJukeboxState(TestContext context) {
		BlockPos pos = new BlockPos(2, 2, 2);
		context.setBlockState(pos, Blocks.JUKEBOX.getDefaultState());
		Storage<ItemVariant> storage = ItemStorage.SIDED.find(context.getWorld(), context.getAbsolutePos(pos), Direction.UP);

		try (Transaction tx = Transaction.openOuter()) {
			storage.insert(ItemVariant.of(Items.MUSIC_DISC_11), 1, tx);
			context.checkBlockState(pos, state -> !state.get(JukeboxBlock.HAS_RECORD), () -> "Jukebox should not have its state changed mid-transaction");
			tx.commit();
		}

		context.checkBlockState(pos, state -> state.get(JukeboxBlock.HAS_RECORD), () -> "Jukebox should have its state changed");
		context.complete();
	}
}

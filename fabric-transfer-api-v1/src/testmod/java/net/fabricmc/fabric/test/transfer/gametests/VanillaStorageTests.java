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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
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
	@GameTest
	public void testFurnaceCookTime(GameTestHelper context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlock(pos, Blocks.FURNACE.defaultBlockState());
		FurnaceBlockEntity furnace = context.getBlockEntity(pos, FurnaceBlockEntity.class);
		AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) furnace;

		ItemVariant rawIron = ItemVariant.of(Items.RAW_IRON);
		furnace.setItem(0, rawIron.toStack(64));
		furnace.setItem(1, new ItemStack(Items.COAL, 64));
		InventoryStorage furnaceWrapper = InventoryStorage.of(furnace, null);

		context.runAtTickTime(5, () -> {
			if (accessor.getCookingTimeSpent() <= 0) {
				throw context.assertionException("Furnace should have started cooking.");
			}

			try (Transaction transaction = Transaction.openOuter()) {
				if (furnaceWrapper.extract(rawIron, 64, transaction) != 64) {
					throw context.assertionException("Failed to extract 64 raw iron.");
				}
			}

			if (accessor.getCookingTimeSpent() <= 0) {
				throw context.assertionException("Furnace should still cook after simulation.");
			}

			try (Transaction transaction = Transaction.openOuter()) {
				if (furnaceWrapper.extract(rawIron, 64, transaction) != 64) {
					throw context.assertionException("Failed to extract 64 raw iron.");
				}

				transaction.commit();
			}

			if (accessor.getCookingTimeSpent() != 0) {
				throw context.assertionException("Furnace should have reset cook time after being emptied.");
			}

			context.succeed();
		});
	}

	/**
	 * Tests that the passed block doesn't update adjacent comparators until the very end of a committed transaction.
	 *
	 * @param block A block with an Inventory block entity.
	 * @param variant The variant to try to insert (needs to be supported by the Inventory).
	 */
	private static <T extends BlockEntity & Container> void testComparatorOnInventory(GameTestHelper context, Block block, ItemVariant variant, Class<T> inventoryClass) {
		Level world = context.getLevel();

		BlockPos pos = new BlockPos(0, 2, 0);
		// Shelf comparator output is directional
		context.setBlock(pos, block.defaultBlockState().trySetValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
		T inventory = context.getBlockEntity(pos, inventoryClass);
		InventoryStorage storage = InventoryStorage.of(inventory, null);

		BlockPos comparatorPos = new BlockPos(1, 2, 0);
		Direction comparatorFacing = context.getTestRotation().rotate(Direction.WEST);
		// support block under the comparator
		context.setBlock(comparatorPos.relative(Direction.DOWN), Blocks.GREEN_WOOL.defaultBlockState());
		// comparator
		context.setBlock(comparatorPos, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, comparatorFacing));

		try (Transaction transaction = Transaction.openOuter()) {
			if (world.getBlockTicks().hasScheduledTick(context.absolutePos(comparatorPos), Blocks.COMPARATOR)) {
				throw context.assertionException("Comparator should not have a tick scheduled.");
			}

			storage.insert(variant, 1000000, transaction);

			// uncommitted insert should not schedule an update
			if (world.getBlockTicks().hasScheduledTick(context.absolutePos(comparatorPos), Blocks.COMPARATOR)) {
				throw context.assertionException("Comparator should not have a tick scheduled.");
			}

			transaction.commit();

			// committed insert should schedule an update
			if (!world.getBlockTicks().hasScheduledTick(context.absolutePos(comparatorPos), Blocks.COMPARATOR)) {
				throw context.assertionException("Comparator should have a tick scheduled.");
			}
		}

		context.succeed();
	}

	/**
	 * Tests that containers such as chests don't update adjacent comparators until the very end of a committed transaction.
	 */
	@GameTest
	public void testChestComparator(GameTestHelper context) {
		testComparatorOnInventory(context, Blocks.CHEST, ItemVariant.of(Items.DIAMOND), ChestBlockEntity.class);
	}

	/**
	 * Same as {@link #testChestComparator} but for shelves.
	 */
	@GameTest
	public void testShelfComparator(GameTestHelper context) {
		testComparatorOnInventory(context, Blocks.OAK_SHELF, ItemVariant.of(Items.DIAMOND), ShelfBlockEntity.class);
	}

	/**
	 * Same as {@link #testChestComparator} but for chiseled bookshelves, because their implementation is very... strange.
	 */
	@GameTest
	public void testChiseledBookshelfComparator(GameTestHelper context) {
		testComparatorOnInventory(context, Blocks.CHISELED_BOOKSHELF, ItemVariant.of(Items.BOOK), ChiseledBookShelfBlockEntity.class);
	}

	/**
	 * Test for chiseled bookshelves, because their implementation is very... strange.
	 */
	@GameTest
	public void testChiseledBookshelf(GameTestHelper context) {
		ItemVariant book = ItemVariant.of(Items.BOOK);

		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlock(pos, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
		ChiseledBookShelfBlockEntity bookshelf = context.getBlockEntity(pos, ChiseledBookShelfBlockEntity.class);
		InventoryStorage storage = InventoryStorage.of(bookshelf, null);

		// First, check that we can correctly undo insert operations, because vanilla's setStack doesn't permit it without our patches.
		try (Transaction transaction = Transaction.openOuter()) {
			if (storage.insert(book, 2, transaction) != 2) throw context.assertionException("Should have inserted 2 books");

			if (bookshelf.getItem(0).getCount() != 1) throw context.assertionException("Bookshelf stack 0 should have size 1");
			if (!book.matches(bookshelf.getItem(0))) throw context.assertionException("Bookshelf stack 0 should be a book");
			if (bookshelf.getItem(1).getCount() != 1) throw context.assertionException("Bookshelf stack 1 should have size 1");
			if (!book.matches(bookshelf.getItem(1))) throw context.assertionException("Bookshelf stack 1 should be a book");
		}

		if (!bookshelf.getItem(0).isEmpty()) throw context.assertionException("Bookshelf stack 0 should be empty again after aborting transaction");
		if (!bookshelf.getItem(1).isEmpty()) throw context.assertionException("Bookshelf stack 1 should be empty again after aborting transaction");

		// Second, check that we correctly update the last modified slot.
		try (Transaction tx = Transaction.openOuter()) {
			if (storage.getSlot(1).insert(book, 1, tx) != 1) throw context.assertionException("Should have inserted 1 book");
			if (bookshelf.getLastInteractedSlot() != 1) throw context.assertionException("Last modified slot should be 1");

			if (storage.getSlot(2).insert(book, 1, tx) != 1) throw context.assertionException("Should have inserted 1 book");
			if (bookshelf.getLastInteractedSlot() != 2) throw context.assertionException("Last modified slot should be 2");

			if (storage.getSlot(1).extract(book, 1, tx) != 1) throw context.assertionException("Should have extracted 1 book");
			if (bookshelf.getLastInteractedSlot() != 1) throw context.assertionException("Last modified slot should be 1");

			// Now, create an aborted nested transaction.
			try (Transaction nested = tx.openNested()) {
				if (storage.insert(book, 100, nested) != 5) throw context.assertionException("Should have inserted 5 books");
				// Now, last modified slot should be 5.
				if (bookshelf.getLastInteractedSlot() != 5) throw context.assertionException("Last modified slot should be 5");
			}

			// And it's back to 1 in theory.
			if (bookshelf.getLastInteractedSlot() != 1) throw context.assertionException("Last modified slot should be 1");
			tx.commit();
		}

		if (bookshelf.getLastInteractedSlot() != 1) throw context.assertionException("Last modified slot should be 1 after committing transaction");

		// Let's also check the state properties. Only slot 2 should be occupied.
		BlockState state = bookshelf.getBlockState();

		if (state.getValue(BlockStateProperties.SLOT_0_OCCUPIED)) throw context.assertionException("Slot 0 should not be occupied");
		if (state.getValue(BlockStateProperties.SLOT_1_OCCUPIED)) throw context.assertionException("Slot 1 should not be occupied");
		if (!state.getValue(BlockStateProperties.SLOT_2_OCCUPIED)) throw context.assertionException("Slot 2 should be occupied");
		if (state.getValue(BlockStateProperties.SLOT_3_OCCUPIED)) throw context.assertionException("Slot 3 should not be occupied");
		if (state.getValue(BlockStateProperties.SLOT_4_OCCUPIED)) throw context.assertionException("Slot 4 should not be occupied");
		if (state.getValue(BlockStateProperties.SLOT_5_OCCUPIED)) throw context.assertionException("Slot 5 should not be occupied");

		context.succeed();
	}

	/**
	 * Tests that shulker boxes cannot be inserted into other shulker boxes.
	 */
	@GameTest
	public void testShulkerNoInsert(GameTestHelper context) {
		BlockPos pos = new BlockPos(0, 2, 0);
		context.setBlock(pos, Blocks.SHULKER_BOX);
		ShulkerBoxBlockEntity shulker = context.getBlockEntity(pos, ShulkerBoxBlockEntity.class);
		InventoryStorage storage = InventoryStorage.of(shulker, null);

		if (StorageUtil.simulateInsert(storage, ItemVariant.of(Items.SHULKER_BOX), 1, null) > 0) {
			context.fail(Component.literal("Expected shulker box to be rejected"), pos);
		}

		context.succeed();
	}

	/**
	 * {@link Container#isValid(int, ItemStack)} is supposed to be independent of the stack size.
	 * However, to limit some stackable inputs to a size of 1, brewing stands and furnaces don't follow this rule in all cases.
	 * This test ensures that the Transfer API works around this issue for furnaces.
	 */
	@GameTest
	public void testBadFurnaceIsValid(GameTestHelper context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlock(pos, Blocks.FURNACE.defaultBlockState());
		FurnaceBlockEntity furnace = context.getBlockEntity(pos, FurnaceBlockEntity.class);
		InventoryStorage furnaceWrapper = InventoryStorage.of(furnace, null);

		try (Transaction tx = Transaction.openOuter()) {
			if (furnaceWrapper.getSlot(1).insert(ItemVariant.of(Items.BUCKET), 2, tx) != 1) {
				throw context.assertionException("Exactly 1 bucket should have been inserted");
			}
		}

		context.succeed();
	}

	/**
	 * Same as {@link #testBadFurnaceIsValid(GameTestHelper)}, but for brewing stands.
	 */
	@GameTest
	public void testBadBrewingStandIsValid(GameTestHelper context) {
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlock(pos, Blocks.BREWING_STAND.defaultBlockState());
		BrewingStandBlockEntity brewingStand = context.getBlockEntity(pos, BrewingStandBlockEntity.class);
		InventoryStorage brewingStandWrapper = InventoryStorage.of(brewingStand, null);

		try (Transaction tx = Transaction.openOuter()) {
			for (int bottleSlot = 0; bottleSlot < 3; ++bottleSlot) {
				if (brewingStandWrapper.getSlot(bottleSlot).insert(ItemVariant.of(Items.GLASS_BOTTLE), 2, tx) != 1) {
					throw context.assertionException("Exactly 1 glass bottle should have been inserted");
				}
			}

			if (brewingStandWrapper.getSlot(3).insert(ItemVariant.of(Items.REDSTONE), 2, tx) != 2) {
				throw context.assertionException("Brewing ingredient insertion should not be limited");
			}
		}

		context.succeed();
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/2810">double chest wrapper only updating modified halves</a>.
	 */
	@GameTest(structure = "fabric-transfer-api-v1-testmod:double_chest_comparators", skyAccess = true)
	public void testDoubleChestComparator(GameTestHelper context) {
		BlockPos chestPos = new BlockPos(2, 1, 2);
		Storage<ItemVariant> storage = ItemStorage.SIDED.find(context.getLevel(), context.absolutePos(chestPos), Direction.UP);
		context.assertTrue(storage != null, Component.literal("Storage must not be null"));

		// Insert one item
		try (Transaction tx = Transaction.openOuter()) {
			context.assertTrue(storage.insert(ItemVariant.of(Items.DIAMOND), 1, tx) == 1, Component.literal("Diamond should have been inserted"));
			tx.commit();
		}

		// Check that the inventory and slotted storages match
		Container inventory = HopperBlockEntity.getContainerAt(context.getLevel(), context.absolutePos(chestPos));
		context.assertTrue(inventory != null, Component.literal("Inventory must not be null"));

		if (!(storage instanceof SlottedStorage<ItemVariant> slottedStorage)) {
			throw context.assertionException("Double chest storage must be a SlottedStorage");
		}

		for (int i = 0; i < inventory.getContainerSize(); ++i) {
			ItemStack stack = inventory.getItem(i);
			ItemVariant variant = ItemVariant.of(stack.getItem());
			context.assertTrue(variant.matches(stack), Component.literal("Item variant in slot " + i + " must match stack"));
			long expectedCount = stack.getCount();
			long actualCount = slottedStorage.getSlot(i).getAmount();
			context.assertTrue(expectedCount == actualCount, Component.literal("Slot " + i + " should have " + expectedCount + " items, but has " + actualCount));
		}

		// Check that an update is queued for every single comparator
		MutableInt comparatorCount = new MutableInt();

		context.forEveryBlockInStructure(relativePos -> {
			if (context.getBlockState(relativePos).getBlock() != Blocks.COMPARATOR) {
				return;
			}

			comparatorCount.increment();

			if (!context.getLevel().getBlockTicks().hasScheduledTick(context.absolutePos(relativePos), Blocks.COMPARATOR)) {
				throw context.assertionException("Comparator at " + relativePos + " should have an update scheduled");
			}
		});

		context.assertTrue(comparatorCount.intValue() == 6, Component.literal("Expected exactly 6 comparators"));

		context.succeed();
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/3017">composters not always incrementing their level on the first insert</a>.
	 */
	@GameTest
	public void testComposterFirstInsert(GameTestHelper context) {
		BlockPos pos = new BlockPos(0, 1, 0);

		ItemVariant carrot = ItemVariant.of(Items.CARROT);

		for (int i = 0; i < 200; ++i) { // Run many times as this can be random.
			context.setBlock(pos, Blocks.COMPOSTER.defaultBlockState());
			Storage<ItemVariant> storage = ItemStorage.SIDED.find(context.getLevel(), context.absolutePos(pos), Direction.UP);

			try (Transaction tx = Transaction.openOuter()) {
				if (storage.insert(carrot, 1, tx) != 1) {
					context.fail(Component.literal("Carrot should have been inserted"), pos);
				}

				tx.commit();
			}

			context.assertBlockState(pos, state -> state.getValue(ComposterBlock.LEVEL) == 1, (s) -> Component.literal("Composter should have level 1"));
		}

		context.succeed();
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/3485">jukeboxes having their state changed mid-transaction</a>.
	 */
	@GameTest
	public void testJukeboxState(GameTestHelper context) {
		BlockPos pos = new BlockPos(2, 2, 2);
		context.setBlock(pos, Blocks.JUKEBOX.defaultBlockState());
		Storage<ItemVariant> storage = ItemStorage.SIDED.find(context.getLevel(), context.absolutePos(pos), Direction.UP);

		try (Transaction tx = Transaction.openOuter()) {
			storage.insert(ItemVariant.of(Items.MUSIC_DISC_11), 1, tx);
			context.assertBlockState(pos, state -> !state.getValue(JukeboxBlock.HAS_RECORD), (b) -> Component.literal("Jukebox should not have its state changed mid-transaction"));
			tx.commit();
		}

		context.assertBlockState(pos, state -> state.getValue(JukeboxBlock.HAS_RECORD), (b) -> Component.literal("Jukebox should have its state changed"));
		context.succeed();
	}
}

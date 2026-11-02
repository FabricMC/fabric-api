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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

class BundleContentsStorageTests extends AbstractTransferApiTest {
	@BeforeAll
	static void beforeAll() {
		bootstrap();
	}

	@Test
	public void partialExtraction() {
		SimpleContainer container = createBundleContainer(2);
		Storage<ItemVariant> storage = findStorage(container);
		ItemVariant diamond = ItemVariant.of(Items.DIAMOND);

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(1, storage.extract(diamond, 1, tx));
			tx.commit();
		}

		BundleContents singleton = new BundleContents(List.of(new ItemStackTemplate(Items.DIAMOND, 1)));
		assertEquals(singleton, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(1, storage.extract(diamond, 1, tx));
			assertEquals(BundleContents.EMPTY, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));
		}

		assertEquals(singleton, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(1, storage.extract(diamond, Long.MAX_VALUE, tx));
			tx.commit();
		}

		assertEquals(BundleContents.EMPTY, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));
	}

	@Test
	public void zeroExtraction() {
		SimpleContainer container = createBundleContainer(1);
		Storage<ItemVariant> storage = findStorage(container);
		BundleContents originalContents = container.getItem(0).get(DataComponents.BUNDLE_CONTENTS);
		ItemVariant diamond = ItemVariant.of(Items.DIAMOND);

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(0, storage.extract(diamond, 0, tx));
			tx.commit();
		}

		assertSame(originalContents, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(0, storage.iterator().next().extract(diamond, 0, tx));
			tx.commit();
		}

		assertSame(originalContents, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));
	}

	@Test
	public void fullDestination() {
		SimpleContainer container = createBundleContainer(1);
		Storage<ItemVariant> storage = findStorage(container);
		SimpleContainer destination = new SimpleContainer(new ItemStack(Items.DIAMOND, 64));
		BundleContents originalContents = container.getItem(0).get(DataComponents.BUNDLE_CONTENTS);

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(0, StorageUtil.move(storage, ContainerStorage.of(destination, null), variant -> true, 1, tx));
			tx.commit();
		}

		assertEquals(originalContents, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));
	}

	@Test
	public void emptyBundle() {
		SimpleContainer container = createBundleContainer();
		Storage<ItemVariant> storage = findStorage(container);
		assertFalse(storage.iterator().hasNext());

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(0, storage.extract(ItemVariant.of(Items.DIAMOND), 1, tx));
			tx.commit();
		}
	}

	@Test
	public void mixedInsertion() {
		SimpleContainer container = createBundleContainer();
		Storage<ItemVariant> storage = findStorage(container);
		ItemVariant diamond = ItemVariant.of(Items.DIAMOND);
		ItemVariant pearl = ItemVariant.of(Items.ENDER_PEARL);

		try (Transaction tx = Transaction.openOuter()) {
			storage.insert(diamond, 16, tx);
			storage.insert(diamond, 16, tx);
			assertEquals(8, storage.insert(pearl, Long.MAX_VALUE, tx));
			assertEquals(0, storage.insert(diamond, 1, tx));
			tx.commit();
		}

		BundleContents expected = new BundleContents(List.of(
				new ItemStackTemplate(Items.ENDER_PEARL, 8),
				new ItemStackTemplate(Items.DIAMOND, 32)));
		assertEquals(expected, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));

		Iterator<StorageView<ItemVariant>> views = storage.iterator();
		StorageView<ItemVariant> pearlView = views.next();
		assertEquals(8, pearlView.getCapacity());
		StorageView<ItemVariant> diamondView = views.next();
		assertEquals(32, diamondView.getCapacity());
	}

	@Test
	public void unstackableItem() {
		SimpleContainer container = createBundleContainer();
		Storage<ItemVariant> storage = findStorage(container);
		ItemVariant sword = ItemVariant.of(Items.DIAMOND_SWORD);

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(1, storage.insert(sword, Long.MAX_VALUE, tx));
			assertEquals(0, storage.insert(ItemVariant.of(Items.DIAMOND), 1, tx));
			tx.commit();
		}

		StorageView<ItemVariant> view = storage.iterator().next();
		assertEquals(1, view.getCapacity());
	}

	@Test
	public void nestedBundle() {
		SimpleContainer container = createBundleContainer();
		Storage<ItemVariant> storage = findStorage(container);
		ItemVariant nestedBundle = ItemVariant.of(createBundleContainer(4).getItem(0));

		try (Transaction tx = Transaction.openOuter()) {
			storage.insert(nestedBundle, 1, tx);
			assertEquals(56, storage.insert(ItemVariant.of(Items.DIAMOND), Long.MAX_VALUE, tx));
			tx.commit();
		}

		try (Transaction tx = Transaction.openOuter()) {
			storage.extract(nestedBundle, 1, tx);
			assertEquals(8, storage.insert(ItemVariant.of(Items.DIAMOND), Long.MAX_VALUE, tx));
			tx.commit();
		}

		assertEquals(new BundleContents(List.of(new ItemStackTemplate(Items.DIAMOND, 64))),
				container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));
	}

	@Test
	public void rejectedOperations() {
		SimpleContainer container = createBundleContainer(1);
		Storage<ItemVariant> storage = findStorage(container);
		BundleContents originalContents = container.getItem(0).get(DataComponents.BUNDLE_CONTENTS);
		StorageView<ItemVariant> view = storage.iterator().next();
		ItemVariant diamond = ItemVariant.of(Items.DIAMOND);
		ItemVariant stone = ItemVariant.of(Items.STONE);

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(0, storage.insert(diamond, 0, tx));
			assertEquals(0, storage.insert(ItemVariant.of(Items.SHULKER_BOX), 1, tx));
			assertEquals(0, storage.extract(stone, 1, tx));
			assertEquals(0, view.extract(stone, 1, tx));
			assertThrows(IllegalArgumentException.class, () -> storage.insert(ItemVariant.blank(), 1, tx));
			assertThrows(IllegalArgumentException.class, () -> storage.insert(diamond, -1, tx));
			assertThrows(IllegalArgumentException.class, () -> storage.extract(diamond, -1, tx));
			assertThrows(IllegalArgumentException.class, () -> view.extract(diamond, -1, tx));
			tx.commit();
		}

		assertSame(originalContents, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));
	}

	@Test
	public void itemComponents() {
		ItemStack namedDiamond = new ItemStack(Items.DIAMOND, 2);
		namedDiamond.set(DataComponents.CUSTOM_NAME, Component.literal("Named diamond"));
		SimpleContainer container = createBundleContainer(
				new ItemStackTemplate(Items.DIAMOND, 3),
				ItemStackTemplate.fromNonEmptyStack(namedDiamond));
		Component bundleName = Component.literal("Named bundle");
		container.getItem(0).set(DataComponents.CUSTOM_NAME, bundleName);
		Storage<ItemVariant> storage = findStorage(container);
		ItemVariant namedVariant = ItemVariant.of(namedDiamond);

		try (Transaction tx = Transaction.openOuter()) {
			storage.extract(namedVariant, 1, tx);
			tx.commit();
		}

		BundleContents expected = new BundleContents(List.of(
				new ItemStackTemplate(Items.DIAMOND, 3),
				ItemStackTemplate.fromNonEmptyStack(namedDiamond, 1)));
		assertEquals(expected, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));

		try (Transaction tx = Transaction.openOuter()) {
			storage.insert(namedVariant, 2, tx);
			assertEquals(3, storage.extract(ItemVariant.of(Items.DIAMOND), Long.MAX_VALUE, tx));
			tx.commit();
		}

		expected = new BundleContents(List.of(ItemStackTemplate.fromNonEmptyStack(namedDiamond, 3)));
		assertEquals(expected, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));
		assertEquals(bundleName, container.getItem(0).get(DataComponents.CUSTOM_NAME));
	}

	@Test
	public void nestedTransactions() {
		SimpleContainer container = createBundleContainer();
		Storage<ItemVariant> storage = findStorage(container);
		ItemVariant diamond = ItemVariant.of(Items.DIAMOND);

		try (Transaction tx = Transaction.openOuter()) {
			storage.insert(diamond, 5, tx);

			try (Transaction nested = tx.openNested()) {
				assertEquals(2, storage.extract(diamond, 2, nested));
				nested.commit();
			}
		}

		assertEquals(BundleContents.EMPTY, container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));

		try (Transaction tx = Transaction.openOuter()) {
			storage.insert(diamond, 5, tx);

			try (Transaction nested = tx.openNested()) {
				assertEquals(1, storage.insert(diamond, 1, nested));
			}

			tx.commit();
		}

		assertEquals(new BundleContents(List.of(new ItemStackTemplate(Items.DIAMOND, 5))),
				container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));
	}

	@Test
	public void storageViews() {
		SimpleContainer container = createBundleContainer(new ItemStackTemplate(Items.DIAMOND, 2), new ItemStackTemplate(Items.STONE, 1));
		Storage<ItemVariant> storage = findStorage(container);
		Iterator<StorageView<ItemVariant>> views = storage.iterator();
		StorageView<ItemVariant> diamondView = views.next();
		StorageView<ItemVariant> stoneView = views.next();
		assertEquals(ItemVariant.of(Items.DIAMOND), diamondView.getResource());
		assertEquals(2, diamondView.getAmount());
		assertEquals(63, diamondView.getCapacity());

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(1, stoneView.extract(ItemVariant.of(Items.STONE), 1, tx));
			tx.commit();
		}

		assertTrue(stoneView.isResourceBlank());
		assertEquals(0, stoneView.getAmount());
		assertEquals(64, diamondView.getCapacity());

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(0, stoneView.extract(ItemVariant.of(Items.STONE), 1, tx));
			storage.insert(ItemVariant.of(Items.ENDER_PEARL), 1, tx);
			tx.commit();
		}

		views = storage.iterator();
		assertEquals(ItemVariant.of(Items.ENDER_PEARL), views.next().getResource());
		assertEquals(ItemVariant.of(Items.DIAMOND), views.next().getResource());
		assertFalse(views.hasNext());
	}

	@Test
	public void replacedBundle() {
		SimpleContainer container = createBundleContainer(1);
		Storage<ItemVariant> storage = findStorage(container);
		StorageView<ItemVariant> view = storage.iterator().next();
		ItemStack replacement = new ItemStack(Items.STONE);
		container.setItem(0, replacement);

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(0, storage.insert(ItemVariant.of(Items.DIAMOND), 1, tx));
			assertEquals(0, storage.extract(ItemVariant.of(Items.DIAMOND), 1, tx));
			assertEquals(0, view.extract(ItemVariant.of(Items.DIAMOND), 1, tx));
			tx.commit();
		}

		assertEquals(ItemVariant.of(Items.STONE), ItemVariant.of(container.getItem(0)));
	}

	@Test
	public void failedExchange() {
		ItemStack bundle = createBundleContainer(2).getItem(0);
		var source = new SingleStackStorage() {
			private ItemStack stack = bundle;

			@Override
			protected ItemStack getStack() {
				return stack;
			}

			@Override
			protected void setStack(ItemStack stack) {
				this.stack = stack;
			}

			@Override
			protected boolean canInsert(ItemVariant variant) {
				return false;
			}
		};
		Storage<ItemVariant> storage = ContainerItemContext.ofSingleSlot(source).find(ItemStorage.ITEM);
		ItemVariant originalVariant = source.getResource();

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(0, storage.insert(ItemVariant.of(Items.DIAMOND), 1, tx));
			assertEquals(0, storage.extract(ItemVariant.of(Items.DIAMOND), 1, tx));
			tx.commit();
		}

		assertEquals(originalVariant, source.getResource());
		assertEquals(1, source.getAmount());
	}

	@Test
	public void partialTransfer() {
		SimpleContainer container = createBundleContainer(2);
		Storage<ItemVariant> storage = findStorage(container);
		SimpleContainer destination = new SimpleContainer(new ItemStack(Items.DIAMOND, 63));

		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(1, StorageUtil.move(storage, ContainerStorage.of(destination, null), variant -> true, Long.MAX_VALUE, tx));
			tx.commit();
		}

		assertEquals(new BundleContents(List.of(new ItemStackTemplate(Items.DIAMOND, 1))),
				container.getItem(0).get(DataComponents.BUNDLE_CONTENTS));
		assertEquals(64, destination.getItem(0).getCount());
	}

	private static Storage<ItemVariant> findStorage(SimpleContainer container) {
		return ContainerItemContext.ofSingleSlot(ContainerStorage.of(container, null).getSlot(0)).find(ItemStorage.ITEM);
	}

	private static SimpleContainer createBundleContainer(int diamondCount) {
		return createBundleContainer(new ItemStackTemplate(Items.DIAMOND, diamondCount));
	}

	private static SimpleContainer createBundleContainer(ItemStackTemplate... contents) {
		ItemStack bundle = new ItemStack(Items.BUNDLE);
		bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(contents)));
		return new SimpleContainer(bundle);
	}
}

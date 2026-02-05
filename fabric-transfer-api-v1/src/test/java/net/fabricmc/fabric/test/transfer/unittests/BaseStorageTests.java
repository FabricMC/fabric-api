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

import static net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;
import static net.fabricmc.fabric.test.transfer.TestUtil.assertEquals;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

public class BaseStorageTests extends AbstractTransferApiTest {
	@BeforeAll
	static void beforeAll() {
		bootstrap();
	}

	@Test
	public void testFilteringStorage() {
		SingleVariantStorage<FluidVariant> storage = new SingleVariantStorage<>() {
			@Override
			protected FluidVariant getBlankVariant() {
				return FluidVariant.blank();
			}

			@Override
			protected long getCapacity(FluidVariant variant) {
				return BUCKET * 10;
			}
		};
		Storage<FluidVariant> noWater = new FilteringStorage<>(storage) {
			@Override
			protected boolean canExtract(FluidVariant resource) {
				return !resource.isOf(Fluids.WATER);
			}

			@Override
			protected boolean canInsert(FluidVariant resource) {
				return !resource.isOf(Fluids.WATER);
			}
		};
		FluidVariant water = FluidVariant.of(Fluids.WATER);
		FluidVariant lava = FluidVariant.of(Fluids.LAVA);

		// Insertion into the backing storage should succeed.
		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(BUCKET, storage.insert(water, BUCKET, tx));
			tx.commit();
		}

		// Insertion through the filter should fail.
		assertEquals(0L, StorageUtil.simulateInsert(noWater, water, BUCKET, null));
		// Extraction should also fail.
		assertEquals(0L, StorageUtil.simulateExtract(noWater, water, BUCKET, null));
		// The fluid should be visible.
		assertEquals(water, StorageUtil.findStoredResource(noWater));
		// Test the filter.
		assertEquals(null, StorageUtil.findStoredResource(noWater, fv -> fv.isOf(Fluids.LAVA)));
		// But it can't be extracted, even through a storage view.
		assertEquals(null, StorageUtil.findExtractableResource(noWater, null));
		assertEquals(null, StorageUtil.findExtractableContent(noWater, null));

		storage.amount = 0;
		storage.variant = FluidVariant.blank();

		// Lava insertion and extract should proceed just fine.
		try (Transaction tx = Transaction.openOuter()) {
			assertEquals(BUCKET, noWater.insert(lava, BUCKET, tx));
			assertEquals(BUCKET, StorageUtil.simulateExtract(noWater, lava, BUCKET, tx));
			// Test that simulating doesn't change the state...
			assertEquals(BUCKET, StorageUtil.simulateExtract(noWater, lava, BUCKET, tx));
			assertEquals(BUCKET, StorageUtil.simulateExtract(noWater, lava, BUCKET, tx));
			tx.commit();
		}

		assertEquals(BUCKET, StorageUtil.simulateExtract(storage, lava, BUCKET, null));
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/3414">
	 * {@code nonEmptyIterator} not handling views that become empty during iteration correctly</a>.
	 */
	@Test
	public void testNonEmptyIteratorWithModifiedView() {
		SingleVariantStorage<FluidVariant> storage = SingleFluidStorage.withFixedCapacity(BUCKET, () -> { });
		storage.variant = FluidVariant.of(Fluids.WATER);

		Iterator<StorageView<FluidVariant>> iterator = storage.nonEmptyIterator();
		storage.amount = BUCKET;
		// Iterator should have a next element now
		assertEquals(true, iterator.hasNext());
		assertEquals(storage, iterator.next());

		iterator = storage.nonEmptyIterator();
		storage.amount = 0;
		// Iterator should not have a next element...
		assertEquals(false, iterator.hasNext());
	}
}

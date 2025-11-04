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

import java.util.Collections;
import java.util.Set;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenCustomHashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.test.transfer.TestUtil;

public class UnderlyingViewTests extends AbstractTransferApiTest {
	@BeforeAll
	static void beforeAll() {
		bootstrap();
	}

	/**
	 * Ensure that only 3 slots with different underlying view exist on all sides of a furnace combined.
	 */
	@Test
	public void testFurnaceSides() {
		FurnaceBlockEntity furnace = new FurnaceBlockEntity(BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());

		Set<StorageView<ItemVariant>> viewSet = Collections.newSetFromMap(new Reference2ReferenceOpenCustomHashMap<>(new Hash.Strategy<>() {
			@Override
			public int hashCode(StorageView<ItemVariant> o) {
				return o == null ? 0 : System.identityHashCode(o.getUnderlyingView());
			}

			@Override
			public boolean equals(StorageView<ItemVariant> a, StorageView<ItemVariant> b) {
				return a == null || b == null ? a == b : a.getUnderlyingView() == b.getUnderlyingView();
			}
		}));

		for (Direction direction : Direction.values()) {
			viewSet.addAll(InventoryStorage.of(furnace, direction).getSlots());
		}

		TestUtil.assertEquals(3, viewSet.size());
	}
}

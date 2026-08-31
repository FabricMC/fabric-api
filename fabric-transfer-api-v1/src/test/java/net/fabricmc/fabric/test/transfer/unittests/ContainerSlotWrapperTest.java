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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;

class ContainerSlotWrapperTest extends AbstractTransferApiTest {
	@BeforeAll
	static void beforeAll() {
		bootstrap();
	}

	@Test
	public void testGetCapacity() {
		SimpleContainer simpleContainer = new SimpleContainer(3);
		simpleContainer.setItem(0, new ItemStack(Items.DIRT));
		simpleContainer.setItem(1, new ItemStack(Items.DIAMOND_PICKAXE));

		ContainerStorage storage = ContainerStorage.of(simpleContainer, null);

		assertEquals(64, storage.getSlot(0).getCapacity());
		assertEquals(1, storage.getSlot(1).getCapacity());
		assertEquals(99, storage.getSlot(2).getCapacity(), "Empty slots report the full capacity");
	}
}

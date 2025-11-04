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

package net.fabricmc.fabric.test.lookup;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.test.lookup.api.ItemExtractable;

public class CobbleGenBlockEntity extends BlockEntity implements ItemExtractable {
	public CobbleGenBlockEntity(BlockPos pos, BlockState state) {
		super(FabricApiLookupTest.COBBLE_GEN_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	public ItemStack tryExtract(int maxCount, Predicate<ItemStack> filter, boolean simulate) {
		ItemStack cobble = new ItemStack(Items.COBBLESTONE);

		if (filter.test(cobble)) {
			return cobble;
		} else {
			return ItemStack.EMPTY;
		}
	}
}

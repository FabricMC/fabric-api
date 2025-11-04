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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.test.lookup.api.ItemApis;
import net.fabricmc.fabric.test.lookup.api.ItemExtractable;
import net.fabricmc.fabric.test.lookup.api.ItemInsertable;
import net.fabricmc.fabric.test.lookup.api.ItemUtils;

public class ChuteBlockEntity extends BlockEntity {
	private int moveDelay = 0;
	private BlockApiCache<ItemInsertable, Direction> cachedInsertable = null;
	private BlockApiCache<ItemExtractable, Direction> cachedExtractable = null;

	public ChuteBlockEntity(BlockPos pos, BlockState state) {
		super(FabricApiLookupTest.CHUTE_BLOCK_ENTITY_TYPE, pos, state);
	}

	public static void serverTick(Level world, BlockPos pos, BlockState blockState, ChuteBlockEntity blockEntity) {
		if (!blockEntity.hasLevel()) {
			return;
		}

		if (blockEntity.cachedInsertable == null) {
			blockEntity.cachedInsertable = BlockApiCache.create(ItemApis.INSERTABLE, (ServerLevel) world, pos.relative(Direction.DOWN));
		}

		if (blockEntity.cachedExtractable == null) {
			blockEntity.cachedExtractable = BlockApiCache.create(ItemApis.EXTRACTABLE, (ServerLevel) world, pos.relative(Direction.UP));
		}

		if (blockEntity.moveDelay == 0) {
			ItemExtractable from = blockEntity.cachedExtractable.find(Direction.DOWN);
			ItemInsertable to = blockEntity.cachedInsertable.find(Direction.UP);

			if (from != null && to != null) {
				ItemUtils.move(from, to, 1);
			}

			blockEntity.moveDelay = 20;
		}

		--blockEntity.moveDelay;
	}
}

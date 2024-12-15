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

package net.fabricmc.fabric.impl.object.builder;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

public class ExtendedBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {
	private final boolean canPotentiallyExecuteCommands;

	public ExtendedBlockEntityType(BlockEntityFactory<? extends T> factory, Set<Block> blocks, boolean canPotentiallyExecuteCommands) {
		super(factory, blocks);
		this.canPotentiallyExecuteCommands = canPotentiallyExecuteCommands;
	}

	@Override
	public boolean canPotentiallyExecuteCommands() {
		if (canPotentiallyExecuteCommands) {
			return true;
		}

		return super.canPotentiallyExecuteCommands();
	}
}

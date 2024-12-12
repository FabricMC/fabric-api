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

package net.fabricmc.fabric.impl.datagen.loot;

import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.data.loottable.BlockLootTableGenerator;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.featuretoggle.FeatureFlags;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.fabric.mixin.datagen.loot.BlockLootTableGeneratorAccessor;

public class ConditionBlockLootTableGenerator extends BlockLootTableGenerator {
	private final BlockLootTableGenerator parent;
	private final ResourceCondition[] conditions;

	public ConditionBlockLootTableGenerator(BlockLootTableGenerator parent, ResourceCondition[] conditions) {
		super(Collections.emptySet(), FeatureFlags.FEATURE_MANAGER.getFeatureSet(), ((BlockLootTableGeneratorAccessor) parent).getRegistries());

		this.parent = parent;
		this.conditions = conditions;
	}

	@Override
	public void generate() {
		throw new UnsupportedOperationException("generate() should not be called.");
	}

	@Override
	public void addDrop(Block block, LootTable.Builder lootTable) {
		FabricDataGenHelper.addConditions(lootTable, conditions);
		this.parent.addDrop(block, lootTable);
	}
}

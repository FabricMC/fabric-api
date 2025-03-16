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

package net.fabricmc.fabric.api.datagen.v1.loot;

import com.google.common.base.Preconditions;

import net.minecraft.data.loottable.EntityLootTableGenerator;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.loot.ConditionEntityLootTableGenerator;

public interface FabricEntityLootTableGenerator {
	/**
	 * {@return a new generator that applies the specified conditions to any loot table it receives, and then forwards
	 * the loot tables to this generator}.
	 */
	default EntityLootTableGenerator withConditions(ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");

		return new ConditionEntityLootTableGenerator((EntityLootTableGenerator) this, conditions);
	}
}

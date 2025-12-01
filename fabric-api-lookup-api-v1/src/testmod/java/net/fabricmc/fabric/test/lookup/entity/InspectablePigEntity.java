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

package net.fabricmc.fabric.test.lookup.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.test.lookup.api.Inspectable;

public class InspectablePigEntity extends Pig implements Inspectable {
	public InspectablePigEntity(EntityType<? extends Pig> entityType, Level world) {
		super(entityType, world);
	}

	@Override
	public Component inspect() {
		return Component.literal("InspectablePigEntity");
	}
}

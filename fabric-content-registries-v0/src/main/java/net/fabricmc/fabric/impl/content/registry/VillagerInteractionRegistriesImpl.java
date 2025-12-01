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

package net.fabricmc.fabric.impl.content.registry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.impl.content.registry.util.ImmutableCollectionUtils;
import net.fabricmc.fabric.mixin.content.registry.VillagerAccessor;
import net.fabricmc.fabric.mixin.content.registry.WorkAtComposterAccessor;

public final class VillagerInteractionRegistriesImpl {
	private static final Set<Item> GATHERABLE_ITEMS = new HashSet<>();

	private VillagerInteractionRegistriesImpl() {
	}

	public static Set<Item> getCollectableRegistry() {
		return GATHERABLE_ITEMS;
	}

	public static List<Item> getCompostableRegistry() {
		return ImmutableCollectionUtils.getAsMutableList(WorkAtComposterAccessor::fabric_getCompostable, WorkAtComposterAccessor::fabric_setCompostables);
	}

	public static Map<Item, Integer> getFoodRegistry() {
		return ImmutableCollectionUtils.getAsMutableMap(() -> Villager.FOOD_POINTS, VillagerAccessor::fabric_setItemFoodValues);
	}
}

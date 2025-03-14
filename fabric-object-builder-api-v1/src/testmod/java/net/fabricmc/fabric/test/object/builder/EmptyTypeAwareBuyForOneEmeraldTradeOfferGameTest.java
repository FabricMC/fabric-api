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

package net.fabricmc.fabric.test.object.builder;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerType;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class EmptyTypeAwareBuyForOneEmeraldTradeOfferGameTest {
	@GameTest
	public void testEmptyTypeAwareTradeOffer(@NotNull TestContext context) {
		VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, context.getWorld(), VillagerType.PLAINS);

		// Create a type-aware trade offer with no villager types specified
		TradeOffers.Factory typeAwareFactory = new TradeOffers.TypeAwareBuyForOneEmeraldFactory(1, 12, 5, ImmutableMap.of());
		// Create an offer with that factory to ensure it doesn't crash when a villager type is missing from the map
		typeAwareFactory.create(villager, Random.create());

		context.complete();
	}
}

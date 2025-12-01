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

import org.jspecify.annotations.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;

class SimpleTradeFactory implements VillagerTrades.ItemListing {
	private final MerchantOffer offer;

	SimpleTradeFactory(MerchantOffer offer) {
		this.offer = offer;
	}

	@Override
	public @Nullable MerchantOffer getOffer(ServerLevel serverWorld, Entity entity, RandomSource random) {
		// ALWAYS supply a copy of the offer.
		return this.offer.copy();
	}
}

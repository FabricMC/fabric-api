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

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

class SimpleTradeFactory implements TradeOffers.Factory {
	private final TradeOffer offer;

	SimpleTradeFactory(TradeOffer offer) {
		this.offer = offer;
	}

	@Override
	public @Nullable TradeOffer create(ServerWorld serverWorld, Entity entity, Random random) {
		// ALWAYS supply a copy of the offer.
		return this.offer.copy();
	}
}

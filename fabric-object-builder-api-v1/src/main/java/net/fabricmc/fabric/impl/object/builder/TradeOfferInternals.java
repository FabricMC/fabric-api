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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

public final class TradeOfferInternals {
	private TradeOfferInternals() {
	}

	/**
	 * Make the rebalanced profession map modifiable, then copy all vanilla
	 * professions' trades to prevent modifications from propagating to the rebalanced one.
	 */
	private static void initVillagerTrades() {
		if (!(TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE instanceof HashMap)) {
			Map<RegistryKey<VillagerProfession>, Int2ObjectMap<TradeOffers.Factory[]>> map = new HashMap<>(TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE);

			for (Map.Entry<RegistryKey<VillagerProfession>, Int2ObjectMap<TradeOffers.Factory[]>> trade : TradeOffers.PROFESSION_TO_LEVELED_TRADE.entrySet()) {
				if (!map.containsKey(trade.getKey())) map.put(trade.getKey(), trade.getValue());
			}

			TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE = map;
		}
	}

	// synchronized guards against concurrent modifications - Vanilla does not mutate the underlying arrays (as of 1.16),
	// so reads will be fine without locking.
	public static synchronized void registerVillagerOffers(RegistryKey<VillagerProfession> profession, int level, TradeOfferHelper.VillagerOffersAdder factory) {
		Objects.requireNonNull(profession, "VillagerProfession may not be null.");
		initVillagerTrades();
		registerOffers(TradeOffers.PROFESSION_TO_LEVELED_TRADE.computeIfAbsent(profession, key -> new Int2ObjectOpenHashMap<>()), level, trades -> factory.onRegister(trades, false));
		registerOffers(TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE.computeIfAbsent(profession, key -> new Int2ObjectOpenHashMap<>()), level, trades -> factory.onRegister(trades, true));
	}

	private static void registerOffers(Int2ObjectMap<TradeOffers.Factory[]> leveledTradeMap, int level, Consumer<List<TradeOffers.Factory>> factory) {
		final List<TradeOffers.Factory> list = new ArrayList<>();
		factory.accept(list);

		final TradeOffers.Factory[] originalEntries = leveledTradeMap.computeIfAbsent(level, key -> new TradeOffers.Factory[0]);
		final TradeOffers.Factory[] addedEntries = list.toArray(new TradeOffers.Factory[0]);

		final TradeOffers.Factory[] allEntries = ArrayUtils.addAll(originalEntries, addedEntries);
		leveledTradeMap.put(level, allEntries);
	}

	public static class WanderingTraderOffersBuilderImpl implements TradeOfferHelper.WanderingTraderOffersBuilder {
		private static final Object2IntMap<Identifier> ID_TO_INDEX = Util.make(new Object2IntOpenHashMap<>(), idToIndex -> {
			idToIndex.put(BUY_ITEMS_POOL, 0);
			idToIndex.put(SELL_SPECIAL_ITEMS_POOL, 1);
			idToIndex.put(SELL_COMMON_ITEMS_POOL, 2);
		});

		private static final Map<Identifier, TradeOffers.Factory[]> DELAYED_MODIFICATIONS = new HashMap<>();

		/**
		 * Make the trade list modifiable.
		 */
		static void initWanderingTraderTrades() {
			if (!(TradeOffers.WANDERING_TRADER_TRADES instanceof ArrayList)) {
				TradeOffers.WANDERING_TRADER_TRADES = new ArrayList<>(TradeOffers.WANDERING_TRADER_TRADES);
			}
		}

		@Override
		public TradeOfferHelper.WanderingTraderOffersBuilder pool(Identifier id, int count, TradeOffers.Factory... factories) {
			if (factories.length == 0) throw new IllegalArgumentException("cannot add empty pool");
			if (count <= 0) throw new IllegalArgumentException("count must be positive");

			Objects.requireNonNull(id, "id cannot be null");

			if (ID_TO_INDEX.containsKey(id)) throw new IllegalArgumentException("pool id %s is already registered".formatted(id));

			Pair<TradeOffers.Factory[], Integer> pool = Pair.of(factories, count);
			initWanderingTraderTrades();
			ID_TO_INDEX.put(id, TradeOffers.WANDERING_TRADER_TRADES.size());
			TradeOffers.WANDERING_TRADER_TRADES.add(pool);
			TradeOffers.Factory[] delayedModifications = DELAYED_MODIFICATIONS.remove(id);

			if (delayedModifications != null) addOffersToPool(id, delayedModifications);

			return this;
		}

		@Override
		public TradeOfferHelper.WanderingTraderOffersBuilder addOffersToPool(Identifier pool, TradeOffers.Factory... factories) {
			if (!ID_TO_INDEX.containsKey(pool)) {
				DELAYED_MODIFICATIONS.compute(pool, (id, current) -> {
					if (current == null) return factories;

					return ArrayUtils.addAll(current, factories);
				});
				return this;
			}

			int poolIndex = ID_TO_INDEX.getInt(pool);
			initWanderingTraderTrades();
			Pair<TradeOffers.Factory[], Integer> poolPair = TradeOffers.WANDERING_TRADER_TRADES.get(poolIndex);
			TradeOffers.Factory[] modified = ArrayUtils.addAll(poolPair.getLeft(), factories);
			TradeOffers.WANDERING_TRADER_TRADES.set(poolIndex, Pair.of(modified, poolPair.getRight()));
			return this;
		}
	}
}

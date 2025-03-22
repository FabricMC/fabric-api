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

package net.fabricmc.fabric.api.object.builder.v1.trade;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import net.fabricmc.fabric.impl.object.builder.TradeOfferInternals;

/**
 * Utilities to help with registration of trade offers.
 */
public final class TradeOfferHelper {
	/**
	 * Registers trade offer factories for use by villagers.
	 * This adds the same trade offers to current and rebalanced trades.
	 * To add separate offers for the rebalanced trade experiment, use
	 * {@link #registerVillagerOffers(RegistryKey, int, VillagerOffersAdder)}.
	 *
	 * <p>Below is an example, of registering a trade offer factory to be added a blacksmith with a profession level of 3:
	 * <blockquote><pre>
	 * TradeOfferHelper.registerVillagerOffers(VillagerProfession.BLACKSMITH, 3, factories -> {
	 * 	factories.add(new CustomTradeFactory(...);
	 * });
	 * </pre></blockquote>
	 *
	 * @param profession the villager profession to assign the trades to
	 * @param level the profession level the villager must be to offer the trades
	 * @param factories a consumer to provide the factories
	 */
	public static void registerVillagerOffers(RegistryKey<VillagerProfession> profession, int level, Consumer<List<TradeOffers.Factory>> factories) {
		TradeOfferInternals.registerVillagerOffers(profession, level, (trades, rebalanced) -> factories.accept(trades));
	}

	/**
	 * Registers trade offer factories for use by villagers.
	 * This method allows separate offers to be added depending on whether the rebalanced
	 * trade experiment is enabled.
	 * If a particular profession's rebalanced trade offers are not added at all, it falls back
	 * to the regular trade offers.
	 *
	 * <p>Below is an example, of registering a trade offer factory to be added a blacksmith with a profession level of 3:
	 * <blockquote><pre>
	 * TradeOfferHelper.registerVillagerOffers(VillagerProfession.BLACKSMITH, 3, (factories, rebalanced) -> {
	 * 	factories.add(new CustomTradeFactory(...);
	 * });
	 * </pre></blockquote>
	 *
	 * <p><strong>Experimental feature</strong>. This API may receive changes as necessary to adapt to further experiment changes.
	 *
	 * @param profession the villager profession to assign the trades to
	 * @param level the profession level the villager must be to offer the trades
	 * @param factories a consumer to provide the factories
	 */
	@ApiStatus.Experimental
	public static void registerVillagerOffers(RegistryKey<VillagerProfession> profession, int level, VillagerOffersAdder factories) {
		TradeOfferInternals.registerVillagerOffers(profession, level, factories);
	}

	/**
	 * Registers trade offer factories for use by wandering trades.
	 *
	 * @param factory a consumer to add trade offers
	 */
	public static synchronized void registerWanderingTraderOffers(Consumer<WanderingTraderOffersBuilder> factory) {
		factory.accept(new TradeOfferInternals.WanderingTraderOffersBuilderImpl());
	}

	private TradeOfferHelper() {
	}

	@FunctionalInterface
	public interface VillagerOffersAdder {
		void onRegister(List<TradeOffers.Factory> factories, boolean rebalanced);
	}

	/**
	 * A builder for wandering trader offers.
	 *
	 * @see #registerWanderingTraderOffers(Consumer)
	 */
	@ApiStatus.NonExtendable
	public interface WanderingTraderOffersBuilder {
		/**
		 * The pool ID for the "buy items" pool.
		 * Two trade offers are picked from this pool.
		 *
		 * <p>In vanilla, this pool contains offers to buy water buckets, baked potatoes, etc.
		 * for emeralds.
		 */
		Identifier BUY_ITEMS_POOL = Identifier.ofVanilla("buy_items");
		/**
		 * The pool ID for the "sell special items" pool.
		 * Two trade offers are picked from this pool.
		 *
		 * <p>In vanilla, this pool contains offers to sell logs, enchanted iron pickaxes, etc.
		 */
		Identifier SELL_SPECIAL_ITEMS_POOL = Identifier.ofVanilla("sell_special_items");
		/**
		 * The pool ID for the "sell common items" pool.
		 * Five trade offers are picked from this pool.
		 *
		 * <p>In vanilla, this pool contains offers to sell flowers, saplings, etc.
		 */
		Identifier SELL_COMMON_ITEMS_POOL = Identifier.ofVanilla("sell_common_items");

		/**
		 * Adds a new pool to the offer list. Exactly {@code count} offers are picked from
		 * {@code factories} and offered to customers.
		 * @param id the ID to be assigned to this pool, to allow further modification
		 * @param count the number of offers to be picked from {@code factories}
		 * @param factories the trade offer factories
		 * @return this builder, for chaining
		 * @throws IllegalArgumentException if {@code count} is not positive or if {@code factories} is empty
		 */
		WanderingTraderOffersBuilder pool(Identifier id, int count, TradeOffers.Factory... factories);

		/**
		 * Adds a new pool to the offer list. Exactly {@code count} offers are picked from
		 * {@code factories} and offered to customers.
		 * @param id the ID to be assigned to this pool, to allow further modification
		 * @param count the number of offers to be picked from {@code factories}
		 * @param factories the trade offer factories
		 * @return this builder, for chaining
		 * @throws IllegalArgumentException if {@code count} is not positive or if {@code factories} is empty
		 */
		default WanderingTraderOffersBuilder pool(Identifier id, int count, Collection<? extends TradeOffers.Factory> factories) {
			return pool(id, count, factories.toArray(TradeOffers.Factory[]::new));
		}

		/**
		 * Adds trade offers to the offer list. All offers from {@code factories} are
		 * offered to each customer.
		 * @param id the ID to be assigned to this pool, to allow further modification
		 * @param factories the trade offer factories
		 * @return this builder, for chaining
		 * @throws IllegalArgumentException if {@code factories} is empty
		 */
		default WanderingTraderOffersBuilder addAll(Identifier id, Collection<? extends TradeOffers.Factory> factories) {
			return pool(id, factories.size(), factories);
		}

		/**
		 * Adds trade offers to the offer list. All offers from {@code factories} are
		 * offered to each customer.
		 * @param id the ID to be assigned to this pool, to allow further modification
		 * @param factories the trade offer factories
		 * @return this builder, for chaining
		 * @throws IllegalArgumentException if {@code factories} is empty
		 */
		default WanderingTraderOffersBuilder addAll(Identifier id, TradeOffers.Factory... factories) {
			return pool(id, factories.length, factories);
		}

		/**
		 * Adds trade offers to an existing pool identified by an ID.
		 *
		 * <p>See the constants for vanilla trade offer pool IDs that are always available.
		 * @param pool the pool ID
		 * @param factories the trade offer factories
		 * @return this builder, for chaining
		 * @throws IndexOutOfBoundsException if {@code pool} is out of bounds
		 */
		WanderingTraderOffersBuilder addOffersToPool(Identifier pool, TradeOffers.Factory... factories);

		/**
		 * Adds trade offers to an existing pool identified by an ID.
		 *
		 * <p>See the constants for vanilla trade offer pool IDs that are always available.
		 * @param pool the pool ID
		 * @param factories the trade offer factories
		 * @return this builder, for chaining
		 * @throws IndexOutOfBoundsException if {@code pool} is out of bounds
		 */
		default WanderingTraderOffersBuilder addOffersToPool(Identifier pool, Collection<TradeOffers.Factory> factories) {
			return addOffersToPool(pool, factories.toArray(TradeOffers.Factory[]::new));
		}
	}
}

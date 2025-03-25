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

import static net.minecraft.command.argument.EntityArgumentType.entity;
import static net.minecraft.command.argument.EntityArgumentType.getEntity;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Optional;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

public class VillagerTypeTest1 implements ModInitializer {
	private static final Identifier FOOD_POOL_ID = ObjectBuilderTestConstants.id("food");
	private static final Identifier THING_POOL_ID = ObjectBuilderTestConstants.id("thing");

	@Override
	public void onInitialize() {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, (factories, rebalanced) -> {
			Item scrap = rebalanced ? Items.NETHER_BRICK : Items.NETHERITE_SCRAP;
			factories.add(new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.GOLD_INGOT, 3), Optional.of(new TradedItem(scrap, 4)), new ItemStack(Items.NETHERITE_INGOT), 2, 6, 0.15F)));
		});
		// Toolsmith is not rebalanced yet
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 1, (factories, rebalanced) -> {
			Item scrap = rebalanced ? Items.NETHER_BRICK : Items.NETHERITE_SCRAP;
			factories.add(new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.GOLD_INGOT, 3), Optional.of(new TradedItem(scrap, 4)), new ItemStack(Items.NETHERITE_INGOT), 2, 6, 0.15F)));
		});

		TradeOfferHelper.registerWanderingTraderOffers(builder -> {
			builder.pool(
					FOOD_POOL_ID,
					5,
					Registries.ITEM.stream().filter(item -> item.getDefaultStack().contains(DataComponentTypes.FOOD)).map(
							item -> new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.NETHERITE_INGOT), new ItemStack(item), 3, 4, 0.15F))
					).toList()
			);
			builder.addAll(
					THING_POOL_ID,
					new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.NETHERITE_INGOT), new ItemStack(Items.MOJANG_BANNER_PATTERN), 1, 4, 0.15F))
			);
			builder.addOffersToPool(
					TradeOfferHelper.WanderingTraderOffersBuilder.BUY_ITEMS_POOL,
					new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.BLAZE_POWDER, 1), new ItemStack(Items.EMERALD, 4), 3, 4, 0.15F)),
					new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.NETHER_WART, 5), new ItemStack(Items.EMERALD, 1), 3, 4, 0.15F)),
					new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.GOLDEN_CARROT, 4), new ItemStack(Items.EMERALD, 1), 3, 4, 0.15F))
			);
			builder.addOffersToPool(
					TradeOfferHelper.WanderingTraderOffersBuilder.SELL_SPECIAL_ITEMS_POOL,
					new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.EMERALD, 6), new ItemStack(Items.BRUSH, 1), 1, 4, 0.15F)),
					new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.DIAMOND, 16), new ItemStack(Items.ELYTRA, 1), 1, 4, 0.15F)),
					new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.EMERALD, 3), new ItemStack(Items.LEAD, 2), 3, 4, 0.15F))
			);
			builder.addOffersToPool(
					FOOD_POOL_ID,
					new SimpleTradeFactory(new TradeOffer(new TradedItem(Items.NETHERITE_INGOT), new ItemStack(Items.EGG), 3, 4, 0.15F))
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("fabric_applywandering_trades")
					.then(argument("entity", entity()).executes(context -> {
						final Entity entity = getEntity(context, "entity");

						if (!(entity instanceof WanderingTraderEntity)) {
							throw new SimpleCommandExceptionType(Text.literal("Entity is not a wandering trader")).create();
						}

						WanderingTraderEntity trader = (WanderingTraderEntity) entity;
						trader.getOffers().clear();

						for (Pair<TradeOffers.Factory[], Integer> value : TradeOffers.WANDERING_TRADER_TRADES) {
							for (TradeOffers.Factory factory : value.getKey()) {
								final TradeOffer result = factory.create(trader, Random.create());

								if (result == null) {
									continue;
								}

								trader.getOffers().add(result);
							}
						}

						return 1;
					})));
		});
	}
}

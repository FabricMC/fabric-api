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

import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

/*
 * Second entrypoint to validate class loading does not break this.
 */
public class VillagerTypeTest2 implements ModInitializer {
	@Override
	public void onInitialize() {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 1, factories -> {
			factories.add(new SimpleTradeFactory(new MerchantOffer(new ItemCost(Items.DIAMOND, 5), new ItemStack(Items.NETHERITE_INGOT), 3, 4, 0.15F)));
		});
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new MerchantOffer(new ItemCost(Items.DIAMOND, 6), new ItemStack(Items.ELYTRA), 3, 4, 0.15F)));
		});
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new MerchantOffer(new ItemCost(Items.DIAMOND, 7), new ItemStack(Items.CHAINMAIL_BOOTS), 3, 4, 0.15F)));
		});
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new MerchantOffer(new ItemCost(Items.DIAMOND, 8), new ItemStack(Items.CHAINMAIL_CHESTPLATE), 3, 4, 0.15F)));
		});
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new MerchantOffer(new ItemCost(Items.DIAMOND, 9), new ItemStack(Items.CHAINMAIL_HELMET), 3, 4, 0.15F)));
		});
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new MerchantOffer(new ItemCost(Items.DIAMOND, 10), new ItemStack(Items.CHAINMAIL_LEGGINGS), 3, 4, 0.15F)));
		});
	}
}

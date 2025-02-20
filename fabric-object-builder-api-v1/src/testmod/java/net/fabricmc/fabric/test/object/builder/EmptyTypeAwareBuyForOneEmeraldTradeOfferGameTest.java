package net.fabricmc.fabric.test.object.builder;

import com.google.common.collect.ImmutableMap;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerType;

import org.jetbrains.annotations.NotNull;

public class EmptyTypeAwareBuyForOneEmeraldTradeOfferGameTest implements FabricGameTest {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEmptyTypeAwareTradeOffer(@NotNull TestContext context) {
		VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, context.getWorld(), VillagerType.PLAINS);

		// Create a type-aware trade offer with no villager types specified
		TradeOffers.Factory typeAwareFactory = new TradeOffers.TypeAwareBuyForOneEmeraldFactory(1, 12, 5, ImmutableMap.of());
		// Create an offer with that factory to ensure it doesn't crash when a villager type is missing from the map
		typeAwareFactory.create(villager, Random.create());

		context.complete();
	}
}

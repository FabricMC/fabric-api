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

package net.fabricmc.fabric.test.item.gametest;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.GameType;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.test.item.CustomEnchantmentEffectsTest;

public class CustomEnchantmentEffectsGameTest {
	@GameTest
	public void weirdImpalingSetsFireToTargets(GameTestHelper context) {
		BlockPos pos = new BlockPos(3, 3, 3);
		Creeper creeper = context.spawn(EntityType.CREEPER, pos);
		Player player = context.makeMockPlayer(GameType.CREATIVE);

		ItemStack trident = Items.TRIDENT.getDefaultInstance();
		Optional<Holder.Reference<Enchantment>> impaling = getEnchantmentRegistry(context)
				.get(CustomEnchantmentEffectsTest.WEIRD_IMPALING);
		if (impaling.isEmpty()) {
			throw context.assertionException("Weird Impaling enchantment is not present");
		}

		trident.enchant(impaling.get(), 1);

		player.setItemInHand(InteractionHand.MAIN_HAND, trident);

		context.assertEntityData(pos, EntityType.CREEPER, Entity::isOnFire, false);
		player.attack(creeper);
		context.succeedWhenEntityData(pos, EntityType.CREEPER, Entity::isOnFire, true);
	}

	@GameTest
	public void weirdImpalingHasTwoDamageEffects(GameTestHelper context) {
		Enchantment impaling = getEnchantmentRegistry(context).getValue(CustomEnchantmentEffectsTest.WEIRD_IMPALING);

		if (impaling == null) {
			throw context.assertionException("Weird Impaling enchantment is not present");
		}

		List<ConditionalEffect<EnchantmentValueEffect>> damageEffects = impaling
				.getEffects(EnchantmentEffectComponents.DAMAGE);

		context.assertTrue(
				damageEffects.size() == 2,
				Component.literal(String.format("Weird Impaling has %d damage effect(s), not the expected 2", damageEffects.size()))
		);
		context.succeed();
	}

	private static Registry<Enchantment> getEnchantmentRegistry(GameTestHelper context) {
		RegistryAccess registryManager = context.getLevel().registryAccess();
		return registryManager.lookupOrThrow(Registries.ENCHANTMENT);
	}
}

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

package net.fabricmc.fabric.test.entity.event.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class ServerLivingEntityEventsGameTest {
	@GameTest
	public void canFeedStickToHorse(GameTestHelper context) {
		Horse horse = context.spawn(EntityTypes.HORSE, BlockPos.ZERO);
		Player player = interactWithHorse(horse, Items.STICK, context);

		context.assertFalse(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.STICK), "Player is still holding a stick!");

		context.succeed();
	}

	@GameTest
	public void stickHealsAndSetsInLoveForHorse(GameTestHelper context) {
		Horse horse = context.spawn(EntityTypes.HORSE, BlockPos.ZERO);
		horse.setHealth(1.0f);
		horse.setTamed(true);

		interactWithHorse(horse, Items.STICK, context);

		context.assertTrue(horse.getHealth() == horse.getMaxHealth(), "Horse was not healed!");
		context.assertTrue(horse.isInLove(), "Horse was not put in love!");

		context.succeed();
	}

	@GameTest
	public void stickTempersAndAgesUpBabyHorse(GameTestHelper context) {
		Horse horse = context.spawn(EntityTypes.HORSE, BlockPos.ZERO);
		horse.setBaby(true);
		horse.setTemper(0);

		interactWithHorse(horse, Items.STICK, context);

		context.assertTrue(!horse.isBaby(), "Horse is still a baby!");
		context.assertTrue(horse.getTemper() == horse.getMaxTemper(), "Horse was not tempered!");

		context.succeed();
	}

	@GameTest
	public void goldenCarrotNoLongerSetsInLoveForHorse(GameTestHelper context) {
		Horse horse = context.spawn(EntityTypes.HORSE, BlockPos.ZERO);
		horse.setTamed(true);

		interactWithHorse(horse, Items.GOLDEN_CARROT, context);

		context.assertTrue(!horse.isInLove(), "Horse is in love!");

		context.succeed();
	}

	private Player interactWithHorse(Horse horse, Item item, GameTestHelper context) {
		Player player = context.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, item.getDefaultInstance());

		player.interactOn(horse, InteractionHand.MAIN_HAND, new Vec3(BlockPos.ZERO));
		return player;
	}
}

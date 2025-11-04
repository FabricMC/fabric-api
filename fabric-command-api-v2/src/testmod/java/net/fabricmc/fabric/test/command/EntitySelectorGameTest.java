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

package net.fabricmc.fabric.test.command;

import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class EntitySelectorGameTest {
	private void spawn(GameTestHelper context, float health) {
		Mob entity = context.spawnWithNoFreeWill(EntityType.CREEPER, BlockPos.ZERO);
		entity.setNoAi(true);
		entity.setHealth(health);
	}

	@GameTest
	public void testEntitySelector(GameTestHelper context) {
		BlockPos absolute = context.absolutePos(BlockPos.ZERO);

		spawn(context, 1.0f);
		spawn(context, 5.0f);
		spawn(context, 10.0f);

		String command = String.format(
				Locale.ROOT,
				"/kill @e[x=%d, y=%d, z=%d, distance=..2, %s=5.0]",
				absolute.getX(),
				absolute.getY(),
				absolute.getZ(),
				CommandTest.SELECTOR_ID.toDebugFileName()
		);

		context.assertEntitiesPresent(EntityType.CREEPER, BlockPos.ZERO, 3, 2.0);
		MinecraftServer server = context.getLevel().getServer();
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
		//context.assertTrue(result == 2, "Expected 2 entities killed, got " + result);
		context.assertEntitiesPresent(EntityType.CREEPER, BlockPos.ZERO, 1, 2.0);
		context.succeed();
	}
}

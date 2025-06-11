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

package net.fabricmc.fabric.test.loot;

import net.minecraft.server.MinecraftServer;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public final class LootGameTest {
	static int inlineLootTablesSeen = 0;

	@GameTest
	public void testInlineTableModifyDrops(TestContext context) {
		int seenAtStart = inlineLootTablesSeen;
		MinecraftServer server = context.getWorld().getServer();
		server.getCommandManager().executeWithPrefix(server.getCommandSource(), "loot spawn 0 0 0 loot {\"pools\":[{\"entries\":[], \"rolls\":1.0}]}");
		int seenAtEnd = inlineLootTablesSeen;

		context.assertTrue(seenAtStart < seenAtEnd, Text.literal("inline loot table should've been processed by MODIFY_DROPS"));
		context.complete();
	}
}

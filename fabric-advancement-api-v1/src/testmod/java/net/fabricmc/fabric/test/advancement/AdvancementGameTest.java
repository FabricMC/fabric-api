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

package net.fabricmc.fabric.test.advancement;

import java.util.List;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public final class AdvancementGameTest {
	private static Advancement get(GameTestHelper helper, Identifier id) {
		AdvancementHolder holder = helper.getLevel().getServer().getAdvancements().get(id);
		helper.assertTrue(holder != null, Component.literal(id + " advancement should be loaded"));
		return holder.value();
	}

	@GameTest
	public void testModify(GameTestHelper helper) {
		// The test mod removes the pufferfish bucket criterion and adds its own criteria.
		Advancement advancement = get(helper, AdvancementTest.TACTICAL_FISHING);

		helper.assertFalse(advancement.criteria().containsKey("pufferfish_bucket"), Component.literal("removed criterion should be gone"));
		helper.assertTrue(advancement.criteria().containsKey("stone_pickaxe"), Component.literal("added criterion should be present"));
		helper.assertTrue(advancement.criteria().containsKey("diamond_sword"), Component.literal("criterion added with setCriteria should be present"));
		helper.assertTrue(advancement.sendsTelemetryEvent(), Component.literal("advancement should send telemetry events"));
		helper.succeed();
	}

	@GameTest
	public void testModifiedRequirements(GameTestHelper helper) {
		Advancement advancement = get(helper, AdvancementTest.TACTICAL_FISHING);
		List<List<String>> requirements = advancement.requirements().requirements();

		helper.assertFalse(advancement.requirements().names().contains("pufferfish_bucket"), Component.literal("removed criterion should not be required"));
		helper.assertTrue(requirements.contains(List.of("stone_pickaxe")), Component.literal("requireCriterion should have added its own requirement group"));
		helper.assertTrue(requirements.contains(List.of("stone_pickaxe", "diamond_sword")), Component.literal("requireCriteria should have added its own requirement group"));
		helper.succeed();
	}

	@GameTest
	public void testReplace(GameTestHelper helper) {
		// The test mod replaces the recipe book root advancement with one granting experience.
		Advancement advancement = get(helper, AdvancementTest.RECIPE_BOOK);

		helper.assertTrue(advancement.rewards().experience() == 1, Component.literal("replaced advancement should grant 1 experience"));
		helper.assertFalse(advancement.criteria().isEmpty(), Component.literal("replaced advancement should keep the copied criteria"));
		helper.succeed();
	}
}

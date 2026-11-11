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

package net.fabricmc.fabric.test.resource.loader;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.AABB;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class FinalizeEntitiesTest {
	private void checkPillager(Pillager pillager, GameTestHelper helper) {
		Block below = pillager.level().getBlockState(pillager.getOnPos().below()).getBlock();
		Item item = pillager.getMainHandItem().getItem();

		if (below == Blocks.WOOL.green()) {
			helper.assertTrue(item == Items.IRON_AXE, Component.literal("Expected pillager to hold axe in hand"));
		} else {
			helper.assertTrue(item == Items.CROSSBOW, Component.literal("Expected pillager to hold crossbow in hand"));
		}
	}

	@GameTest
	public void finalizeEntities(GameTestHelper helper) {
		AABB bounds = helper.getBounds();
		ServerLevel level = helper.getLevel();
		Registry<StructureTemplatePool> pools = level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> pool = pools.get(Constants.id("test_finalize_entities/platform")).orElseThrow();

		Identifier empty = Identifier.fromNamespaceAndPath("minecraft", "empty");
		boolean keepJigsaws = false;

		JigsawPlacement.generateJigsaw(level, pool, empty, 2, BlockPos.containing(bounds.getCenter()), keepJigsaws);

		List<Pillager> mobs = level.getEntities(EntityTypes.PILLAGER, bounds, mob -> mob.getBlockStateOn().is(BlockTags.WOOL));

		helper.assertTrue(mobs.size() == 3, Component.literal("Expected 3 pillagers, found " + mobs.size()));

		for (Pillager pillager : mobs) {
			checkPillager(pillager, helper);
		}

		helper.succeed();
	}
}

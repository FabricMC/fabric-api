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

package net.fabricmc.fabric.test.event.interaction;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class UseEntityTests implements ModInitializer {
	@Override
	public void onInitialize() {
		// Disallow interactions with toolsmiths
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (entity instanceof Villager villager && villager.getVillagerData().profession().is(VillagerProfession.TOOLSMITH)) {
				return InteractionResult.FAIL;
			}

			return InteractionResult.PASS;
		});
	}
}

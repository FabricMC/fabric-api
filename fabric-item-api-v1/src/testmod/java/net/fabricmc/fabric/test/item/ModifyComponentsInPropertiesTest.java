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

package net.fabricmc.fabric.test.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

public class ModifyComponentsInPropertiesTest implements ModInitializer {
	@Override
	public void onInitialize() {
		Item item = Registry.register(
				BuiltInRegistries.ITEM,
				Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "op_sword"),
				new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "op_sword"))).sword(ToolMaterial.NETHERITE, 3.0F, -2.4F).fireResistant().modifyComponent(DataComponents.TOOL, (original, _, _) -> {
					// derived from ToolMaterial#applySwordProperties
					var newRules = new ArrayList<>(Objects.requireNonNull(original, "sword method did not add a tool component?").rules());
					newRules.addFirst(new Tool.Rule(HolderSet.direct(Blocks.DIRT.builtInRegistryHolder()), Optional.of(44f), Optional.of(false)));
					return new Tool(List.copyOf(newRules), original.defaultMiningSpeed(), original.damagePerBlock(), original.canDestroyBlocksInCreative());
				}))
		);

		HashMap<UUID, Unit> giftedMap = new HashMap<>();
		ServerTickEvents.START_SERVER_TICK.register(server -> PlayerLookup.all(server).forEach(player -> {
			if (!giftedMap.containsKey(player.getUUID())) {
				player.drop(item.getDefaultInstance(), true);
				giftedMap.put(player.getUUID(), Unit.INSTANCE);
			}
		}));
	}
}

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

package net.fabricmc.fabric.test.holder.component;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.references.BlockIds;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Blocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.holder.component.v1.FabricDataComponentInitializers;

public class HolderComponentTestModEntrypoint implements ModInitializer {
	public static final String MODID = "fabric-holder-component-api-v1-testmod";
	private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			HolderComponentCommand.register(dispatcher, buildContext);
		});

		final Identifier first = Identifier.fromNamespaceAndPath("fabric", "first");
		final Identifier second = Identifier.fromNamespaceAndPath("fabric", "second");
		final Identifier third = Identifier.fromNamespaceAndPath("fabric", "third");

		FabricDataComponentInitializers.registerInitializer(third, _ -> LOGGER.info("I should be third!"));
		FabricDataComponentInitializers.registerInitializer(first, _ -> LOGGER.info("I should be first!"));
		FabricDataComponentInitializers.registerInitializer(second, _ -> LOGGER.info("I should be second!"));

		FabricDataComponentInitializers.addInitializerOrdering(first, second);
		FabricDataComponentInitializers.addInitializerOrdering(second, third);
	}
}

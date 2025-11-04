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

import java.util.List;

import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

public class DefaultItemComponentTest implements ModInitializer {
	@Override
	public void onInitialize() {
		Identifier latePhase = Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "late");
		DefaultItemComponentEvents.MODIFY.addPhaseOrdering(Event.DEFAULT_PHASE, latePhase);

		DefaultItemComponentEvents.MODIFY.register(context -> {
			context.modify(Items.GOLD_INGOT, builder -> {
				builder.set(DataComponents.ITEM_NAME, Component.literal("Fool's Gold").withStyle(ChatFormatting.GOLD));
			});
			context.modify(Items.GOLD_NUGGET, builder -> {
				builder.set(DataComponents.FIREWORKS, new Fireworks(1, List.of(
					new FireworkExplosion(FireworkExplosion.Shape.STAR, IntList.of(0x32a852), IntList.of(0x32a852), true, true)
				)));
			});
			context.modify(Items.BEEF, builder -> {
				// Remove the food component from beef
				builder.set(DataComponents.FOOD, null);
			});
			// add a word to the start of diamond pickaxe name
			context.modify(Items.DIAMOND_PICKAXE, builder -> {
				Component baseName = builder.getOrCreate(
						DataComponents.ITEM_NAME,
						Items.DIAMOND_PICKAXE::getName
				);
				builder.set(DataComponents.ITEM_NAME, prependModifiedLiteral(baseName));
			});
		});

		// Make all fireworks glint
		DefaultItemComponentEvents.MODIFY.register(latePhase, context -> {
			context.modify(item -> item.components().has(DataComponents.FIREWORKS), (builder, item) -> {
				builder.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
			});
		});
	}

	public static Component prependModifiedLiteral(Component name) {
		return Component.literal("Modified ")
				.append(name);
	}
}

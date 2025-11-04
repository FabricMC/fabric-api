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

package net.fabricmc.fabric.test.item.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;

import net.fabricmc.fabric.impl.item.DefaultItemComponentImpl;
import net.fabricmc.fabric.test.item.ComponentTooltipAppenderTest;

public class ComponentTooltipAppenderRegistryTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		new ComponentTooltipAppenderTest().onInitialize();
		DefaultItemComponentImpl.modifyItemComponents();
	}

	@Test
	void getSwordTooltips() {
		ItemStack stack = new ItemStack(Items.GOLDEN_SWORD);
		stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);

		assertEquals("""
				Golden Sword
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)
				This Item is Happy :)

				When in Main Hand:
				+3 Attack Damage
				-2.4 Attack Speed
				Unbreakable
				This Item is Sadder :'(""", getTooltip(stack));
	}

	@Test
	void getEggTooltips() {
		ItemStack stack = new ItemStack(Items.PIG_SPAWN_EGG);
		stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Hello"))));

		assertEquals("""
				Pig Spawn Egg
				Hello
				This Item is the Saddest :
				This Item is Sad :(""", getTooltip(stack));
	}

	private static String getTooltip(ItemStack stack) {
		List<Component> tooltips = stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL);
		return tooltips.stream().map(Component::getString).collect(Collectors.joining("\n"));
	}
}

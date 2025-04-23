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

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;

import net.fabricmc.fabric.impl.item.DefaultItemComponentImpl;
import net.fabricmc.fabric.test.item.ComponentTooltipAppenderTest;

public class ComponentTooltipAppenderRegistryTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();

		new ComponentTooltipAppenderTest().onInitialize();
		DefaultItemComponentImpl.modifyItemComponents();
	}

	@Test
	void getSwordTooltips() {
		ItemStack stack = new ItemStack(Items.GOLDEN_SWORD);
		stack.set(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE);

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
				This Item is Sadder :'(
				Unbreakable""", getTooltip(stack));
	}

	@Test
	void getEggTooltips() {
		ItemStack stack = new ItemStack(Items.PIG_SPAWN_EGG);
		stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal("Hello"))));

		assertEquals("""
				Pig Spawn Egg
				Hello
				This Item is the Saddest :
				This Item is Sad :(""", getTooltip(stack));
	}

	private static String getTooltip(ItemStack stack) {
		List<Text> tooltips = stack.getTooltip(Item.TooltipContext.DEFAULT, null, TooltipType.BASIC);
		return tooltips.stream().map(Text::getString).collect(Collectors.joining("\n"));
	}
}

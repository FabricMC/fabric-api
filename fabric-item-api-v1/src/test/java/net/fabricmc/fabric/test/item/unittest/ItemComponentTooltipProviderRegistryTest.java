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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;

import net.fabricmc.fabric.impl.item.DefaultItemComponentImpl;
import net.fabricmc.fabric.test.item.ComponentTooltipProviderTest;

public class ItemComponentTooltipProviderRegistryTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		new ComponentTooltipProviderTest().onInitialize();

		for (Item item : BuiltInRegistries.ITEM) {
			item.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
		}

		DefaultItemComponentImpl.modifyItemComponents(new HolderLookup.Provider() {
			@Override
			public @NonNull Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
				return Stream.empty();
			}

			@Override
			public <T> @NonNull Optional<? extends HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
				return Optional.empty();
			}
		});
	}

	@Test
	void getSwordTooltips() {
		ItemStack stack = new ItemStack(Items.GOLDEN_SWORD);
		stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);

		assertEquals("""

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
				Unbreakable
				This Item is Sadder :'(""", getTooltip(stack));
	}

	@Test
	void getEggTooltips() {
		ItemStack stack = new ItemStack(Items.PIG_SPAWN_EGG);
		stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Hello"))));

		assertEquals("""

				Hello
				This Item is the Saddest :
				This Item is Sad :(""", getTooltip(stack));
	}

	private static String getTooltip(ItemStack stack) {
		List<Component> tooltips = stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL);
		return tooltips.stream().map(Component::getString).collect(Collectors.joining("\n"));
	}
}

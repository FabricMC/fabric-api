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

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipProvider;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

public class ComponentTooltipAppenderTest implements ModInitializer {
	@Override
	public void onInitialize() {
		DataComponentType<TestComponent> happyComponent = Registry.register(
				BuiltInRegistries.DATA_COMPONENT_TYPE,
				"fabric-item-api-v1-testmod:happy_component",
				DataComponentType.<TestComponent>builder()
						.persistent(MapCodec.unitCodec(TestComponent.ONE))
						.build()
		);

		DataComponentType<TestComponent> sadComponent = Registry.register(
				BuiltInRegistries.DATA_COMPONENT_TYPE,
				"fabric-item-api-v1-testmod:sad_component",
				DataComponentType.<TestComponent>builder()
						.persistent(MapCodec.unitCodec(TestComponent.TWO))
						.build()
		);

		DataComponentType<TestComponent> sadderComponent = Registry.register(
				BuiltInRegistries.DATA_COMPONENT_TYPE,
				"fabric-item-api-v1-testmod:sadder_component",
				DataComponentType.<TestComponent>builder()
						.persistent(MapCodec.unitCodec(TestComponent.THREE))
						.build()
		);

		DataComponentType<TestComponent> saddestComponent = Registry.register(
				BuiltInRegistries.DATA_COMPONENT_TYPE,
				"fabric-item-api-v1-testmod:saddest_component",
				DataComponentType.<TestComponent>builder()
						.persistent(MapCodec.unitCodec(TestComponent.FOUR))
						.build()
		);

		ComponentTooltipAppenderRegistry.addFirst(happyComponent);
		ComponentTooltipAppenderRegistry.addLast(sadComponent);
		ComponentTooltipAppenderRegistry.addBefore(DataComponents.UNBREAKABLE, sadderComponent);
		ComponentTooltipAppenderRegistry.addAfter(DataComponents.LORE, saddestComponent);

		DefaultItemComponentEvents.MODIFY.register(context -> {
			context.modify(Items.GOLDEN_SWORD, builder -> builder.set(happyComponent, TestComponent.ONE));
			context.modify(Items.PIG_SPAWN_EGG, builder -> builder.set(sadComponent, TestComponent.TWO));
			context.modify(Items.GOLDEN_SWORD, builder -> builder.set(sadderComponent, TestComponent.THREE));
			context.modify(Items.PIG_SPAWN_EGG, builder -> builder.set(saddestComponent, TestComponent.FOUR));
		});
	}

	private interface TestComponent extends TooltipProvider {
		TestComponent ONE = (context, textConsumer, type, components) -> {
			for (int i = 0; i < 14; i++) {
				textConsumer.accept(Component.literal("This Item is Happy :)").withStyle(s -> s.withColor(0xFFFF00).withItalic(true)));
			}
		};

		TestComponent TWO = (context, textConsumer, type, components) -> textConsumer.accept(Component.literal("This Item is Sad :("));

		TestComponent THREE = (context, textConsumer, type, components) -> textConsumer.accept(Component.literal("This Item is Sadder :'("));

		TestComponent FOUR = (context, textConsumer, type, components) -> textConsumer.accept(Component.literal("This Item is the Saddest :"));
	}
}

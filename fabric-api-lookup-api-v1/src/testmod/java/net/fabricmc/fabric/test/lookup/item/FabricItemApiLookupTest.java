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

package net.fabricmc.fabric.test.lookup.item;

import static net.fabricmc.fabric.test.lookup.FabricApiLookupTest.ensureException;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.test.lookup.FabricApiLookupTest;
import net.fabricmc.fabric.test.lookup.api.Inspectable;

public class FabricItemApiLookupTest {
	public static final ItemApiLookup<Inspectable, Void> INSPECTABLE =
			ItemApiLookup.get(Identifier.fromNamespaceAndPath("testmod", "inspectable"), Inspectable.class, Void.class);

	public static final ResourceKey<Item> HELLO_ITEM_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FabricApiLookupTest.MOD_ID, "hello"));
	public static final InspectableItem HELLO_ITEM = new InspectableItem("Hello Fabric API tester!", new Item.Properties().setId(HELLO_ITEM_KEY));

	public static void onInitialize() {
		Registry.register(BuiltInRegistries.ITEM, HELLO_ITEM_KEY, HELLO_ITEM);

		// Diamonds and diamond blocks can be inspected and will also print their name.
		INSPECTABLE.registerForItems((stack, ignored) -> () -> {
			if (stack.has(DataComponents.CUSTOM_NAME)) {
				return stack.getHoverName();
			} else {
				return Component.literal("Unnamed gem.");
			}
		}, Items.DIAMOND, Items.DIAMOND_BLOCK);
		// Test registerSelf
		INSPECTABLE.registerSelf(HELLO_ITEM);

		testSelfRegistration();
	}

	private static void testSelfRegistration() {
		ensureException(() -> {
			INSPECTABLE.registerSelf(Items.WATER_BUCKET);
		}, "The ItemApiLookup should have prevented self-registration of incompatible items.");
	}
}

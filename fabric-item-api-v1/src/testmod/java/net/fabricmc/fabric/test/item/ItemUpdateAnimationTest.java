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

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

import net.fabricmc.api.ModInitializer;

public class ItemUpdateAnimationTest implements ModInitializer {
	public static final DataComponentType<Integer> TICKS = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "ticks"),
																			DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

	@Override
	public void onInitialize() {
		ResourceKey<Item> updatingAllowedKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "updating_allowed"));
		ResourceKey<Item> updatingDisallowedKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "updating_disallowed"));

		Registry.register(BuiltInRegistries.ITEM, updatingAllowedKey, new UpdatingItem(true, new Item.Properties().setId(updatingAllowedKey)));
		Registry.register(BuiltInRegistries.ITEM, updatingDisallowedKey, new UpdatingItem(false, new Item.Properties().setId(updatingDisallowedKey)));
	}
}

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

import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.ModInitializer;

public class CreatorNamespaceTest implements ModInitializer {
	public static final Codec<String> NAMESPACE_CODEC = Codec.STRING.validate((string) -> {
		if (Identifier.isValidNamespace(string)) return DataResult.success(string);
		else {
			return DataResult.error(() -> "Non [a-z0-9_.-] character in namespace "+string);
		}
	});
	public static final DataComponentType<String> MOD_NAMESPACE = registerComponent("mod_namespace", (builder) -> builder.persistent(NAMESPACE_CODEC).networkSynchronized(ByteBufCodecs.STRING_UTF8));
	public static final Item NAMESPACE_TEST_ITEM = registerItem("namespace_test", TestItem::new);

	@Override
	public void onInitialize() {
	}

	public static Item registerItem(String id, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(id));

		Item item = factory.apply(new Item.Properties().setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	private static <T> DataComponentType<T> registerComponent(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id(id), builderOperator.apply(DataComponentType.builder()).build());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", path);
	}

	public static class TestItem extends Item {
		public TestItem(Properties settings) {
			super(settings);
		}

		@Override
		public String getCreatorNamespace(ItemStack stack) {
			return stack.getOrDefault(MOD_NAMESPACE, super.getCreatorNamespace(stack));
		}
	}
}

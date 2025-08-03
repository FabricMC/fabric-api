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

import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

public class CreatorNamespaceTest implements ModInitializer {
	public static final Codec<String> NAMESPACE_CODEC = Codec.STRING.validate((string) -> {
		if (Identifier.isNamespaceValid(string)) return DataResult.success(string);
		else {
			return DataResult.error(() -> "Non [a-z0-9_.-] character in namespace "+string);
		}
	});
	public static final ComponentType<String> MOD_NAMESPACE = registerComponent("mod_namespace", (builder) -> builder.codec(NAMESPACE_CODEC).packetCodec(PacketCodecs.STRING));
	public static final Item NAMESPACE_TEST_ITEM = registerItem("namespace_test", TestItem::new);

	@Override
	public void onInitialize() {
	}

	public static Item registerItem(String id, Function<Item.Settings, Item> factory) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id(id));

		Item item = factory.apply(new Item.Settings().registryKey(key));
		return Registry.register(Registries.ITEM, key, item);
	}

	private static <T> ComponentType<T> registerComponent(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
		return Registry.register(Registries.DATA_COMPONENT_TYPE, id(id), builderOperator.apply(ComponentType.builder()).build());
	}

	public static Identifier id(String path) {
		return Identifier.of("fabric-item-api-v1-testmod", path);
	}

	public static class TestItem extends Item {
		public TestItem(Settings settings) {
			super(settings);
		}

		@Override
		public String getCreatorNamespace(ItemStack stack) {
			return stack.getOrDefault(MOD_NAMESPACE, super.getCreatorNamespace(stack));
		}
	}
}

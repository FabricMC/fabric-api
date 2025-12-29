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

package net.fabricmc.fabric.api.creativetab.v1;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import net.fabricmc.fabric.impl.creativetab.FabricCreativeModeTabBuilderImpl;

/**
 * Contains a method to create a creative mode tab builder.
 */
public final class FabricCreativeModeTab {
	private FabricCreativeModeTab() {
	}

	/**
	 * Creates a new builder for {@link CreativeModeTab}. Creative Mode Tab are used to group items in the creative
	 * inventory.
	 *
	 * <p>You must register the newly created {@link CreativeModeTab} to the {@link BuiltInRegistries#CREATIVE_MODE_TAB} registry.
	 *
	 * <p>You must also set a display name by calling {@link CreativeModeTab.Builder#title(Component)}
	 *
	 * <p>Example:
	 *
	 * <pre>{@code
	 * private static final ResourceKey<CreativeModeTab> CREATIVE_MODE_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("modid", "custom_group"));
	 *
	 * @Override
	 * public void onInitialize() {
	 *    Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_MODE_TAB, FabricCreativeModeTab.builder()
	 *       .title(Component.translatable("modid.test_group"))
	 *       .icon(() -> new ItemStack(Items.DIAMOND))
	 *       .displayItems((context, output) -> {
	 *          output.accept(TEST_ITEM);
	 *       })
	 *       .build()
	 *    );
	 * }
	 * }</pre>
	 *
	 * @return a new {@link CreativeModeTab.Builder} instance
	 */
	public static CreativeModeTab.Builder builder() {
		return new FabricCreativeModeTabBuilderImpl();
	}
}

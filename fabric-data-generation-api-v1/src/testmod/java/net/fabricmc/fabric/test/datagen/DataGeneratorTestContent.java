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

package net.fabricmc.fabric.test.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class DataGeneratorTestContent implements ModInitializer {
	public static final String MOD_ID = "fabric-data-gen-api-v1-testmod";

	public static Block SIMPLE_BLOCK;
	public static Block BLOCK_WITHOUT_ITEM;
	public static Block BLOCK_WITHOUT_LOOT_TABLE;
	public static Block BLOCK_WITH_VANILLA_LOOT_TABLE;
	public static Block BLOCK_THAT_DROPS_NOTHING;

	public static SoundEvent TEST_SOUND;

	public static EntityType<?> SIMPLE_ENTITY_TYPE;
	public static EntityType<?> ENTITY_TYPE_WITHOUT_LOOT_TABLE;

	public static final ResourceKey<CreativeModeTab> SIMPLE_ITEM_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "simple"));

	public static final ResourceKey<Registry<TestDatagenObject>> TEST_DATAGEN_DYNAMIC_REGISTRY_KEY =
			ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("fabric", "test_datagen_dynamic"));
	public static final ResourceKey<TestDatagenObject> TEST_DYNAMIC_REGISTRY_ITEM_KEY = ResourceKey.create(
			TEST_DATAGEN_DYNAMIC_REGISTRY_KEY,
			Identifier.fromNamespaceAndPath(MOD_ID, "tiny_potato")
	);
	public static final ResourceKey<TestDatagenObject> TEST_DYNAMIC_REGISTRY_EXTRA_ITEM_KEY = ResourceKey.create(
			TEST_DATAGEN_DYNAMIC_REGISTRY_KEY,
			Identifier.fromNamespaceAndPath(MOD_ID, "tinier_potato")
	);
	// Empty registry
	public static final ResourceKey<Registry<TestDatagenObject>> TEST_DATAGEN_DYNAMIC_EMPTY_REGISTRY_KEY =
			ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("fabric", "test_datagen_dynamic_empty"));

	@Override
	public void onInitialize() {
		SIMPLE_BLOCK = createBlock("simple_block", true, BlockBehaviour.Properties.of());
		BLOCK_WITHOUT_ITEM = createBlock("block_without_item", false, BlockBehaviour.Properties.of());
		BLOCK_WITHOUT_LOOT_TABLE = createBlock("block_without_loot_table", false, BlockBehaviour.Properties.of());
		BLOCK_WITH_VANILLA_LOOT_TABLE = createBlock("block_with_vanilla_loot_table", false, BlockBehaviour.Properties.of().overrideLootTable(Blocks.STONE.getLootTable()));
		BLOCK_THAT_DROPS_NOTHING = createBlock("block_that_drops_nothing", false, BlockBehaviour.Properties.of().noLootTable());

		SIMPLE_ENTITY_TYPE = createEntityType("simple_entity", EntityType.Builder.createNothing(MobCategory.MISC));
		ENTITY_TYPE_WITHOUT_LOOT_TABLE = createEntityType("entity_without_loot_table", EntityType.Builder.createNothing(MobCategory.MISC));

		ItemGroupEvents.modifyEntriesEvent(SIMPLE_ITEM_GROUP).register(entries -> entries.accept(SIMPLE_BLOCK));

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SIMPLE_ITEM_GROUP, FabricItemGroup.builder()
				.icon(() -> new ItemStack(Items.DIAMOND_PICKAXE))
				.title(Component.translatable("fabric-data-gen-api-v1-testmod.simple_item_group"))
				.build());

		TEST_SOUND = Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "test_sound"), SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "test_sound")));

		DynamicRegistries.register(TEST_DATAGEN_DYNAMIC_REGISTRY_KEY, TestDatagenObject.CODEC);
		DynamicRegistries.register(TEST_DATAGEN_DYNAMIC_EMPTY_REGISTRY_KEY, TestDatagenObject.CODEC);
	}

	private static Block createBlock(String name, boolean hasItem, BlockBehaviour.Properties settings) {
		Identifier identifier = Identifier.fromNamespaceAndPath(MOD_ID, name);
		Block block = Registry.register(BuiltInRegistries.BLOCK, identifier, new Block(settings.setId(ResourceKey.create(Registries.BLOCK, identifier))));

		if (hasItem) {
			Registry.register(BuiltInRegistries.ITEM, identifier, new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, identifier))));
		}

		return block;
	}

	private static <E extends Entity> EntityType<E> createEntityType(String name, EntityType.Builder<E> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name));

		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public record TestDatagenObject(String value) {
		public static final Codec<TestDatagenObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("value").forGetter(TestDatagenObject::value)
		).apply(instance, TestDatagenObject::new));
	}
}

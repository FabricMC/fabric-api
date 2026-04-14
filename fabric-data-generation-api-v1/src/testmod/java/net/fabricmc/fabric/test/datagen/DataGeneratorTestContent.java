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
import net.minecraft.references.BlockItemId;
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
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public class DataGeneratorTestContent implements ModInitializer {
	public static final String MOD_ID = "fabric-data-gen-api-v1-testmod";

	public static Block SIMPLE_BLOCK;
	public static Block BLOCK_WITHOUT_ITEM;
	public static Block BLOCK_WITHOUT_LOOT_TABLE;
	public static Block BLOCK_WITH_VANILLA_LOOT_TABLE;
	public static Block BLOCK_THAT_DROPS_NOTHING;

	public static BlockItemId SIMPLE_BLOCK_KEY = createBlockItemId("simple_block");
	public static ResourceKey<Block> BLOCK_WITHOUT_ITEM_KEY = createBlockResourceKey("block_without_item");
	public static ResourceKey<Block> BLOCK_WITHOUT_LOOT_TABLE_KEY = createBlockResourceKey("block_without_loot_table");
	public static ResourceKey<Block> BLOCK_WITH_VANILLA_LOOT_TABLE_KEY = createBlockResourceKey("block_with_vanilla_loot_table");
	public static ResourceKey<Block> BLOCK_THAT_DROPS_NOTHING_KEY = createBlockResourceKey("block_that_drops_nothing");

	public static SoundEvent TEST_SOUND;

	public static EntityType<?> SIMPLE_ENTITY_TYPE;
	public static EntityType<?> ENTITY_TYPE_WITHOUT_LOOT_TABLE;

	public static ResourceKey<EntityType<?>> SIMPLE_ENTITY_TYPE_KEY = createEntityTypeResourceKey("simple_entity");
	public static ResourceKey<EntityType<?>> ENTITY_TYPE_WITHOUT_LOOT_TABLE_KEY = createEntityTypeResourceKey("entity_without_loot_table");

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
		SIMPLE_BLOCK = createBlockItem(SIMPLE_BLOCK_KEY, BlockBehaviour.Properties.of());
		BLOCK_WITHOUT_ITEM = createBlock(BLOCK_WITHOUT_ITEM_KEY, BlockBehaviour.Properties.of());
		BLOCK_WITHOUT_LOOT_TABLE = createBlock(BLOCK_WITHOUT_LOOT_TABLE_KEY, BlockBehaviour.Properties.of());
		BLOCK_WITH_VANILLA_LOOT_TABLE = createBlock(BLOCK_WITH_VANILLA_LOOT_TABLE_KEY, BlockBehaviour.Properties.of().overrideLootTable(Blocks.STONE.getLootTable()));
		BLOCK_THAT_DROPS_NOTHING = createBlock(BLOCK_THAT_DROPS_NOTHING_KEY, BlockBehaviour.Properties.of().noLootTable());

		SIMPLE_ENTITY_TYPE = createEntityType(SIMPLE_ENTITY_TYPE_KEY, EntityType.Builder.createNothing(MobCategory.MISC));
		ENTITY_TYPE_WITHOUT_LOOT_TABLE = createEntityType(ENTITY_TYPE_WITHOUT_LOOT_TABLE_KEY, EntityType.Builder.createNothing(MobCategory.MISC));

		CreativeModeTabEvents.modifyOutputEvent(SIMPLE_ITEM_GROUP).register(entries -> entries.accept(SIMPLE_BLOCK));

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SIMPLE_ITEM_GROUP, FabricCreativeModeTab.builder()
				.icon(() -> new ItemStack(Items.DIAMOND_PICKAXE))
				.title(Component.translatable("fabric-data-gen-api-v1-testmod.simple_item_group"))
				.build());

		TEST_SOUND = Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "test_sound"), SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "test_sound")));

		DynamicRegistries.register(TEST_DATAGEN_DYNAMIC_REGISTRY_KEY, TestDatagenObject.CODEC);
		DynamicRegistries.register(TEST_DATAGEN_DYNAMIC_EMPTY_REGISTRY_KEY, TestDatagenObject.CODEC);
	}

	private static BlockItemId createBlockItemId(String name) {
		Identifier identifier = Identifier.fromNamespaceAndPath(MOD_ID, name);
		return BlockItemId.create(identifier, identifier);
	}

	private static ResourceKey<Block> createBlockResourceKey(String name) {
		return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
	}

	private static Block createBlock(ResourceKey<Block> key, BlockBehaviour.Properties settings) {
		return Registry.register(BuiltInRegistries.BLOCK, key, new Block(settings.setId(key)));
	}

	private static Block createBlockItem(BlockItemId id, BlockBehaviour.Properties settings) {
		Block block = createBlock(id.block(), settings);
		Registry.register(BuiltInRegistries.ITEM, id.item(), new BlockItem(block, new Item.Properties().setId(id.item())));
		return block;
	}

	private static ResourceKey<EntityType<?>> createEntityTypeResourceKey(String name) {
		return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name));
	}

	private static <E extends Entity> EntityType<E> createEntityType(ResourceKey<EntityType<?>> key, EntityType.Builder<E> builder) {
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public record TestDatagenObject(String value) {
		public static final Codec<TestDatagenObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("value").forGetter(TestDatagenObject::value)
		).apply(instance, TestDatagenObject::new));
	}
}

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

package net.fabricmc.fabric.test.lookup;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.test.lookup.api.ItemApis;
import net.fabricmc.fabric.test.lookup.api.ItemInsertable;
import net.fabricmc.fabric.test.lookup.compat.InventoryExtractableProvider;
import net.fabricmc.fabric.test.lookup.compat.InventoryInsertableProvider;
import net.fabricmc.fabric.test.lookup.entity.FabricEntityApiLookupTest;
import net.fabricmc.fabric.test.lookup.item.FabricItemApiLookupTest;

public class FabricApiLookupTest implements ModInitializer {
	public static final String MOD_ID = "fabric-lookup-api-v1-testmod";
	// Chute - Block without model that transfers item from the container above to the container below.
	// It's meant to work with unsided containers: chests, dispensers, droppers and hoppers.
	public static final RegistryKey<Block> CHUTE_BLOCK_KEY = keyOf("chute");
	public static final ChuteBlock CHUTE_BLOCK = new ChuteBlock(AbstractBlock.Settings.create().registryKey(CHUTE_BLOCK_KEY));
	public static final BlockItem CHUTE_ITEM = new BlockItem(CHUTE_BLOCK, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, CHUTE_BLOCK_KEY.getValue())));
	public static BlockEntityType<ChuteBlockEntity> CHUTE_BLOCK_ENTITY_TYPE;
	// Cobble gen - Block without model that can generate infinite cobblestone when placed above a chute.
	// It's meant to test BlockApiLookup#registerSelf.
	public static final RegistryKey<Block> COBBLE_GEN_BLOCK_KEY = keyOf("cobble_gen");
	public static final CobbleGenBlock COBBLE_GEN_BLOCK = new CobbleGenBlock(AbstractBlock.Settings.create().registryKey(COBBLE_GEN_BLOCK_KEY));
	public static final BlockItem COBBLE_GEN_ITEM = new BlockItem(COBBLE_GEN_BLOCK, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, COBBLE_GEN_BLOCK_KEY.getValue())));
	public static BlockEntityType<CobbleGenBlockEntity> COBBLE_GEN_BLOCK_ENTITY_TYPE;
	// Testing for item api lookups is done in the `item` package.

	public static final RegistryKey<Block> INSPECTOR_BLOCK_KEY = keyOf("inspector");
	public static final InspectorBlock INSPECTOR_BLOCK = new InspectorBlock(AbstractBlock.Settings.create().registryKey(INSPECTOR_BLOCK_KEY));
	public static final BlockItem INSPECTOR_ITEM = new BlockItem(INSPECTOR_BLOCK, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, INSPECTOR_BLOCK_KEY.getValue())));

	private static RegistryKey<Block> keyOf(String id) {
		return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, id));
	}

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, CHUTE_BLOCK_KEY, CHUTE_BLOCK);
		Registry.register(Registries.ITEM, CHUTE_BLOCK_KEY.getValue(), CHUTE_ITEM);
		CHUTE_BLOCK_ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, CHUTE_BLOCK_KEY.getValue(), FabricBlockEntityTypeBuilder.create(ChuteBlockEntity::new, CHUTE_BLOCK).build());

		Registry.register(Registries.BLOCK, COBBLE_GEN_BLOCK_KEY, COBBLE_GEN_BLOCK);
		Registry.register(Registries.ITEM, COBBLE_GEN_BLOCK_KEY.getValue(), COBBLE_GEN_ITEM);
		COBBLE_GEN_BLOCK_ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, COBBLE_GEN_BLOCK_KEY.getValue(), FabricBlockEntityTypeBuilder.create(CobbleGenBlockEntity::new, COBBLE_GEN_BLOCK).build());

		InventoryExtractableProvider extractableProvider = new InventoryExtractableProvider();
		InventoryInsertableProvider insertableProvider = new InventoryInsertableProvider();

		ItemApis.INSERTABLE.registerForBlockEntities(insertableProvider, BlockEntityType.CHEST, BlockEntityType.DISPENSER, BlockEntityType.DROPPER, BlockEntityType.HOPPER);
		ItemApis.EXTRACTABLE.registerForBlockEntities(extractableProvider, BlockEntityType.CHEST, BlockEntityType.DISPENSER, BlockEntityType.DROPPER, BlockEntityType.HOPPER);
		ItemApis.EXTRACTABLE.registerSelf(COBBLE_GEN_BLOCK_ENTITY_TYPE);

		testLookupRegistry();
		testSelfRegistration();

		Registry.register(Registries.BLOCK, INSPECTOR_BLOCK_KEY, INSPECTOR_BLOCK);
		Registry.register(Registries.ITEM, INSPECTOR_BLOCK_KEY.getValue(), INSPECTOR_ITEM);

		FabricItemApiLookupTest.onInitialize();
		FabricEntityApiLookupTest.onInitialize();
	}

	private static void testLookupRegistry() {
		BlockApiLookup<ItemInsertable, Direction> insertable2 = BlockApiLookup.get(Identifier.of("testmod", "item_insertable"), ItemInsertable.class, Direction.class);

		if (insertable2 != ItemApis.INSERTABLE) {
			throw new AssertionError("The registry should have returned the same instance.");
		}

		ensureException(() -> {
			BlockApiLookup<Void, Void> wrongInsertable = BlockApiLookup.get(Identifier.of("testmod", "item_insertable"), Void.class, Void.class);
			wrongInsertable.registerFallback((world, pos, state, be, nocontext) -> null);
		}, "The registry should have prevented creation of another instance with different classes, but same id.");

		if (!insertable2.getId().equals(Identifier.of("testmod", "item_insertable"))) {
			throw new AssertionError("Incorrect identifier was returned.");
		}

		if (insertable2.apiClass() != ItemInsertable.class) {
			throw new AssertionError("Incorrect API class was returned.");
		}

		if (insertable2.contextClass() != Direction.class) {
			throw new AssertionError("Incorrect context class was returned.");
		}
	}

	private static void testSelfRegistration() {
		ensureException(() -> {
			ItemApis.INSERTABLE.registerSelf(COBBLE_GEN_BLOCK_ENTITY_TYPE);
		}, "The BlockApiLookup should have prevented self-registration of incompatible block entity types.");
	}

	public static void ensureException(Runnable runnable, String message) {
		boolean failed = false;

		try {
			runnable.run();
		} catch (Throwable t) {
			failed = true;
		}

		if (!failed) {
			throw new AssertionError(message);
		}
	}
}

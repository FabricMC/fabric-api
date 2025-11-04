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

package net.fabricmc.fabric.test.transfer.ingame;

import com.mojang.brigadier.arguments.LongArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

public class TransferTestInitializer implements ModInitializer {
	public static final String MOD_ID = "fabric-transfer-api-v1-testmod";

	private static final ResourceKey<Block> INFINITE_WATER_SOURCE_KEY = block("infinite_water_source");
	private static final Block INFINITE_WATER_SOURCE = new Block(BlockBehaviour.Properties.of().setId(INFINITE_WATER_SOURCE_KEY));
	private static final ResourceKey<Block> INFINITE_LAVA_SOURCE_KEY = block("infinite_lava_source");
	private static final Block INFINITE_LAVA_SOURCE = new Block(BlockBehaviour.Properties.of().setId(INFINITE_LAVA_SOURCE_KEY));
	private static final ResourceKey<Block> FLUID_CHUTE_KEY = block("fluid_chute");
	private static final Block FLUID_CHUTE = new FluidChuteBlock(BlockBehaviour.Properties.of().setId(FLUID_CHUTE_KEY));
	private static final ResourceKey<Item> EXTRACT_STICK_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "extract_stick"));
	private static final Item EXTRACT_STICK = new ExtractStickItem(new Item.Properties().setId(EXTRACT_STICK_KEY));
	public static BlockEntityType<FluidChuteBlockEntity> FLUID_CHUTE_TYPE;

	@Override
	public void onInitialize() {
		registerBlock(INFINITE_WATER_SOURCE_KEY, INFINITE_WATER_SOURCE);
		registerBlock(INFINITE_LAVA_SOURCE_KEY, INFINITE_LAVA_SOURCE);
		registerBlock(FLUID_CHUTE_KEY, FLUID_CHUTE);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "extract_stick"), EXTRACT_STICK);

		FLUID_CHUTE_TYPE = FabricBlockEntityTypeBuilder.create(FluidChuteBlockEntity::new, FLUID_CHUTE).build();
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "fluid_chute"), FLUID_CHUTE_TYPE);

		FluidStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> CreativeStorage.WATER, INFINITE_WATER_SOURCE);
		FluidStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> CreativeStorage.LAVA, INFINITE_LAVA_SOURCE);

		// Obsidian is now a trash can :-P
		ItemStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> TrashingStorage.ITEM, Blocks.OBSIDIAN);
		// And diamond ore blocks are an infinite source of diamonds! Yay!
		ItemStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> CreativeStorage.DIAMONDS, Blocks.DIAMOND_ORE);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					Commands.literal("fabric_insertintoheldstack")
							.then(Commands.argument("stack", ItemArgument.item(registryAccess))
									.then(Commands.argument("count", LongArgumentType.longArg(1))
											.executes(context -> {
												ItemVariant variant = ItemVariant.of(ItemArgument.getItem(context, "stack")
														.createItemStack(1, false));

												ContainerItemContext containerCtx = ContainerItemContext.ofPlayerHand(context.getSource().getPlayerOrException(), InteractionHand.MAIN_HAND);
												Storage<ItemVariant> storage = containerCtx.find(ItemStorage.ITEM);

												if (storage == null) {
													context.getSource().sendSystemMessage(Component.literal("no storage found"));
													return 0;
												}

												long inserted;

												try (Transaction tx = Transaction.openOuter()) {
													inserted = storage.insert(
															variant,
															LongArgumentType.getLong(context, "count"),
															tx
													);
													tx.commit();
												}

												context.getSource().sendSystemMessage(Component.literal("inserted " + inserted + " items"));

												return (int) inserted;
											})))
			);

			dispatcher.register(
					Commands.literal("fabric_extractfromheldstack")
							.then(Commands.argument("stack", ItemArgument.item(registryAccess))
									.then(Commands.argument("count", LongArgumentType.longArg(1))
											.executes(context -> {
												ItemVariant variant = ItemVariant.of(ItemArgument.getItem(context, "stack")
														.createItemStack(1, false));

												ContainerItemContext containerCtx = ContainerItemContext.ofPlayerHand(context.getSource().getPlayerOrException(), InteractionHand.MAIN_HAND);
												Storage<ItemVariant> storage = containerCtx.find(ItemStorage.ITEM);

												if (storage == null) {
													context.getSource().sendSystemMessage(Component.literal("no storage found"));
													return 0;
												}

												long extracted;

												try (Transaction tx = Transaction.openOuter()) {
													extracted = storage.extract(
															variant,
															LongArgumentType.getLong(context, "count"),
															tx
													);
													tx.commit();
												}

												context.getSource().sendSystemMessage(Component.literal("extracted " + extracted + " items"));

												return (int) extracted;
											})))
			);
		});
	}

	private static ResourceKey<Block> block(String name) {
		return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
	}

	private static void registerBlock(ResourceKey<Block> key, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, key, block);
		Registry.register(BuiltInRegistries.ITEM, key.identifier(), new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key.identifier()))));
	}
}

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

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

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

	private static final Block INFINITE_WATER_SOURCE = new Block(AbstractBlock.Settings.create());
	private static final Block INFINITE_LAVA_SOURCE = new Block(AbstractBlock.Settings.create());
	private static final Block FLUID_CHUTE = new FluidChuteBlock();
	private static final Item EXTRACT_STICK = new ExtractStickItem();
	public static BlockEntityType<FluidChuteBlockEntity> FLUID_CHUTE_TYPE;

	@Override
	public void onInitialize() {
		registerBlock(INFINITE_WATER_SOURCE, "infinite_water_source");
		registerBlock(INFINITE_LAVA_SOURCE, "infinite_lava_source");
		registerBlock(FLUID_CHUTE, "fluid_chute");
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "extract_stick"), EXTRACT_STICK);

		FLUID_CHUTE_TYPE = FabricBlockEntityTypeBuilder.create(FluidChuteBlockEntity::new, FLUID_CHUTE).build();
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "fluid_chute"), FLUID_CHUTE_TYPE);

		FluidStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> CreativeStorage.WATER, INFINITE_WATER_SOURCE);
		FluidStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> CreativeStorage.LAVA, INFINITE_LAVA_SOURCE);

		// Obsidian is now a trash can :-P
		ItemStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> TrashingStorage.ITEM, Blocks.OBSIDIAN);
		// And diamond ore blocks are an infinite source of diamonds! Yay!
		ItemStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> CreativeStorage.DIAMONDS, Blocks.DIAMOND_ORE);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					CommandManager.literal("fabric_insertintoheldstack")
							.then(CommandManager.argument("stack", ItemStackArgumentType.itemStack(registryAccess))
									.then(CommandManager.argument("count", LongArgumentType.longArg(1))
											.executes(context -> {
												ItemVariant variant = ItemVariant.of(ItemStackArgumentType.getItemStackArgument(context, "stack")
														.createStack(1, false));

												ContainerItemContext containerCtx = ContainerItemContext.ofPlayerHand(context.getSource().getPlayerOrThrow(), Hand.MAIN_HAND);
												Storage<ItemVariant> storage = containerCtx.find(ItemStorage.ITEM);

												if (storage == null) {
													context.getSource().sendMessage(Text.literal("no storage found"));
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

												context.getSource().sendMessage(Text.literal("inserted " + inserted + " items"));

												return (int) inserted;
											})))
			);

			dispatcher.register(
					CommandManager.literal("fabric_extractfromheldstack")
							.then(CommandManager.argument("stack", ItemStackArgumentType.itemStack(registryAccess))
									.then(CommandManager.argument("count", LongArgumentType.longArg(1))
											.executes(context -> {
												ItemVariant variant = ItemVariant.of(ItemStackArgumentType.getItemStackArgument(context, "stack")
														.createStack(1, false));

												ContainerItemContext containerCtx = ContainerItemContext.ofPlayerHand(context.getSource().getPlayerOrThrow(), Hand.MAIN_HAND);
												Storage<ItemVariant> storage = containerCtx.find(ItemStorage.ITEM);

												if (storage == null) {
													context.getSource().sendMessage(Text.literal("no storage found"));
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

												context.getSource().sendMessage(Text.literal("extracted " + extracted + " items"));

												return (int) extracted;
											})))
			);
		});
	}

	private static void registerBlock(Block block, String name) {
		Identifier id = Identifier.of(MOD_ID, name);
		Registry.register(Registries.BLOCK, id, block);
		Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
	}
}

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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.ModInitializer;

public class UseOnItemFirstTest implements ModInitializer {
	public static final ResourceKey<Item> BLOCK_NAME_PRINTER_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "block_name_printer"));
	public static final TestItem BLOCK_NAME_PRINTER = new TestItem(new Item.Properties().setId(BLOCK_NAME_PRINTER_KEY));

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ITEM, BLOCK_NAME_PRINTER_KEY, BLOCK_NAME_PRINTER);
	}

	public static class TestItem extends Item {
		public TestItem(Properties properties) {
			super(properties);
		}

		@Override
		public InteractionResult useOnItemFirst(UseOnContext useOnContext) {
			Level level = useOnContext.getLevel();
			BlockPos blockPos = useOnContext.getClickedPos();
			BlockState state = level.getBlockState(blockPos);
			Block block = state.getBlock();

			Player player = useOnContext.getPlayer();

			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
						Component.literal("Block: ").append(Component.translatable(block.getDescriptionId()))
				));
			} else {
				System.out.println("Client-side useOnItemFirst item interaction on block: " + block.getDescriptionId());
			}

			return InteractionResult.SUCCESS;
		}
	}
}

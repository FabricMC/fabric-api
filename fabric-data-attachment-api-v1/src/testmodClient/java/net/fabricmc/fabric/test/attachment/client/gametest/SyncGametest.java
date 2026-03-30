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

package net.fabricmc.fabric.test.attachment.client.gametest;

import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.test.attachment.AttachmentTestMod;

public class SyncGametest implements FabricClientGameTest {
	public static final Logger LOGGER = LoggerFactory.getLogger("data-attachment-syncing-gametest");

	private static ServerPlayer getSinglePlayer(MinecraftServer server) {
		return server.getPlayerList().getPlayers().getFirst();
	}

	private static void setSyncedWithAll(AttachmentTarget target) {
		set(target, AttachmentTestMod.SYNCED_WITH_ALL);
	}

	private static void set(AttachmentTarget target, AttachmentType<Boolean> type) {
		target.setAttached(type, true);
	}

	private static void assertHasSyncedWithAll(AttachmentTarget target) {
		assertHasSynced(target, AttachmentTestMod.SYNCED_WITH_ALL);
	}

	private static void assertHasSynced(AttachmentTarget target, AttachmentType<?> type) {
		assertPresence(target, type, true);
	}

	private static void assertHasNotSynced(AttachmentTarget target, AttachmentType<?> type) {
		assertPresence(target, type, false);
	}

	private static void assertPresence(AttachmentTarget target, AttachmentType<?> type, boolean expected) {
		if (Objects.requireNonNull(target).hasAttached(type) != expected) {
			throw new AssertionError("Synced attachment %s not present on %s".formatted(type.identifier(), target));
		}
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		Properties serverProps = new Properties();
		serverProps.setProperty("gamemode", "creative");

		try (TestDedicatedServerContext serverContext = context.worldBuilder().createServer(serverProps)) {
			var state = new Object() {
				BlockPos furnacePos;
				UUID villagerId;
			};

			context.runOnClient(client -> {
				// set client render distance before the server sets it
				client.options.renderDistance().set(5);
			});

			LOGGER.info("Setting up synced attachments before join");
			// setup before player joins
			serverContext.runOnServer(server -> {
				ServerLevel level = server.overworld();
				BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.ZERO);
				state.furnacePos = top;

				level.setBlockAndUpdate(top, Blocks.FURNACE.defaultBlockState());
				setSyncedWithAll(level.getBlockEntity(top, BlockEntityType.FURNACE).orElseThrow());

				var villager = new Villager(EntityType.VILLAGER, level);
				villager.setNoAi(true);
				villager.setInvulnerable(true);
				villager.setCustomName(Component.literal("TestVillager"));
				state.villagerId = villager.getUUID();
				level.addFreshEntity(villager);
				setSyncedWithAll(villager);
				set(villager, AttachmentTestMod.SYNCED_WITH_TARGET);
				villager.setAttached(AttachmentTestMod.SYNCED_LARGE, AttachmentTestMod.LARGE_DATA);
				villager.setAttached(AttachmentTestMod.SYNCED_ITEM, new ItemStack(Items.EGG));

				LevelChunk originChunk = level.getChunk(0, 0);
				setSyncedWithAll(originChunk);

				ServerLevel nether = server.getLevel(Level.NETHER);
				setSyncedWithAll(Objects.requireNonNull(nether));

				setSyncedWithAll(server.globalAttachments());
			});

			LOGGER.info("Joining dedicated server");

			try (TestServerConnection connection = serverContext.connect()) {
				connection.getClientLevel().waitForChunksDownload();

				LOGGER.info("Setting up rest of synced attachments");
				serverContext.runOnServer(server -> {
					ServerPlayer player = getSinglePlayer(server);
					setSyncedWithAll(player);
					set(player, AttachmentTestMod.SYNCED_EXCEPT_TARGET);
					set(player, AttachmentTestMod.SYNCED_CREATIVE_ONLY);

					// check registry objects are synced correctly
					player.setAttached(AttachmentTestMod.SYNCED_ITEM, Items.APPLE.getDefaultInstance());

					// check that the client changes the render distance as requested
					player.setAttached(AttachmentTestMod.SYNCED_RENDER_DISTANCE, 8);

					// check that block entity deferred syncing works correctly
					set(server.overworld().getBlockEntity(state.furnacePos), AttachmentTestMod.SYNCED_EXCEPT_TARGET);
				});

				// safety
				context.waitTick();

				LOGGER.info("Testing synced attachments (1/2)");
				context.runOnClient(client -> {
					ClientLevel level = Objects.requireNonNull(client.level);
					Entity villager = level.getEntity(state.villagerId);
					BlockEntity furnace = level.getBlockEntity(state.furnacePos);

					assertHasSyncedWithAll(furnace);
					assertHasSyncedWithAll(villager);
					assertHasSyncedWithAll(level.getChunk(0, 0));
					assertHasSyncedWithAll(client.player);
					assertHasSyncedWithAll(level.globalAttachments());
					assertHasSynced(client.player, AttachmentTestMod.SYNCED_CREATIVE_ONLY);
					assertHasSynced(client.player, AttachmentTestMod.SYNCED_ITEM);
					assertHasSynced(villager, AttachmentTestMod.SYNCED_LARGE);
					assertHasSynced(furnace, AttachmentTestMod.SYNCED_EXCEPT_TARGET);

					// `level` is the overworld here
					assertHasNotSynced(level, AttachmentTestMod.SYNCED_WITH_ALL);
					assertHasNotSynced(client.player, AttachmentTestMod.SYNCED_EXCEPT_TARGET);
					assertHasNotSynced(villager, AttachmentTestMod.SYNCED_WITH_TARGET);

					if (client.options.renderDistance().get() != 8) {
						throw new AssertionError("Client did not set render distance to server requested synced attachment.");
					}

					// reset view distance
					client.options.renderDistance().set(12);
				});

				// Test modifying attachments using the data command, and that the changes are synced to the client.
				serverContext.runCommand("data modify entity @n[name=\"TestVillager\"] \"fabric:attachments\".\"fabric-data-attachment-api-v1-testmod:synced_item\".id set value \"minecraft:diamond\"");
				context.waitTick();
				context.runOnClient(client -> {
					ClientLevel level = Objects.requireNonNull(client.level);
					Entity villager = level.getEntity(state.villagerId);
					ItemStack syncedItem = villager.getAttached(AttachmentTestMod.SYNCED_ITEM);

					if (syncedItem.getItem() != Items.DIAMOND) {
						throw new AssertionError("Unexpected synced item: %s".formatted(syncedItem.getItem()));
					}
				});

				LOGGER.info("Setting up second phase");
				// now teleport to nether, on roof to avoid suffocation when switching to survival
				serverContext.runCommand("execute in minecraft:the_nether run tp @p ~ 128 ~");
				serverContext.runCommand("gamemode survival @p");
				serverContext.runOnServer(server -> getSinglePlayer(server).removeAttached(AttachmentTestMod.SYNCED_CREATIVE_ONLY));

				// safety
				context.waitTick();

				LOGGER.info("Testing synced attachments (2/2)");
				context.runOnClient(client -> {
					assertHasSyncedWithAll(client.level);
					assertHasSyncedWithAll(client.level.globalAttachments());
					// asserts the removal wasn't synced
					assertPresence(client.player, AttachmentTestMod.SYNCED_CREATIVE_ONLY, true);
				});

				LOGGER.info("Done");
			}
		}
	}
}

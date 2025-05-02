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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.test.attachment.AttachmentTestMod;

public class SyncGametest implements FabricClientGameTest {
	public static final Logger LOGGER = LoggerFactory.getLogger("data-attachment-persistence-gametest");

	private static ServerPlayerEntity getSinglePlayer(MinecraftServer server) {
		return server.getPlayerManager().getPlayerList().getFirst();
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
				int villagerId;
			};

			context.runOnClient(client -> {
				// set client render distance before the server sets it
				client.options.getViewDistance().setValue(5);
			});

			LOGGER.info("Setting up synced attachments before join");
			// setup before player joins
			serverContext.runOnServer(server -> {
				ServerWorld world = server.getOverworld();
				BlockPos top = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BlockPos.ORIGIN);
				state.furnacePos = top;

				world.setBlockState(top, Blocks.FURNACE.getDefaultState());
				setSyncedWithAll(world.getBlockEntity(top, BlockEntityType.FURNACE).orElseThrow());

				var villager = new VillagerEntity(EntityType.VILLAGER, world);
				villager.setAiDisabled(true);
				villager.setInvulnerable(true);
				state.villagerId = villager.getId();
				world.spawnEntity(villager);
				setSyncedWithAll(villager);
				set(villager, AttachmentTestMod.SYNCED_WITH_TARGET);

				WorldChunk originChunk = world.getChunk(0, 0);
				setSyncedWithAll(originChunk);

				ServerWorld nether = server.getWorld(World.NETHER);
				setSyncedWithAll(Objects.requireNonNull(nether));
			});

			LOGGER.info("Joining dedicated server");

			try (TestServerConnection connection = serverContext.connect()) {
				connection.getClientWorld().waitForChunksDownload();

				LOGGER.info("Setting up rest of synced attachments");
				serverContext.runOnServer(server -> {
					ServerPlayerEntity player = getSinglePlayer(server);
					setSyncedWithAll(player);
					set(player, AttachmentTestMod.SYNCED_EXCEPT_TARGET);
					set(player, AttachmentTestMod.SYNCED_CREATIVE_ONLY);

					// check registry objects are synced correctly
					player.setAttached(AttachmentTestMod.SYNCED_ITEM, Items.APPLE.getDefaultStack());

					// check that the client changes the render distance as requested
					player.setAttached(AttachmentTestMod.SYNCED_RENDER_DISTANCE, 8);
				});

				// safety
				context.waitTick();

				LOGGER.info("Testing synced attachments (1/2)");
				context.runOnClient(client -> {
					ClientWorld world = Objects.requireNonNull(client.world);
					Entity villager = world.getEntityById(state.villagerId);

					assertHasSyncedWithAll(world.getBlockEntity(state.furnacePos));
					assertHasSyncedWithAll(villager);
					assertHasSyncedWithAll(world.getChunk(0, 0));
					assertHasSyncedWithAll(client.player);
					assertHasSynced(client.player, AttachmentTestMod.SYNCED_CREATIVE_ONLY);
					assertHasSynced(client.player, AttachmentTestMod.SYNCED_ITEM);

					// `world` is the overworld here
					assertHasNotSynced(world, AttachmentTestMod.SYNCED_WITH_ALL);
					assertHasNotSynced(client.player, AttachmentTestMod.SYNCED_EXCEPT_TARGET);
					assertHasNotSynced(villager, AttachmentTestMod.SYNCED_WITH_TARGET);

					if (client.options.getViewDistance().getValue() != 8) {
						throw new AssertionError("Client did not set render distance to server requested synced attachment.");
					}

					// reset view distance
					client.options.getViewDistance().setValue(12);
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
					assertHasSyncedWithAll(client.world);
					// asserts the removal wasn't synced
					assertPresence(client.player, AttachmentTestMod.SYNCED_CREATIVE_ONLY, true);
				});

				LOGGER.info("Done");
			}
		}
	}
}

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

import static net.fabricmc.fabric.test.attachment.AttachmentTestMod.PERSISTENT;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.test.attachment.AttachmentTestMod;

public class PersistenceGametest implements FabricClientGameTest {
	public static final Logger LOGGER = LoggerFactory.getLogger("data-attachment-persistence-gametest");
	public static final ChunkPos FAR_CHUNK_POS = new ChunkPos(300, 0);

	private static <T> void assertAttached(
			AttachmentTarget target, AttachmentType<T> type, T expected,
			String message
	) {
		if (!Objects.equals(expected, Objects.requireNonNull(target).getAttached(type))) {
			throw new AssertionError(message);
		}
	}

	private static ServerPlayer getSinglePlayer(MinecraftServer server) {
		return server.getPlayerList().getPlayers().getFirst();
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		TestWorldSave save;

		LOGGER.info("First launch");
		try (TestSingleplayerContext spContext = context.worldBuilder()
				.setUseConsistentSettings(false)
				.adjustSettings(worldCreator -> worldCreator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE))
				.create()) {
			save = spContext.getWorldSave();
			spContext.getClientWorld().waitForChunksDownload();

			spContext.getServer().runOnServer(server -> {
				ServerLevel overworld = server.overworld();
				LevelChunk originChunk = overworld.getChunk(0, 0);

				assertAttached(
						originChunk,
						AttachmentTestMod.FEATURE_ATTACHMENT,
						"feature_data",
						"Feature did not write attachment to ProtoChunk"
				);

				// setting up persistent attachments for second run
				getSinglePlayer(server).setAttached(PERSISTENT, "player_data");
				overworld.setAttached(PERSISTENT, "world_data");
				originChunk.setAttached(PERSISTENT, "chunk_data");

				ProtoChunk farChunk = (ProtoChunk) overworld.getChunkSource()
						.getChunk(FAR_CHUNK_POS.x, FAR_CHUNK_POS.z, ChunkStatus.STRUCTURE_STARTS, true);
				farChunk.setAttached(PERSISTENT, "protochunk_data");
				LOGGER.info("Set persistent attachments");
			});
		}

		LOGGER.info("Second launch");

		// second launch
		try (TestSingleplayerContext spContext = save.open()) {
			spContext.getClientWorld().waitForChunksDownload();

			LOGGER.info("Testing persistent attachments");
			spContext.getServer().runOnServer(server -> {
				ServerLevel overworld = server.overworld();
				LevelChunk originChunk = overworld.getChunk(0, 0);

				assertAttached(getSinglePlayer(server), PERSISTENT, "player_data", "Player attachment did not persist");
				assertAttached(overworld, PERSISTENT, "world_data", "World attachment did not persist");
				assertAttached(originChunk, PERSISTENT, "chunk_data", "WorldChunk attachment did not persist");

				ImposterProtoChunk wrapperProtoChunk = (ImposterProtoChunk) overworld.getChunkSource()
						.getChunk(0, 0, ChunkStatus.EMPTY, true);
				assertAttached(
						wrapperProtoChunk, PERSISTENT, "chunk_data",
						"Attachment is not accessible through WrapperProtoChunk"
				);

				ChunkAccess farChunk = overworld.getChunkSource()
						.getChunk(FAR_CHUNK_POS.x, FAR_CHUNK_POS.z, ChunkStatus.EMPTY, true);

				if (farChunk instanceof ImposterProtoChunk) {
					LOGGER.warn("Far chunk already generated, can't test persistence in ProtoChunk.");
				}

				assertAttached(farChunk, PERSISTENT, "protochunk_data", "ProtoChunk attachment did not persist");
			});

			LOGGER.info("Testing ProtoChunk transfer");
			// load far chunk
			spContext.getServer().runCommand("tp @p 4800 ~ 0");
			spContext.getClientWorld().waitForChunksDownload();

			spContext.getServer().runOnServer(server -> {
				LevelChunk farChunk = server.overworld().getChunk(FAR_CHUNK_POS.x, FAR_CHUNK_POS.z);

				assertAttached(
						farChunk,
						PERSISTENT,
						"protochunk_data",
						"ProtoChunk attachment was not transferred to WorldChunk"
				);
			});

			LOGGER.info("Done");
		}
	}
}

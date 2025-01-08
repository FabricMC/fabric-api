package net.fabricmc.fabric.test.attachment.client.gametest;

import static net.fabricmc.fabric.test.attachment.AttachmentTestMod.PERSISTENT;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestWorldSave;
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

	private static ServerPlayerEntity getSinglePlayer(MinecraftServer server) {
		return server.getPlayerManager().getPlayerList().getFirst();
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		TestWorldSave save;

		try (TestSingleplayerContext spContext = context.worldBuilder()
				.adjustSettings(worldCreator -> worldCreator.setGameMode(WorldCreator.Mode.CREATIVE))
				.create()) {
			save = spContext.getWorldSave();

			spContext.getServer().runOnServer(server -> {
				ServerWorld overworld = server.getOverworld();
				WorldChunk originChunk = overworld.getChunk(0, 0);

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

				ProtoChunk farChunk = (ProtoChunk) overworld.getChunkManager()
						.getChunk(FAR_CHUNK_POS.x, FAR_CHUNK_POS.z, ChunkStatus.STRUCTURE_STARTS, true);
				farChunk.setAttached(PERSISTENT, "protochunk_data");
			});
		}

		// second launch
		try (TestSingleplayerContext spContext = save.open()) {
			ServerWorld overworld = spContext.getServer().computeOnServer(MinecraftServer::getOverworld);
			WorldChunk originChunk = overworld.getChunk(0, 0);

			assertAttached(
					spContext.getServer().computeOnServer(PersistenceGametest::getSinglePlayer),
					PERSISTENT,
					"player_data",
					"Player attachment did not persist"
			);
			assertAttached(overworld, PERSISTENT, "world_data", "World attachment did not persist");
			assertAttached(originChunk, PERSISTENT, "chunk_data", "WorldChunk attachment did not persist");

			WrapperProtoChunk wrapperProtoChunk = (WrapperProtoChunk) overworld.getChunkManager()
					.getChunk(0, 0, ChunkStatus.EMPTY, true);
			assertAttached(
					wrapperProtoChunk,
					PERSISTENT,
					"chunk_data",
					"Attachment is not accessible through WrapperProtoChunk"
			);

			Chunk farChunk = overworld.getChunkManager()
					.getChunk(FAR_CHUNK_POS.x, FAR_CHUNK_POS.z, ChunkStatus.EMPTY, true);

			if (farChunk instanceof WrapperProtoChunk) {
				LOGGER.warn("Far chunk already generated, can't test persistence in ProtoChunk.");
			}

			assertAttached(farChunk, PERSISTENT, "protochunk_data", "ProtoChunk attachment did not persist");

			// load far chunk
			spContext.getServer().runCommand("tp @p 4800 ~ 0");
			spContext.getClientWorld().waitForChunksDownload();
			farChunk = overworld.getChunk(FAR_CHUNK_POS.x, FAR_CHUNK_POS.z);

			assertAttached(
					farChunk,
					PERSISTENT,
					"protochunk_data",
					"ProtoChunk attachment was not transferred to WorldChunk"
			);
		}
	}
}

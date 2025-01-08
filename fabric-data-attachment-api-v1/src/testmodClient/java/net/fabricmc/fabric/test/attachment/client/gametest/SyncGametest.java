package net.fabricmc.fabric.test.attachment.client.gametest;

import java.util.Objects;
import java.util.Properties;

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
import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestServerConnection;
import net.fabricmc.fabric.test.attachment.AttachmentTestMod;

public class SyncGametest implements FabricClientGameTest {
	private static ServerPlayerEntity getSinglePlayer(MinecraftServer server) {
		return server.getPlayerManager().getPlayerList().getFirst();
	}

	private static void setSyncedWithAll(AttachmentTarget target) {
		set(target, AttachmentTestMod.SYNCED_WITH_ALL);
	}

	private static void set(AttachmentTarget target, AttachmentType<Boolean> type) {
		target.setAttached(type, true);
	}

	private static void assertSyncedWithAll(AttachmentTarget target) {
		assertSynced(target, AttachmentTestMod.SYNCED_WITH_ALL);
	}

	private static void assertSynced(AttachmentTarget target, AttachmentType<?> type) {
		assertPresence(target, type, true);
	}

	private static void assertNotSynced(AttachmentTarget target, AttachmentType<?> type) {
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

			try (TestServerConnection connection = serverContext.connect()) {
				connection.getClientWorld().waitForChunksDownload();

				serverContext.runCommand("gamemode @p survival");
				serverContext.runOnServer(server -> {
					ServerPlayerEntity player = getSinglePlayer(server);
					setSyncedWithAll(player);
					set(player, AttachmentTestMod.SYNCED_EXCEPT_TARGET);
					set(player, AttachmentTestMod.SYNCED_CREATIVE_ONLY);

					// check registry objects are synced correctly
					player.setAttached(AttachmentTestMod.SYNCED_ITEM, Items.APPLE.getDefaultStack());
				});

				context.runOnClient(client -> {
					ClientWorld world = Objects.requireNonNull(client.world);
					Entity villager = world.getEntityById(state.villagerId);

					assertSyncedWithAll(world.getBlockEntity(state.furnacePos));
					assertSyncedWithAll(villager);
					assertSyncedWithAll(world.getChunk(0, 0));
					assertSyncedWithAll(client.player);
					assertSynced(client.player, AttachmentTestMod.SYNCED_CREATIVE_ONLY);
					assertSynced(client.player, AttachmentTestMod.SYNCED_ITEM);

					// `world` is the overworld here
					assertNotSynced(world, AttachmentTestMod.SYNCED_WITH_ALL);
					assertNotSynced(client.player, AttachmentTestMod.SYNCED_EXCEPT_TARGET);
					assertNotSynced(villager, AttachmentTestMod.SYNCED_WITH_TARGET);
				});

				// now teleport to nether, on roof to avoid suffocation when switching to survival
				serverContext.runCommand("execute in minecraft:the_nether run tp @p ~ 128 ~");

				context.runOnClient(client -> {
					assertSyncedWithAll(client.world);
					assertNotSynced(client.player, AttachmentTestMod.SYNCED_CREATIVE_ONLY);
				});
			}
		}
	}
}

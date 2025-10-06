package net.fabricmc.fabric.impl.recipe.sync;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class PacketRegistrar {
	public static void registerPayload() {
		PayloadTypeRegistry.playS2C().register(RecipeSyncPayload.ID, RecipeSyncPayload.PACKET_CODEC);
	}
}

package net.fabricmc.fabric.impl.recipe.sync;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public record RecipeSyncPayload(Set<Identifier> recipes, boolean revoke, boolean isLast) implements CustomPayload {
	public static final CustomPayload.Id<RecipeSyncPayload> ID = new Id<>(Identifier.of("fabric-recipe-sync-api-v1", "recipe_sync"));
	public static final PacketCodec<RegistryByteBuf, RecipeSyncPayload> PACKET_CODEC = PacketCodec.tuple(
		Identifier.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), RecipeSyncPayload::recipes,
		PacketCodecs.BOOLEAN, RecipeSyncPayload::revoke,
		PacketCodecs.BOOLEAN, RecipeSyncPayload::isLast,
		RecipeSyncPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}

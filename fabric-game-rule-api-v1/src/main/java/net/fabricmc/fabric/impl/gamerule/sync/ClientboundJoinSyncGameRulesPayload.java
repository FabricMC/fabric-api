package net.fabricmc.fabric.impl.gamerule.sync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRuleMap;

public record ClientboundJoinSyncGameRulesPayload(GameRuleMap gameRules) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ClientboundJoinSyncGameRulesPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.fromCodec(GameRuleMap.CODEC), ClientboundJoinSyncGameRulesPayload::gameRules,
			ClientboundJoinSyncGameRulesPayload::new
	);
	public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath("fabric", "join_sync_game_rules_v1");
	public static final Type<ClientboundJoinSyncGameRulesPayload> ID = new Type<>(PACKET_ID);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

}

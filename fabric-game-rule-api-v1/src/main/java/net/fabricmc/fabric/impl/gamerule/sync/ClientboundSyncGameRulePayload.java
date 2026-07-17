package net.fabricmc.fabric.impl.gamerule.sync;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleMap;

public record ClientboundSyncGameRulePayload<T>(GameRule<T> gameRule, T value) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncGameRulePayload<?>> CODEC = StreamCodec.of((output, value1) -> value1.write(output), ClientboundSyncGameRulePayload::fromBuf);

	public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath("fabric", "sync_game_rule_v1");
	public static final Type<ClientboundSyncGameRulePayload<?>> ID = new Type<>(PACKET_ID);

	@SuppressWarnings("unchecked")
	private static <T> ClientboundSyncGameRulePayload<T> fromBuf(RegistryFriendlyByteBuf buf) {
		GameRule<T> gameRule = (GameRule<T>) BuiltInRegistries.GAME_RULE.getOptional(buf.readIdentifier()).orElseThrow();
		return new ClientboundSyncGameRulePayload<>(gameRule, buf.readLenientJsonWithCodec(gameRule.valueCodec()));
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeIdentifier(this.gameRule.getIdentifier());
		buf.writeJsonWithCodec(this.gameRule.valueCodec(), this.value);
	}

	public void updateGameRules(GameRuleMap gameRules) {
		gameRules.set(this.gameRule, this.value);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}

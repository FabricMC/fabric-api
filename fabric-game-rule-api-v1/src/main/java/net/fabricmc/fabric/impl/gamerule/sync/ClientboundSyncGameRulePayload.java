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

package net.fabricmc.fabric.impl.gamerule.sync;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;

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

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}

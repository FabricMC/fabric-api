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

package net.fabricmc.fabric.impl.screenhandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

public final class Networking implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-screen-handler-api-v1/server");

	// [Packet format]
	// typeId: identifier
	// syncId: varInt
	// title: text
	// customData: buf
	public static final Identifier OPEN_ID = Identifier.fromNamespaceAndPath("fabric-screen-handler-api-v1", "open_screen");
	public static final Map<Identifier, StreamCodec<? super RegistryFriendlyByteBuf, ?>> CODEC_BY_ID = new HashMap<>();

	/**
	 * Opens an extended screen handler by sending a custom packet to the client.
	 *
	 * @param player  the player
	 * @param factory the screen handler factory
	 * @param handler the screen handler instance
	 * @param syncId  the synchronization ID
	 */
	@SuppressWarnings("unchecked")
	public static <D> void sendOpenPacket(ServerPlayer player, ExtendedScreenHandlerFactory<D> factory, AbstractContainerMenu handler, int syncId) {
		Objects.requireNonNull(player, "player is null");
		Objects.requireNonNull(factory, "factory is null");
		Objects.requireNonNull(handler, "handler is null");

		Identifier typeId = BuiltInRegistries.MENU.getKey(handler.getType());

		if (typeId == null) {
			LOGGER.warn("Trying to open unregistered screen handler {}", handler);
			return;
		}

		StreamCodec<RegistryFriendlyByteBuf, D> codec = (StreamCodec<RegistryFriendlyByteBuf, D>) Objects.requireNonNull(CODEC_BY_ID.get(typeId), () -> "Codec for " + typeId + " is not registered!");
		D data = factory.getScreenOpeningData(player);

		ServerPlayNetworking.send(player, new OpenScreenPayload<>(typeId, syncId, factory.getDisplayName(), codec, data));
	}

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(OpenScreenPayload.ID, OpenScreenPayload.CODEC);

		forEachEntry(BuiltInRegistries.MENU, (type, id) -> {
			if (type instanceof ExtendedScreenHandlerType<?, ?> extended) {
				CODEC_BY_ID.put(id, extended.getPacketCodec());
			}
		});
	}

	// Calls the consumer for each registry entry that has been registered or will be registered.
	private static <T> void forEachEntry(Registry<T> registry, BiConsumer<T, Identifier> consumer) {
		for (T type : registry) {
			consumer.accept(type, registry.getKey(type));
		}

		RegistryEntryAddedCallback.event(registry).register((rawId, id, type) -> {
			consumer.accept(type, id);
		});
	}

	public record OpenScreenPayload<D>(Identifier identifier, int syncId, Component title, StreamCodec<RegistryFriendlyByteBuf, D> innerCodec, D data) implements CustomPacketPayload {
		public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreenPayload<?>> CODEC = CustomPacketPayload.codec(OpenScreenPayload::write, OpenScreenPayload::fromBuf);
		public static final CustomPacketPayload.Type<OpenScreenPayload<?>> ID = new Type<>(OPEN_ID);

		@SuppressWarnings("unchecked")
		private static <D> OpenScreenPayload<D> fromBuf(RegistryFriendlyByteBuf buf) {
			Identifier id = buf.readIdentifier();
			StreamCodec<RegistryFriendlyByteBuf, D> codec = (StreamCodec<RegistryFriendlyByteBuf, D>) CODEC_BY_ID.get(id);

			return new OpenScreenPayload<>(id, buf.readByte(), ComponentSerialization.STREAM_CODEC.decode(buf), codec, codec == null ? null : codec.decode(buf));
		}

		private void write(RegistryFriendlyByteBuf buf) {
			buf.writeIdentifier(this.identifier);
			buf.writeByte(this.syncId);
			ComponentSerialization.STREAM_CODEC.encode(buf, this.title);
			this.innerCodec.encode(buf, this.data);
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}
}
